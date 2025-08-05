package com.lyh.yuaicodemather.service;

import com.lyh.yuaicodemather.model.dto.app.AppQueryRequest;
import com.lyh.yuaicodemather.model.vo.app.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lyh.yuaicodemather.model.entity.App;

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
}
