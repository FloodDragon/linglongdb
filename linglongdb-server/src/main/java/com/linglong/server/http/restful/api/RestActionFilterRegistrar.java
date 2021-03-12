package com.linglong.server.http.restful.api;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stereo on 2019/9/11.
 */
public final class RestActionFilterRegistrar implements RestActionFilterRegistration {

    private static final List<RestActionFilter> backendFilters = new ArrayList<>();

    protected static RestActionFilter[] getFilters() {
        return backendFilters.size() > 0 ? backendFilters.toArray(new RestActionFilter[backendFilters.size()]) : null;
    }

    @Override
    public void registerFilter(RestActionFilter backendFilter) {
        if (backendFilter != null)
            backendFilters.add(backendFilter);
    }

    @Override
    public void registerFilter(List<RestActionFilter> backendFilters) {
        if (!CollectionUtils.isEmpty(backendFilters))
            backendFilters.addAll(backendFilters);
    }
}
