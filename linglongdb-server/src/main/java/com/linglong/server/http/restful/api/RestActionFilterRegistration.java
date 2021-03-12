package com.linglong.server.http.restful.api;

import java.util.List;

/**
 * @author Stereo on 2019/8/12.
 */
public interface RestActionFilterRegistration {

    void registerFilter(RestActionFilter restActionFilter);

    void registerFilter(List<RestActionFilter> restActionFilters);
}
