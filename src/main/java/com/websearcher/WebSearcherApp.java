package com.websearcher;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.websearcher.impl.WebSearcherModule;
import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Main App
 */
public class WebSearcherApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSearcherApp.class);

    /**
     * Main does the following
     * - Processes CLI arguments
     * - Initializes guice
     * - Fetches CSV pointed to by the given URL
     * - Spawns threads
     * - Waits for thread completion and joins
     * - Creates result output
     * @param args
     */
    public static void main(String[] args) {

        try {

            /**
             * Init CLI
             */
            CommandLine cmd = processCommandlineArgs(args);

            LOGGER.info("Starting WebSearcherApp ...");
            LOGGER.info("Configuring Guice...");

            /**
             * Initializes guice
             */
            final AppUtils appUtils = new AppUtils(
                    cmd.getOptionValue("keyword"),
                    cmd.getOptionValue("output"));
            Injector injector = Guice.createInjector(
                    new AbstractModule() {

                        @Provides
                        public AppUtils getAppUtils() {
                            return appUtils;
                        }

                    },
                    new WebSearcherModule());

            /**
             * Initialize folders
             */
            injector.getInstance(IOUtils.class).initFolders();

            /**
             * Download csv
             */
            Path urlsFilePath = appUtils.getLocalUrlSeedFilePath();
            LOGGER.info("Fetching urls seed file to " + urlsFilePath.toString());
            injector.getInstance(IOUtils.class).getHttpFile(
                    Constants.INPUT_FILE_URL,
                    urlsFilePath.toString(), Constants.CONNECTION_TIMEOUT);

            /**
             * Process URLs
             */
            try(Reader in = new FileReader(urlsFilePath.toString())) {

                Iterator<CSVRecord> recordsIterator =
                        CSVFormat.EXCEL.withHeader().parse(in).iterator();
                Thread threads[] = createThreads(
                        injector.getInstance(UrlProcessor.class),
                        recordsIterator);
                joinThreads(threads);
            }

            /**
             * Create result output
             */
            LOGGER.info("Processing completed.");
            String resultText = getResultsText(
                    injector.getInstance(ResultCollector.class));

            LOGGER.info(resultText);
            FileUtils.writeStringToFile(
                    new File(appUtils.getResultsFilePath().toString()),
                    resultText, Charset.defaultCharset());
            LOGGER.info("Processed Results at : " +
                    appUtils.getResultsFilePath().toString());
            System.exit(0);

        } catch (IOException | InterruptedException e) {

            LOGGER.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    /**
     * Process command line args
     * @param args : as received from user
     * @return
     */
    private static CommandLine processCommandlineArgs(String[] args) {

        Options options = new Options();

        Option input = new Option("k", "keyword", true, "keyword text");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output folder path");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("websearcher", options);

            System.exit(-1);
        }
        return null;
    }

    /**
     * Spawning Threads to process each URL.
     * The strategy is to let give the csv records iterator
     * so each thread can process the next available url
     *
     * @param urlProcessor
     * @param recordsIterator
     * @return
     */
    private static Thread[] createThreads(
            UrlProcessor urlProcessor,
            Iterator<CSVRecord> recordsIterator) {

        Thread[] threads = new Thread[Constants.THREAD_COUNT];
        //Spawn threads and process
        for (int threadIndex = 0;
             threadIndex < Constants.THREAD_COUNT;
             threadIndex++) {

            threads[threadIndex] = new Thread(new Runnable() {
                @Override
                public void run() {
                    urlProcessor.processUrl(recordsIterator);
                }
            });
            threads[threadIndex].start();
        }
        return threads;
    }

    /**
     * Wait and make sure all threads have finished processing
     * @param threads
     * @throws InterruptedException
     */
    private static void joinThreads(Thread[] threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Format and return the result.
     * @param resultCollector
     * @return
     */
    private static String getResultsText(ResultCollector resultCollector) {

        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> errorMap = resultCollector.getErrorMap();
        stringBuilder.append(MessageFormat.format(
                "{0} Urls Errored out.\n", errorMap.size()));
        for(String urlFragment : errorMap.keySet()) {

            stringBuilder.append(MessageFormat.format(
                    "{0} : {1}\n", urlFragment,
                    errorMap.get(urlFragment)));
        }
        Map<String, List<Integer>> resultsMap = resultCollector.getSearchResultMap();
        stringBuilder.append(MessageFormat.format(
                "{0} Urls Processed.\n", resultsMap.size()));
        for(String urlFragment : resultsMap.keySet()) {

            stringBuilder.append(MessageFormat.format(
                    "{0} : {1}\n", urlFragment,
                    resultsMap.get(urlFragment.toString())));
        }

        return stringBuilder.toString();
    }
}
