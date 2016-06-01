package com.brianyarr.jaws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.ListAliasesRequest;
import com.amazonaws.services.lambda.model.ListAliasesResult;

import java.util.concurrent.CompletionStage;

public class AsyncLambdaUtil {

    private final AWSLambdaAsync lambdaAsync;

    public AsyncLambdaUtil(final AWSLambdaAsync lambdaAsync) {
        this.lambdaAsync = lambdaAsync;
    }

    public CompletionStage<ListAliasesResult> listAliasesAsync() {
        final CompletionStageAsyncHandler<ListAliasesRequest, ListAliasesResult> asyncHandler = new CompletionStageAsyncHandler<>();
        lambdaAsync.listAliasesAsync(new ListAliasesRequest(), asyncHandler);
        return asyncHandler.getCompletionStage();
    }

    public static void main(String[] args) {
        final AWSLambdaAsyncClient lambdaAsync = new AWSLambdaAsyncClient();
        lambdaAsync.setRegion(Region.getRegion(Regions.EU_WEST_1));
        final AsyncLambdaUtil asyncLambdaUtil = new AsyncLambdaUtil(lambdaAsync);
        asyncLambdaUtil.listAliasesAsync().handle((r, t) -> {
            System.out.println(r);
            System.out.println(t);
            return null;
        } );
    }

}
