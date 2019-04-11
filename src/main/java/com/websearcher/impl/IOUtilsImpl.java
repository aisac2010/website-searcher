package com.websearcher.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.websearcher.AppUtils;
import com.websearcher.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Stateless Thread safe HTTP File fetcher
 */
@Singleton
class IOUtilsImpl implements IOUtils {

    private final AppUtils appUtils;

    @Inject
    IOUtilsImpl(AppUtils appUtils) throws IOException {
        this.appUtils = appUtils;
    }

    @Override
    public void initFolders() throws IOException {

        Files.createDirectories(appUtils.getRootFolderPath());
        Files.createDirectories(appUtils.getRawFileRoot());
        Files.createDirectories(appUtils.getTextFileRoot());
    }

    @Override
    public String readTextFile(File file) throws IOException {
        return FileUtils.readFileToString(
                file, Charset.defaultCharset());
    }

    @Override
    public void writeTextFile(File file, String text) throws IOException {
        FileUtils.writeStringToFile(
                file, text, Charset.defaultCharset());
    }

    public void getHttpFile(String url, String filePath, int timeout) throws IOException {

        HttpGet httpget = new HttpGet(url);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        try(CloseableHttpClient httpclient =
                    HttpClientBuilder.create()
                            .setDefaultRequestConfig(config).build()) {

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try(BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)))) {
                    int inByte;
                    while ((inByte = bis.read()) != -1) {
                        bos.write(inByte);
                    }
                }
            }
        }
    }
}
