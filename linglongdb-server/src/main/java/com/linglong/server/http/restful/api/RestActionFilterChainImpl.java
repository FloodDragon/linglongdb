package com.linglong.server.http.restful.api;

/**
 * @author Stereo on 2019/8/9.
 */
public final class RestActionFilterChainImpl implements RestActionFilterChain {

    private RestActionFilter[] filters = new RestActionFilter[0];
    private int pos = 0;
    private int len = 0;
    private boolean filtered;
    private RestActionFilterCallback callback;

    public RestActionFilterChainImpl(RestActionFilter[] filters, RestActionFilterCallback callback) {
        this.callback = callback;
        if (filters != null && filters.length > 0) {
            for (RestActionFilter backendFilter : filters) {
                addFilter(backendFilter);
            }
        }
    }

    public boolean isFiltered() {
        return filtered;
    }

    @Override
    public void doFilter(RestAction restAction) {
        if (this.pos < this.len) {
            filtered = true;
            RestActionFilter restActionFilter = this.filters[this.pos++];
            restActionFilter.doFilter(restAction, this);
        } else {
            callback.call();
            filtered = false;
        }
    }

    @Override
    public void addFilter(RestActionFilter restActionFilter) {
        RestActionFilter[] newFilters = this.filters;
        int len$ = newFilters.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            RestActionFilter filter = newFilters[i$];
            if (filter == restActionFilter) {
                return;
            }
        }

        if (this.len == this.filters.length) {
            newFilters = new RestActionFilter[this.len + 10];
            System.arraycopy(this.filters, 0, newFilters, 0, this.len);
            this.filters = newFilters;
        }
        this.filters[this.len++] = restActionFilter;
    }
}
