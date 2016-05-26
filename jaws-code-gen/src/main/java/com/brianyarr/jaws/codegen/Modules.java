package com.brianyarr.jaws.codegen;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.sns.AmazonSNS;

public class Modules {

    public static Module[] MODULES = {
            of(AWSLambda.class),
            of(AmazonSNS.class),
            of(AmazonApiGateway.class, "api-gateway"),
            of(AmazonCloudWatch.class, "cloudwatchmetrics"),
    };


    public static class Module {
        final Class<?> serviceInterface;
        final String awsModuleName;

        public Module(final Class<?> serviceInterface, final String awsModuleName) {
            this.serviceInterface = serviceInterface;
            this.awsModuleName = awsModuleName;
        }

    }

    private static Module of(final Class<?> clazz) {
        return new Module(clazz, Util.getAwsModuleName(clazz));
    }
    private static Module of(final Class<?> clazz, final String name) {
        return new Module(clazz, name);
    }

}
