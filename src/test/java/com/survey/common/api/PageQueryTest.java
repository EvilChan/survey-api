package com.survey.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PageQueryTest {

    @Test
    void defaultsWhenNull() {
        PageQuery q = new PageQuery(null, null);
        assertEquals(1, q.pageOrDefault());
        assertEquals(20, q.sizeOrDefault());
    }

    @Test
    void clampsSizeUpperBound() {
        assertEquals(100, new PageQuery(1, 500).sizeOrDefault());
    }

    @Test
    void normalizesInvalidPage() {
        assertEquals(1, new PageQuery(0, 10).pageOrDefault());
    }
}
