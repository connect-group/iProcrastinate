package com.connect_group.iprocrastinate;

import org.apache.log4j.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RequestProcessor {
    private static final Logger logger = Logger.getLogger(RequestProcessor.class);

    private final ScheduledExecutorService executorService;
    private final PriorityBlockingQueue<Request> queue = new PriorityBlockingQueue<>(20000);

    public RequestProcessor() {
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::tick, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void add(Request request) {
        queue.add(request);
    }

    private void tick() {
        int count = 0;
        ArrayList<Request> expired = new ArrayList<>();
        final long now = System.currentTimeMillis();
        Request r = queue.peek();
        while(r!=null && r.isExpired(now)) {
            r = queue.poll();
            expired.add(r);
            r = queue.peek();
            count ++;
        }


        if(count>0) {
            logger.trace(String.format("Tick (%d requests)", count));
        }

        expired.parallelStream().forEach(this::complete);
    }

    private void complete(Request request) {
        final AsyncContext context = request.getContext();
        final HttpServletResponse response = (HttpServletResponse) context.getResponse();

        if(request.getRedirectUrl()!=null) {
            response.setHeader("Location", request.getRedirectUrl());

            if(request.isPost()) {
                response.setStatus(307);
            } else {
                response.setStatus(302);
            }
        }

        try {
            ServletOutputStream sos = response.getOutputStream();
            sos.setWriteListener(new WriteListener() {
                @Override
                public void onWritePossible() throws IOException {
                    if (sos.isReady()) {
                        sos.println("{ \"result\": \"OK\" }");
                        context.complete();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.trace("Error occurred", throwable);
                    context.complete();
                }
            });
        } catch (Throwable t) {
            logger.trace("Failed to get output stream", t);
            context.complete();
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void destroy() {
        executorService.shutdownNow();
    }
}
