package com.linglong.server.http.restful.api;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stereo on 2019/9/11.
 */
public final class RestActionFilterRegistrar implements RestActionFilterRegistration {

    private static final List<RestActionFilter> restActionFilters = new ArrayList<>();

    protected static RestActionFilter[] getFilters() {
        return restActionFilters.size() > 0 ? restActionFilters.toArray(new RestActionFilter[restActionFilters.size()]) : null;
    }

    @Override
    public void registerFilter(RestActionFilter restActionFilter) {
        if (restActionFilter != null) {
            this.restActionFilters.add(restActionFilter);
        }
    }

    @Override
    public void registerFilter(List<RestActionFilter> restActionFilters) {
        if (!CollectionUtils.isEmpty(restActionFilters)) {
            this.restActionFilters.addAll(restActionFilters);
        }
    }
}
