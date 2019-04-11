package com.websearcher;

import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;

/**
 * The main processor interface
 */
public interface UrlProcessor {

    /**
     * Process records pointed to by iterator
     * @param iterableCSVRecord
     */
    void processUrl(
            Iterator<CSVRecord> iterableCSVRecord);
}
