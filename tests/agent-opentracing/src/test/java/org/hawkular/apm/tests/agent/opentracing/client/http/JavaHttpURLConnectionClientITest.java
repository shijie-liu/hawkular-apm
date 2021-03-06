/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.apm.tests.agent.opentracing.client.http;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.hawkular.apm.tests.common.Wait;
import org.junit.Ignore;
import org.junit.Test;

import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;

/**
 * @author gbrown
 */
public class JavaHttpURLConnectionClientITest extends AbstractBaseHttpITest {

    private static final String SAY_HELLO_URL = "http://localhost:8180/sayHello";

    @Test
    public void testHttpURLConnectionConnectGET() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    public void testHttpURLConnectionGetStatusCodeGET() throws IOException {
        testHttpRequestGetStatusCode(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamGET() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    public void testHttpURLConnectionConnectGETWithFault() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    @Ignore("HWKAPM-863 - multiple spans being created for same single use of HttpURLConnection")
    public void testHttpURLConnectionInputStreamGETWithFault() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "GET", true);
    }

    @Test
    public void testHttpURLConnectionConnectPUT() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "PUT", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamPUT() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "PUT", false);
    }

    @Test
    public void testHttpURLConnectionConnectPOST() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "POST", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamPOST() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "POST", false);
    }

    protected void testHttpRequestConnect(URL url, String method, boolean fault) throws IOException {
        sendRequest(url, method, fault, true);
    }

    protected void testHttpRequestGetStatusCode(URL url, String method, boolean fault) throws IOException {
        sendRequest(url, method, fault, false);
    }

    protected void sendRequest(URL url, String method, boolean fault, boolean connect) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);

        connection.addRequestProperty("test-header", "test-value");
        if (fault) {
            connection.addRequestProperty("test-fault", "true");
        }

        if (connect) {
            connection.connect();
        }

        int status = connection.getResponseCode();

        if (!fault) {
            assertEquals("Unexpected response code", 200, status);
        } else {
            assertEquals("Unexpected fault response code", 401, status);
        }

        // Call again to make sure does not attempt to finish the span again
        connection.getResponseCode();

        verifyTrace(url, method, fault);
    }

    protected void sendRequestUsingInputStream(URL url, String method, boolean fault) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);

        connection.addRequestProperty("test-header", "test-value");
        if (fault) {
            connection.addRequestProperty("test-fault", "true");
        }

        try (InputStream is = connection.getInputStream()) {
        } catch (IOException ioe) {
            if (!fault) {
                throw ioe;
            }
        }

        int status = connection.getResponseCode();

        if (!fault) {
            assertEquals("Unexpected response code", 200, status);
        } else {
            assertEquals("Unexpected fault response code", 401, status);
        }

        // Call again to make sure does not attempt to finish the span again
        connection.getResponseCode();

        verifyTrace(url, method, fault);
    }

    protected void verifyTrace(URL url, String method, boolean fault) {

        Wait.until(() -> getTracer().finishedSpans().size() == 1);

        List<MockSpan> spans = getTracer().finishedSpans();
        assertEquals(1, spans.size());
        assertEquals(Tags.SPAN_KIND_CLIENT, spans.get(0).tags().get(Tags.SPAN_KIND.getKey()));
        assertEquals(method, spans.get(0).operationName());
        assertEquals(url.toString(), spans.get(0).tags().get(Tags.HTTP_URL.getKey()));
        if (fault) {
            assertEquals("401", spans.get(0).tags().get(Tags.HTTP_STATUS.getKey()));
        }
    }

}
