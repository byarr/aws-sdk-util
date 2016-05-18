package com.brianyarr.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.ListFunctionsRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;

import java.util.stream.Stream;

public class LambdaUtil {

    private final AWSLambda lambda;

    public LambdaUtil(final AWSLambda lambda) {
        this.lambda = lambda;
    }

    public Stream<ListFunctionsResult> listFunctions() {
        final ListFunctionsRequest request = new ListFunctionsRequest();
        return RequestUtil.getStream(lambda::listFunctions, request, ListFunctionsRequest::withMarker, ListFunctionsResult::getNextMarker);
    }
}
