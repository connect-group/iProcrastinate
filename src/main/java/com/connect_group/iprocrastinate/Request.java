package com.connect_group.iprocrastinate;

import javax.servlet.AsyncContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

public class Request implements Comparable<Request> {

    private final long minWait, maxWait, expires;
    private final AsyncContext context;
    private final String redirectUrl;
    private final boolean post;

    public Request(String path, String redirectUrl, AsyncContext context) {
        this(path, redirectUrl, context, false);
    }

    public Request(String path, String redirectUrl, AsyncContext context, boolean post) {
        this.context = context;

        if(redirectUrl!=null) {
            try {
                redirectUrl = new URL(redirectUrl).toString();
            } catch (MalformedURLException e) {
                redirectUrl = null;
            }
        }
        this.redirectUrl = redirectUrl;

        long[] range = getRange(path);

        this.minWait = Math.min(range[0], range[1]);
        this.maxWait = Math.max(range[0], range[1]);
        this.expires = calculateExpires();
        this.post = post;
    }

    private long calculateExpires() {
        long now = System.currentTimeMillis();
        if(minWait!=maxWait) {
            return now + ThreadLocalRandom.current().nextLong(minWait, maxWait);
        } else {
            return now+minWait;
        }
    }

    private long[] getRange(String path) {
        long[] result = new long[] {0,0};

        if(path!=null) {
            if(path.startsWith("/")) {
                path=path.substring(1);
            }
            String[] range = path.split("-", 2);
            if(range.length == 2 && range[1].length() > 0) {
                result[1] = getNumericValue(range[1]);
            }

            if(range[0].length() > 0) {
                result[0] = getNumericValue(range[0]);
                if(range.length==1 || range[1].length()==0) {
                    result[1] = result[0];
                }
            }
        }
        return result;
    }

    private long getNumericValue(String value) {
        try {
            return Long.parseLong(value, 10);
        } catch(NumberFormatException ex) {
            return 0L;
        }
    }

    public long getMinWait() {
        return minWait;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public long getExpires() {
        return expires;
    }

    @Override
    public int compareTo(Request o) {
        if (this.expires < o.expires) {
            return -1;
        } else if(this.expires==o.expires) {
            return 0;
        }
        return 1;
    }

    public boolean isExpired(long now) {
        return now >= expires;
    }

    public boolean isPost() {
        return post;
    }

    public AsyncContext getContext() {
        return context;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
