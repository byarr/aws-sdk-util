package com.brianyarr.aws;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RequestUtil {

    private RequestUtil() {

    }

    private static <Request, Response> Stream<Response> getStream(final Function<Request, Response> service, final Request initialRequest, final BiFunction<Request, Response, Request> nextRequestGenerator) {
        final ResponseIterator<Request, Response> respIter = new ResponseIterator<>(service, initialRequest, nextRequestGenerator);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(respIter, Spliterator.ORDERED), false);
    }

    public static <Request, Response, Token> Stream<Response> getStream(final Function<Request, Response> service, final Request req, final BiFunction<Request, Token, Request> tokenApplier, final Function<Response, Token> tokenExtractor) {

        final BiFunction<Request, Response, Request> nextReqGenerator = (request, response) -> {
            final Token token = tokenExtractor.apply(response);
            if (token == null) {
                return null;
            } else {
                return tokenApplier.apply(req, token);
            }
        };
        return getStream(service, req, nextReqGenerator);
    }

    private static class ResponseIterator<Request, Response> implements Iterator<Response> {

        private final Function<Request, Response> service;
        private final BiFunction<Request, Response, Request> nextReqGenerator;
        private Request request;

        private ResponseIterator(final Function<Request, Response> service, final Request initialRequest, final BiFunction<Request, Response, Request> nextReqGenerator) {
            this.service = service;
            this.request = initialRequest;
            this.nextReqGenerator = nextReqGenerator;
        }

        @Override
        public boolean hasNext() {
            return request != null;
        }

        @Override
        public Response next() {
            final Response response = service.apply(request);
            request = nextReqGenerator.apply(request, response);
            return response;
        }
    }
}
