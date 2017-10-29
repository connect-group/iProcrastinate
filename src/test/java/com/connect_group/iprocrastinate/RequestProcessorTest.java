package com.connect_group.iprocrastinate;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestProcessorTest {
    private RequestProcessor processor;
    private AsyncContext asyncContext;
    private StringBuilder result = new StringBuilder();
    private WriteListener writeListener;
    private Map<String, String> headers;
    private int status = 0;


    @Before
    public void setup() throws IOException {
        processor = new RequestProcessor();
        headers = new HashMap<>();

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletOutputStream sos = mock(ServletOutputStream.class);
        asyncContext = mock(AsyncContext.class);
        when(asyncContext.getResponse()).thenReturn(response);
        when(response.getOutputStream()).thenReturn(sos);

        doAnswer(invocationOnMock -> {
            headers.put(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
            return null;
        }).when(response).setHeader(any(), any());

        doAnswer(invocationOnMock -> {
            status = invocationOnMock.getArgument(0);
            return null;
        }).when(response).setStatus(anyInt());

        when(sos.isReady()).thenReturn(true);
        doAnswer(invocationOnMock -> {
            writeListener = invocationOnMock.getArgument(0);
            return null;
        }).when(sos).setWriteListener(any());

        doAnswer(invocationOnMock -> {
            result.append(invocationOnMock.getArgument(0).toString());
            return null;
        }).when(sos).println(any());
    }

    @Test
    public void shouldAddRequestToQueue() {
        processor.add(new Request("1000", null, asyncContext));
        assertThat(processor.isEmpty(), equalTo(false));
    }

    @Test
    public void shouldRemoveRequestFromQueue() throws InterruptedException {
        processor.add(new Request("0", null, asyncContext));
        Thread.sleep(200);
        assertThat(processor.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldWriteExpectedResponse() throws InterruptedException, IOException {
        processor.add(new Request("0", null, asyncContext));
        Thread.sleep(200);
        writeListener.onWritePossible();

        assertThat(result.toString(), equalTo("{ \"result\": \"OK\" }"));
    }

    @Test
    public void shouldRedirect_WhenRedirectParamInQuerystring() throws InterruptedException {
        processor.add(new Request("0", "http://news.bbc.co.uk", asyncContext));
        Thread.sleep(200);

        assertThat(headers.get("Location"), equalTo("http://news.bbc.co.uk"));
        assertThat(status, equalTo(302));
    }

    @Test
    public void shouldPerform307Redirect_WhenRedirectParamInQuerystring_AndPostRequest() throws InterruptedException {
        processor.add(new Request("0", "http://www.example.com", asyncContext, true));
        Thread.sleep(200);

        assertThat(status, equalTo(307));
    }
}