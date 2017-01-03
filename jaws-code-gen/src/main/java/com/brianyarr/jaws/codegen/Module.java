package com.brianyarr.jaws.codegen;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.sns.AmazonSNS;

public class Module {

    final static Module[] MODULES = {
            of(AWSLambda.class),
            of(AmazonSNS.class),
            of(AmazonApiGateway.class, "api-gateway"),
            of(AmazonCloudWatch.class, "cloudwatchmetrics"),
            of(AmazonEC2.class),
            of(AmazonAutoScaling.class)
    };


    public final Class<?> serviceInterface;
    public final String awsModuleName;

    public Module(final Class<?> serviceInterface, final String awsModuleName) {
        this.serviceInterface = serviceInterface;
        this.awsModuleName = awsModuleName;
    }

    private static Module of(final Class<?> clazz) {
        return new Module(clazz, Util.getAwsModuleName(clazz));
    }
    private static Module of(final Class<?> clazz, final String name) {
        return new Module(clazz, name);
    }

}
