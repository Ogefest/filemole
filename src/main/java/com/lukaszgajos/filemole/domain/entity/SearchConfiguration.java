package com.lukaszgajos.filemole.domain.entity;

import com.lukaszgajos.filemole.domain.entity.search.Filter;

import java.util.ArrayList;
import java.util.List;

public class SearchConfiguration {

    private List<Filter> filters = new ArrayList<>();

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public List<Filter> getFilters() {
        return filters;
    }
}
