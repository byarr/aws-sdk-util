package com.brianyarr.jaws;

import org.junit.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class RequestUtilTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldMakeOneRequest() {
        final Function<DummyRequest, DummyResponse> service = mock(Function.class);

        final DummyRequest dummyRequest = new DummyRequest();
        when(service.apply(dummyRequest)).thenReturn(new DummyResponse(null));

        final Stream<DummyResponse> responseStream = RequestUtil.getStream(service, dummyRequest, DummyRequest::withMarker, DummyResponse::getNextMarker);
        verifyZeroInteractions(service);

        final List<DummyResponse> responses = responseStream.collect(Collectors.toList());
        assertThat(responses.size(), is(equalTo(1)));
        verify(service).apply(dummyRequest);

        verifyNoMoreInteractions(service);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldMakeMultipleRequests() {
        final Function<DummyRequest, DummyResponse> service = mock(Function.class);

        when(service.apply(new DummyRequest())).thenReturn(new DummyResponse("t1"));
        when(service.apply(new DummyRequest("t1").withMarker("t1"))).thenReturn(new DummyResponse(null));

        final Stream<DummyResponse> responseStream = RequestUtil.getStream(service, new DummyRequest(), DummyRequest::withMarker, DummyResponse::getNextMarker);
        verifyZeroInteractions(service);

        final List<DummyResponse> responses = responseStream.collect(Collectors.toList());
        assertThat(responses.size(), is(equalTo(2)));

        final InOrder inOrder = inOrder(service);
        inOrder.verify(service).apply(new DummyRequest());
        inOrder.verify(service).apply(new DummyRequest("t1"));


        verifyNoMoreInteractions(service);
    }

    private static final class DummyResponse {

        private final String nextMarker;

        private DummyResponse(String nextMarker) {
            this.nextMarker = nextMarker;
        }

        public String getNextMarker() {
            return nextMarker;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DummyResponse that = (DummyResponse) o;

            return !(nextMarker != null ? !nextMarker.equals(that.nextMarker) : that.nextMarker != null);

        }

        @Override
        public int hashCode() {
            return nextMarker != null ? nextMarker.hashCode() : 0;
        }
    }

    private static final class DummyRequest {
        private final String marker;

        private DummyRequest() {
            this(null);
        }

        private DummyRequest(final String marker) {
            this.marker = marker;
        }

        public DummyRequest withMarker(final String marker) {
            return new DummyRequest(marker);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) { return true; }
            if (other == null || getClass() != other.getClass()) { return false; }

            DummyRequest that = (DummyRequest) other;

            return !(marker != null ? !marker.equals(that.marker) : that.marker != null);

        }

        @Override
        public int hashCode() {
            return marker != null ? marker.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "DummyRequest{" +
                    "marker='" + marker + '\'' +
                    '}';
        }
    }

}