package com.websearcher;

import java.util.List;
import java.util.Map;

/**
 * Used to collect result after processing a URL
 */
public interface ResultCollector {

    /**
     * Called after successful processing of result
     * @param url : url
     * @param searchIndexArray : Array containing the indices where the
     *                         keyword was located
     */
    void setResult(
            String url,
            List<Integer> searchIndexArray);

    /**
     * Called when processing resulted in error.
     * @param url
     * @param error
     */
    void setError(
            String url,
            String error);

    /**
     * Get errors
     * @return
     */
    Map<String, String> getErrorMap();

    /**
     * Get results
     * @return
     */
    Map<String, List<Integer>> getSearchResultMap();
}
