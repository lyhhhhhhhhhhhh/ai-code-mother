package com.lyh.yuaicodemather.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.lyh.yuaicodemather.constant.AppConstant;
import com.lyh.yuaicodemather.core.AiCodeGeneratorFacade;
import com.lyh.yuaicodemather.exception.BusinessException;
import com.lyh.yuaicodemather.exception.ErrorCode;
import com.lyh.yuaicodemather.exception.ThrowUtils;
import com.lyh.yuaicodemather.mapper.AppMapper;
import com.lyh.yuaicodemather.model.dto.app.AppQueryRequest;
import com.lyh.yuaicodemather.model.entity.App;
import com.lyh.yuaicodemather.model.entity.DiffResultVO;
import com.lyh.yuaicodemather.model.entity.User;
import com.lyh.yuaicodemather.model.enums.ChatHistoryMessageTypeEnum;
import com.lyh.yuaicodemather.model.enums.CodeGenTypeEnum;
import com.lyh.yuaicodemather.model.vo.app.AppVO;
import com.lyh.yuaicodemather.model.vo.user.UserVO;
import com.lyh.yuaicodemather.service.AppService;
import com.lyh.yuaicodemather.service.ChatHistoryService;
import com.lyh.yuaicodemather.service.UserService;
import com.lyh.yuaicodemather.utils.FolderDiffUtil;
import com.lyh.yuaicodemather.utils.VersionUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/lyhhhhhhhhhhhh">lyhhhhhhhhhhhh</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;


    /**
     *
     * @param appId 应用Id
     * @param prompt 提示词
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId,String prompt,User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(prompt == null, ErrorCode.PARAMS_ERROR, "提示词不能为空");
        // 2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        // 3.权限校验，仅本人可以和自己的应用对话
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限");
        // 4.获取应用的代码类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum genTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(genTypeEnum == null, ErrorCode.PARAMS_ERROR, "应用代码类型错误");
        // 5.在调用AI前 先保存用户消息到数据库中
        chatHistoryService.addChatMessage(appId, prompt, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 6.流式调用AI进行生成
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(prompt, genTypeEnum, appId);
        // 7.收集AI响应内容，并且在完成后保存记录到对话历史
        StringBuilder aiResponseBuilder = new StringBuilder();
        // 这里return 返回的内容其实就是codeStream 所以在上一步
        return contentFlux.map(chunk -> {
            // 实时收集代码片段
            aiResponseBuilder.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
            // 流式返回完成之后，保存AI消息到对话历史中
            chatHistoryService.addChatMessage(appId, aiResponseBuilder.toString(),
                    ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        }).doOnError(error -> {
            // 如果发生错误，也需要保存到数据库当中
            String aiErrorMessage = "AI 回复失败" + error.getMessage();
            chatHistoryService.addChatMessage(appId, aiErrorMessage,
                    ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        });
    }

     /**
     * 获取应用VO
     * @param app 应用实体
     * @return
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    /**
     * 根据应用列表获取应用VO列表
     * @param appList 应用列表
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        // 如果为应用列表为空 直接返回空
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    /**
     * 根据请求参数获取查询条件
     * 这里如果某一字段为空 就不会构造 mybatis-flex会自动判断
     * @param appQueryRequest 查询请求实体
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 部署应用
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String latestVersionDir = VersionUtils.getLatestVersionDir(sourceDirName);
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName + File.separator + latestVersionDir;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    /**
     * 查看文件diff
     * @param codeGenTypeEnum 文件类型
     * @param appId 应用ID
     * @return
     */
    @Override
    public DiffResultVO getVersionDiff(String codeGenTypeEnum, Long appId) {
        // 1.参数校验
        ThrowUtils.throwIf(CodeGenTypeEnum.getEnumByValue(codeGenTypeEnum) == null, ErrorCode.PARAMS_ERROR, "代码生成类型不正确");
        ThrowUtils.throwIf(StrUtil.isBlank(codeGenTypeEnum), ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 2.获取JSON文件 查询最近的两次文件差异
        String deployKey = codeGenTypeEnum + "_" + appId;
        String[] latestTwoVersionDirs = VersionUtils.getLatestTwoVersionDirs(deployKey);
        // 如果长度为1 抛出异常 没有版本差异
        ThrowUtils.throwIf(latestTwoVersionDirs == null || latestTwoVersionDirs.length == 0, ErrorCode.OPERATION_ERROR,"没有版本差异");
        ThrowUtils.throwIf(latestTwoVersionDirs.length < 2 || latestTwoVersionDirs[1] == null, ErrorCode.OPERATION_ERROR,"没有版本差异");
        // 3.获取两个文件夹
        String oldVersionPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + deployKey + File.separator + latestTwoVersionDirs[1];
        String newVersionPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + deployKey + File.separator + latestTwoVersionDirs[0];
        // 4.比较两个文件的差异
        try {
            return FolderDiffUtil.compareFolders(oldVersionPath, newVersionPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "比较文件差异失败：");
        }
    }

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }
}