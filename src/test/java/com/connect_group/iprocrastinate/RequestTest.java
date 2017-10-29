package com.connect_group.iprocrastinate;

import org.junit.Test;

import java.util.PriorityQueue;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class RequestTest {

    @Test
    public void shouldSetMin0WhenRangeNotSpecified() {
        Request request = new Request("", null, null);
        assertThat(request.getMinWait(), equalTo(0L));
    }

    @Test
    public void shouldSetMax0WhenRangeNotSpecified() {
        Request request = new Request("/", null, null);
        assertThat(request.getMaxWait(), equalTo(0L));
    }

    @Test
    public void shouldSetMinEqualToMaxWhenOneNumberSpecified() {
        Request request = new Request("/100", null, null);
        assertThat(request.getMinWait(), equalTo(100L));
        assertThat(request.getMaxWait(), equalTo(100L));
    }

    @Test
    public void shouldSetMaxEqualToMinWhenNoMaxSpecified() {
        Request request = new Request("/150-", null, null);
        assertThat(request.getMinWait(), equalTo(150L));
        assertThat(request.getMaxWait(), equalTo(150L));
    }

    @Test
    public void shouldSetMinEqualToZeroWhenMaxSpecified() {
        Request request = new Request("/-200", null, null);
        assertThat(request.getMinWait(), equalTo(0L));
        assertThat(request.getMaxWait(), equalTo(200L));
    }

    @Test
    public void shouldSetMinAndMaxWhenRangeSpecified() {
        Request request = new Request("/100-200", null, null);
        assertThat(request.getMinWait(), equalTo(100L));
        assertThat(request.getMaxWait(), equalTo(200L));
    }

    @Test
    public void shouldTreatNonNumericValuesAsZero() {
        Request request = new Request("/1a-200", null, null);
        assertThat(request.getMinWait(), equalTo(0L));
        assertThat(request.getMaxWait(), equalTo(200L));
    }

    @Test
    public void shouldIdentifyMinimumAndMaximum_WhenMaximumSpecifiedBeforeMinimum() {
        Request request = new Request("/200-100", null, null);
        assertThat(request.getMinWait(), equalTo(100L));
        assertThat(request.getMaxWait(), equalTo(200L));
    }

    @Test
    public void shouldReturnAWaitValueBetweenMinAndMax_WhenMinNotEqualToMax() {
        Request request = new Request("/100-200", null, null);
        long now = System.currentTimeMillis();
        assertThat(request.getExpires(), lessThan(now+200L));
        assertThat(request.getExpires(), greaterThanOrEqualTo(now+100L));
    }

    @Test
    public void shouldNotIndicateExpired_WhenNotExpired() {
        Request request = new Request("10000", null, null);
        assertThat(request.isExpired(System.currentTimeMillis()), equalTo(false));
    }

    @Test
    public void shouldSortRequestsSuchThatThoseExpiringSoonestAreFirstInQueue() {
        Request early = new Request("100", null, null);
        Request late = new Request("10000", null, null);
        PriorityQueue<Request> queue = new PriorityQueue<>(2);
        queue.add(early);
        queue.add(late);
        assertThat(queue.peek().getMaxWait(), equalTo(100L));
    }
}