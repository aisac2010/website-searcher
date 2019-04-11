package com.websearcher;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instance created from App with methods
 * providing various information like paths
 * and helpers.
 */
public final class AppUtils {

    private final Path rootFolderPath;  //Root path
    private final Path rawFileRoot;     //Raw files are HTML files downloaded
    private final Path textFileRoot;    //text files are text files extracted from the html files
    private final Pattern pattern;      //Regex pattern to match

    /**
     * Constructor initializes directories
     *
     * @param keyword
     * @param outputFolder
     * @throws IOException
     */
    public AppUtils(
            String keyword,
            String outputFolder) throws IOException {

        rootFolderPath = Paths.get(outputFolder);
        rawFileRoot = Paths.get(rootFolderPath.toString(), Constants.RAW_FOLDER);
        textFileRoot = Paths.get(rootFolderPath.toString(), Constants.TEXT_FOLDER);

        String regexString = MessageFormat.format("\\b{0}\\b", keyword);
        pattern = Pattern.compile(regexString);
    }

    public Path getRootFolderPath() {
        return rootFolderPath;
    }

    public Path getRawFileRoot() {
        return rawFileRoot;
    }

    public Path getTextFileRoot() {
        return textFileRoot;
    }

    /**
     * Given a fragment like walmart.com, create the url
     * like http://walmart.com
     *
     * @param urlFragment
     * @return
     */
    public String getUrl(String urlFragment) {
        return (urlFragment.startsWith("http://")? "" : "http://")+urlFragment;
    }

    /**
     * Path where given seed file is locally copied to
     * @return
     */
    public Path getLocalUrlSeedFilePath() {
        return Paths.get(rootFolderPath.toString(), "urls.txt");
    }

    /**
     * Path to where the HTML file pointed to by the fragemnt must be stored
     * locally.
     * @param urlFragment
     * @return
     */
    public Path getLocalRawFilePath(String urlFragment) {
        return Paths.get(rawFileRoot.toString(), urlFragment+".html");
    }

    /**
     * Path to save the extracted text file
     * @param urlFragment
     * @return
     */
    public Path getLocalTextFilePath(String urlFragment) {
        return Paths.get(textFileRoot.toString(), urlFragment+".txt");
    }

    /**
     * Final Results path
     * @return
     */
    public Path getResultsFilePath() {
        return Paths.get(rootFolderPath.toString(), "results.txt");
    }

    /**
     * Called on the value in the CSV to remove the trailing / if
     * it exists.
     *
     * @param urlFragment
     * @return
     */
    public String normalizeFragment(String urlFragment) {
        if (StringUtils.isBlank(urlFragment)) {
            return null;
        }
        return urlFragment.endsWith("/") ?
                urlFragment.substring(0, urlFragment.length()-1) : urlFragment;
    }

    /**
     * Find matches for the keyword in the given text.
     *
     * @param text
     * @return
     */
    public List<Integer> findAllMatches(String text) {

        List<Integer> searchResults = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while(matcher.find()) {
            searchResults.add(matcher.start());
        }
        return searchResults;
    }
}
