package com.survey.common.api;

public record PageQuery(Integer page, Integer size) {
    public int pageOrDefault() {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    public int sizeOrDefault() {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
