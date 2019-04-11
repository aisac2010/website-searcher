package com.websearcher.impl;

import com.google.inject.AbstractModule;
import com.websearcher.IOUtils;
import com.websearcher.ResultCollector;
import com.websearcher.UrlProcessor;

public class WebSearcherModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();
        bind(IOUtils.class).to(IOUtilsImpl.class);
        bind(UrlProcessor.class).to(UrlProcessorImpl.class);
        bind(ResultCollector.class).to(ResultCollectorImpl.class);
    }
}
