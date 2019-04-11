package com.websearcher.impl;

import com.google.common.collect.ImmutableList;
import com.websearcher.ResultCollector;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ResultCollectorImplTest {

    private ResultCollector resultCollector = null;

    @Before
    public void setupTests() {
        resultCollector = new ResultCollectorImpl();
    }

    @Test
    public void setResultTest() {
        resultCollector.setResult("test", ImmutableList.of(1,2,3));
        Map<String, List<Integer>> results = resultCollector.getSearchResultMap();
        assertEquals(1, results.size());
        assertEquals(3, results.values().iterator().next().size());
    }

    //More tests here
}
