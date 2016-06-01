package com.brianyarr.jaws;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.handlers.AsyncHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CompletionStageAsyncHandler<REQUEST extends AmazonWebServiceRequest, RESULT> implements AsyncHandler<REQUEST, RESULT> {

    private final CompletableFuture<RESULT> future = new CompletableFuture<RESULT>();

    public CompletionStage<RESULT> getCompletionStage() {
        return future;
    }

    @Override
    public void onError(final Exception exception) {
        future.completeExceptionally(exception);
    }

    @Override
    public void onSuccess(final REQUEST request, final RESULT result) {
        future.complete(result);
    }
}
