package com.brianyarr.jaws.codegen;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.GetResourcesRequest;
import com.amazonaws.services.apigateway.model.GetResourcesResult;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.ListAliasesRequest;
import com.amazonaws.services.lambda.model.ListAliasesResult;
import com.amazonaws.services.lambda.model.ListFunctionsRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

public class ServiceGeneratorTest {

    @Test
    public void shouldWork() throws NoSuchMethodException {
        final Class<AWSLambda> serviceInterface = AWSLambda.class;
        final Method testMethod = serviceInterface.getMethod("listAliases", ListAliasesRequest.class);

        final Method getTokenMethod = ListAliasesResult.class.getMethod("getNextMarker");
        final Method withTokenMethod = ListAliasesRequest.class.getMethod("withMarker", String.class);
        final Method resultCollectionMethod = ListAliasesResult.class.getMethod("getAliases");

        final ClassGenerator classGenerator = mock(ClassGenerator.class);
        final ServiceGenerator serviceGenerator = new ServiceGenerator(classGenerator, serviceInterface, "test");
        serviceGenerator.addMethod(testMethod);

        verify(classGenerator).createClass("Lambda", "test", serviceInterface);
        verify(classGenerator).addMethod("listAliases", ListAliasesResult.class, ListAliasesRequest.class, getTokenMethod, withTokenMethod, resultCollectionMethod, false);
    }

    @Test
    public void shouldWorkForApiGateway() throws NoSuchMethodException {
        final Class<AmazonApiGateway> serviceInterface = AmazonApiGateway.class;
        final Method testMethod = serviceInterface.getMethod("getResources", GetResourcesRequest.class);

        final Method getTokenMethod = GetResourcesResult.class.getMethod("getPosition");
        final Method withTokenMethod = GetResourcesRequest.class.getMethod("withPosition", String.class);
        final Method resultCollectionMethod = GetResourcesResult.class.getMethod("getItems");

        final ClassGenerator classGenerator = mock(ClassGenerator.class);
        final ServiceGenerator serviceGenerator = new ServiceGenerator(classGenerator, serviceInterface, "test");
        serviceGenerator.addMethod(testMethod);

        verify(classGenerator).createClass("ApiGateway", "test", serviceInterface);
        verify(classGenerator).addMethod("getResources", GetResourcesResult.class, GetResourcesRequest.class, getTokenMethod, withTokenMethod, resultCollectionMethod, false);
    }

    @Test
    public void shouldGenerateZeroArgVersions() throws NoSuchMethodException {
        final Class<AWSLambda> serviceInterface = AWSLambda.class;
        final Method testMethod = serviceInterface.getMethod("listFunctions", ListFunctionsRequest.class);

        final Method getTokenMethod = ListFunctionsResult.class.getMethod("getNextMarker");
        final Method withTokenMethod = ListFunctionsRequest.class.getMethod("withMarker", String.class);
        final Method resultCollectionMethod = ListFunctionsResult.class.getMethod("getFunctions");

        final ClassGenerator classGenerator = mock(ClassGenerator.class);
        final ServiceGenerator serviceGenerator = new ServiceGenerator(classGenerator, serviceInterface, "test");
        serviceGenerator.addMethod(testMethod);

        verify(classGenerator).createClass("Lambda", "test", serviceInterface);
        verify(classGenerator).addMethod("listFunctions", ListFunctionsResult.class, ListFunctionsRequest.class, getTokenMethod, withTokenMethod, resultCollectionMethod, true);
    }

}