package com.brianyarr.jaws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;

import java.util.stream.Stream;

public class LambdaUtil {

    private final AWSLambda lambda;

    public LambdaUtil(final AWSLambda lambda) {
        this.lambda = lambda;
    }

    public Stream<FunctionConfiguration> listFunctions() {
        final ListFunctionsRequest request = new ListFunctionsRequest();
        return RequestUtil.getStream(lambda::listFunctions, request, ListFunctionsRequest::withMarker, ListFunctionsResult::getNextMarker)
                .flatMap(r -> r.getFunctions().stream());
    }

    public static void main(String[] args) {
        final AWSLambdaClient lambda = new AWSLambdaClient();
        lambda.setRegion(Region.getRegion(Regions.EU_WEST_1));
        final LambdaUtil lambdaUtil = new LambdaUtil(lambda);
        lambdaUtil.listFunctions().forEach(System.out::println);
    }
}
