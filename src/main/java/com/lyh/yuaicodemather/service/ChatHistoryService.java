package com.lyh.yuaicodemather.service;

import com.lyh.yuaicodemather.model.dto.chathistory.ChatHistoryQueryRequest;
import com.lyh.yuaicodemather.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lyh.yuaicodemather.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/lyhhhhhhhhhhhh">lyhhhhhhhhhhhh</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加消息
     * @param appId 应用Id
     * @param message 消息
     * @param messageType 消息类型(user/ai)
     * @param userId 用户id
     * @return 是否成功插入
     */
    boolean addChatMessage(Long appId,String message,String messageType,Long userId);

    /**
     * 根据应用Id删除历史记录
     * @param appId 应用Id
     * @return 是否成功删除
     */
    boolean deleteByAppId(Long appId);

    /**
     * 根据应用Id分页查询历史记录
     * @param appId 应用Id
     * @param pageSize 每页大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser 登录用户
     * @return 历史记录
     */
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser);

    /**
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 加载历史记录到内存
     * @param appId 应用Id
     * @param chatMemory 内存
     * @param maxCount 最大数量
     * @return 加载数量
     */
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
