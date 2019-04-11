package com.websearcher.impl;

import com.websearcher.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Iterator;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UrlProcessorImplTest {

    private IOUtils ioUtils = null;
    private ResultCollector resultCollector = null;
    private UrlProcessor urlProcessor = null;
    @Before
    public void setupTests() throws IOException {

        ioUtils = mock(IOUtils.class);
        resultCollector = mock(ResultCollector.class);
        urlProcessor = new UrlProcessorImpl(
                new AppUtils("test", "/tmp"),
                ioUtils, resultCollector);
    }

    @Test
    public void testProcessUrl() throws IOException {

        doNothing().when(ioUtils).getHttpFile(
                anyString(), anyString(), eq(Constants.CONNECTION_TIMEOUT));
        doNothing().when(ioUtils).writeTextFile(any(File.class), anyString());
        when(ioUtils.readTextFile(any(File.class))).thenReturn("This is a text file");

        try(ByteArrayInputStream is = new ByteArrayInputStream(
                createSampleCSV(
                        "facebook.com",
                        "google.com",
                        "twitter.com").getBytes());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {

            Iterator<CSVRecord> recordsIterator =
                    CSVFormat.EXCEL.withHeader().parse(bufferedReader).iterator();
            urlProcessor.processUrl(recordsIterator);
        }

        verify(ioUtils).getHttpFile(eq("http://facebook.com"), any(), eq(Constants.CONNECTION_TIMEOUT));
        verify(ioUtils).getHttpFile(eq("http://google.com"), any(), eq(Constants.CONNECTION_TIMEOUT));
        verify(ioUtils).getHttpFile(eq("http://twitter.com"), any(), eq(Constants.CONNECTION_TIMEOUT));

        verify(ioUtils, times(3)).writeTextFile(any(File.class), anyString());
        verify(ioUtils, times(6)).readTextFile(any(File.class));
    }

    private String createSampleCSV(String...urls) throws IOException {

        try(ByteArrayOutputStream os = new ByteArrayOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os))) {
            CSVPrinter csvPrinter =
                    new CSVPrinter(bufferedWriter,
                            CSVFormat.EXCEL.withHeader("ID", "URL"));
            int index = 0;
            for(String url : urls) {
                csvPrinter.printRecord(index+"", url);
            }

            csvPrinter.flush();
            return os.toString();
        }
    }
}
