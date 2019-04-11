package com.websearcher;

import java.io.File;
import java.io.IOException;

/**
 * I/O Operations.
 * Interface mocked during tests
 */
public interface IOUtils {

    /**
     * init Folders
     */
    void initFolders() throws IOException;

    /**
     * Read text file and return as String
     * @param path
     * @return
     * @throws IOException
     */
    String readTextFile(File path) throws IOException;

    /**
     * Write text file
     * @param path
     * @param text
     * @throws IOException
     */
    void writeTextFile(File path, String text) throws IOException;

    /**
     * Gets a Contents of what the URL points to in a file.
     *
     * @param url : url
     * @param filePath : Local path where file needs to be copied to
     * @throws IOException
     */
    void getHttpFile(
            String url,
            String filePath,
            int timeOut) throws IOException;
}
