package com.lyh.yuaicodemather.service;

import com.lyh.yuaicodemather.model.dto.app.AppQueryRequest;
import com.lyh.yuaicodemather.model.entity.DiffResultVO;
import com.lyh.yuaicodemather.model.entity.User;
import com.lyh.yuaicodemather.model.enums.CodeGenTypeEnum;
import com.lyh.yuaicodemather.model.vo.app.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lyh.yuaicodemather.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/lyhhhhhhhhhhhh">lyhhhhhhhhhhhh</a>
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    Flux<String> chatToGenCode(Long appId, String prompt, User loginUser);

    String deployApp(Long appId, User loginUser);

    DiffResultVO getVersionDiff(String codeGenTypeEnum, Long appId);
}
