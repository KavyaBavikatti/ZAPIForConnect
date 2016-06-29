package com.thed.zephyr.cloud.rest;

import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.thed.zephyr.cloud.rest.client.*;

import com.thed.zephyr.cloud.rest.client.async.ZAsyncHttpClientFactory;
import com.thed.zephyr.cloud.rest.client.impl.*;
import com.thed.zephyr.cloud.rest.model.ZConfig;
import com.thed.zephyr.cloud.rest.util.json.ZephyrAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by aliakseimatsarski on 3/25/16.
 */
public class ZFJCloudRestClient {

    private ExecutionRestClient executionRestClient;

    private CycleRestClient cycleRestClient;

    private TeststepRestClient teststepRestClient;

    private StepResultRestClient stepResultRestClient;

    private ChartRestClient chartRestClient;

    private Logger log = LoggerFactory.getLogger(ZFJCloudRestClient.class);

    private DisposableHttpClient httpClient;

    private ZFJCloudRestClient() {
    }

    public ExecutionRestClient getExecutionRestClient() {
        return executionRestClient;
    }

    public static Builder  restBuilder(String zephyrBaseUrl, String accessKey, String secretKey, String userName){
        return new ZFJCloudRestClient().new Builder(zephyrBaseUrl, accessKey, secretKey, userName);
    }

    public CycleRestClient getCycleRestClient() {
        return cycleRestClient;
    }

    protected void finalize() throws Throwable {
        try {
            httpClient.destroy();
        } catch (Exception exception) {
            log.error("Error during destroy http client", exception);
        }
    }

    public TeststepRestClient getTeststepRestClient() {
        return teststepRestClient;
    }

    public StepResultRestClient getStepResultRestClient() {
        return stepResultRestClient;
    }

    public ChartRestClient getChartRestClient() {
        return  chartRestClient;
    }

    public class Builder {

        private String accessKey;
        private String secretKey;
        private String userName;
        private String zephyrBaseUrl;
        private String apiVersion = "1.0";

        private Builder(String zephyrBaseUrl, String accessKey, String secretKey, String userName) {
            this.zephyrBaseUrl = zephyrBaseUrl;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.userName = userName;
        }

        public ZFJCloudRestClient build() {
            ZConfig zConfig = new ZConfig(accessKey, secretKey, userName, zephyrBaseUrl);
            URI serverUri = UriBuilder.fromUri(zConfig.ZEPHYR_BASE_URL).path("/public/rest/api/" + apiVersion).build();
            HttpClientOptions options = new HttpClientOptions();
            options.setSocketTimeout(60000, TimeUnit.MILLISECONDS);
            httpClient = new ZAsyncHttpClientFactory().createClient(serverUri, new ZephyrAuthenticationHandler(zConfig), options);

            executionRestClient = new ExecutionRestClientImpl(serverUri, httpClient);
            cycleRestClient = new CycleRestClientImpl(serverUri, httpClient);
            teststepRestClient = new TeststepRestClientImpl(serverUri, httpClient);
            stepResultRestClient = new StepResultRestClientImpl(serverUri, httpClient);
            chartRestClient = new ChartRestClientImpl(serverUri, httpClient);

            log.info("ZFJCloudRestClient was successfully created with url:{}", serverUri.toString());

            return ZFJCloudRestClient.this;
        }

        public Builder setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }
    }
}
