package com.websearcher.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.websearcher.ResultCollector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton to store results of processing
 */
@Singleton
class ResultCollectorImpl implements ResultCollector {

    private final Map<String, String> errorMap = new HashMap<>();
    private final Map<String, List<Integer>> searchResultsMap = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResult(String url, List<Integer> searchIndexArray) {

        /**
         * Copy array to an immutable collection.
         * Synchronize on the map to only allow one thread
         * at a time to insert into map
         */
        List<Integer> finalResultList = ImmutableList.copyOf(searchIndexArray);
        synchronized (searchResultsMap) {
            searchResultsMap.put(url, finalResultList);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setError(String url, String error) {

        /**
         * Synchronize on the map to only allow one thread
         * at a time to insert into map
         */
        synchronized (errorMap) {
            errorMap.put(url, error);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getErrorMap() {
        return Collections.unmodifiableMap(errorMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Integer>> getSearchResultMap() {
        return Collections.unmodifiableMap(searchResultsMap);
    }
}
