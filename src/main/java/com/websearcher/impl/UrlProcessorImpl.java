package com.websearcher.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.websearcher.*;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implements URL Processor
 * Process method is called from the run method of the Thread.
 * Defined as guice singleton.
 */
@Singleton
class UrlProcessorImpl implements UrlProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlProcessorImpl.class);

    private final IOUtils ioUtils;
    private final ResultCollector resultCollector;
    private final AppUtils appUtils;
    private final Set<String> processedUrlSet = new HashSet<>();

    @Inject
    UrlProcessorImpl(
            AppUtils appUtils,
            IOUtils IOUtils,
            ResultCollector resultCollector) {

        this.appUtils = appUtils;
        this.ioUtils = IOUtils;
        this.resultCollector = resultCollector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processUrl(
            Iterator<CSVRecord> iterableCSVRecord) {

        while (true) {

            /**
             * Lock only for as short time as possible.
             *
             * Each thread needs to get the next url to process.
             * We want to block for as little time as possible.
             * The strategy is to check if iterator has anything left.
             * Leave crtitical section to process if it does, Exit if it does not.
             *
             * The set<> tracks URLs processed so far to avoid dups.
             * We lock on `this` as the instance is a singleton.
             */
            String urlFragment = null;
            synchronized (this) {
                if (!iterableCSVRecord.hasNext()) {
                    return;
                }
                urlFragment = appUtils.normalizeFragment(iterableCSVRecord.next().get("URL"));
                if (StringUtils.isBlank(urlFragment) ||
                        processedUrlSet.contains(urlFragment)) {
                    continue;
                }
                processedUrlSet.add(urlFragment);
            }

            /**
             * At this point we have the fragment and no need to hold the other
             * threads hostage. We are free to process this url.
             * I could have fetched, processed in memory, but thought
             * it would be nicer to see what the raw files and text files
             * looked like =)
             */
            try {

                Path rawFilePath = fetchFile(urlFragment);
                Path textFilePath = extractText(urlFragment, rawFilePath);
                List<Integer> indices = textFilePath != null ?
                        searchKeyword(textFilePath) : ImmutableList.of();

                /**
                 * Successfully processed, lets persist the results.
                 */
                resultCollector.setResult(urlFragment, indices);

            } catch (Exception e) {

                LOGGER.error(e.getMessage(), e);

                /**
                 * Lets persist the error
                 */
                resultCollector.setError(urlFragment, e.getMessage());
            }
        }
    }

    /**
     * Read file contents and get matching indices
     *
     * @param textFilePath
     * @return
     * @throws IOException
     */
    private List<Integer> searchKeyword(
            Path textFilePath) throws IOException {

        String text = ioUtils.readTextFile(new File(textFilePath.toString()));
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return appUtils.findAllMatches(text);
    }

    /**
     * Extract and store text from raw html locally in <root folder>/text/
     *
     * @param urlFragment
     * @param rawFilePath
     * @return
     * @throws IOException
     */
    private Path extractText(
            String urlFragment,
            Path rawFilePath) throws IOException {

        String html = ioUtils.readTextFile(new File(rawFilePath.toString()));
        if (StringUtils.isBlank(html)) {
            return null;
        }
        String text = Jsoup.parse(html).text();
        if (StringUtils.isBlank(text)) {
            return null;
        }
        text = text.toLowerCase();

        Path localTextFiPath = appUtils.getLocalTextFilePath(urlFragment);
        ioUtils.writeTextFile(new File(localTextFiPath.toString()), text);
        return localTextFiPath;
    }

    /**
     * Fetch html and copy it locally in <root folder>/raw/
     *
     * @param urlFragment
     * @return
     * @throws IOException
     */
    private Path fetchFile(String urlFragment) throws IOException {

        Path localHTML = appUtils.getLocalRawFilePath(urlFragment);
        String url = appUtils.getUrl(urlFragment);
        LOGGER.info(MessageFormat.format(
                "Fetch url {0} to {1}",
                url, localHTML.toString()));
        ioUtils.getHttpFile(url, localHTML.toString(), Constants.CONNECTION_TIMEOUT);
        return localHTML;
    }
}
