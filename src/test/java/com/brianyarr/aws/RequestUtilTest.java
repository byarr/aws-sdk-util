package com.brianyarr.aws;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RequestUtilTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldMakeOneRequest() {
        final Function<DummyRequest, DummyResponse> service = mock(Function.class);

        final DummyRequest dummyRequest = new DummyRequest();
        when(service.apply(dummyRequest)).thenReturn(new DummyResponse(null));

        final Stream<DummyResponse> responseStream = RequestUtil.getStream(service, dummyRequest, DummyRequest::withMarker, DummyResponse::getNextMarker);

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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DummyResponse that = (DummyResponse) o;

            return !(nextMarker != null ? !nextMarker.equals(that.nextMarker) : that.nextMarker != null);

        }

        @Override
        public int hashCode() {
            return nextMarker != null ? nextMarker.hashCode() : 0;
        }
    }

    private static final class DummyRequest {
        private String marker;

        public DummyRequest withMarker(final String marker) {
            this.marker = marker;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DummyRequest that = (DummyRequest) o;

            return !(marker != null ? !marker.equals(that.marker) : that.marker != null);

        }

        @Override
        public int hashCode() {
            return marker != null ? marker.hashCode() : 0;
        }
    }

}