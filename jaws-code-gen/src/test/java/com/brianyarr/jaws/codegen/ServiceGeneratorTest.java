package com.brianyarr.jaws.codegen;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.ListAliasesRequest;
import com.amazonaws.services.lambda.model.ListAliasesResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class ServiceGeneratorTest {

    @Test
    public void shouldWork() throws NoSuchMethodException {
        final Class<AWSLambda> serviceInterface = AWSLambda.class;
        final Method testMethod = serviceInterface.getMethod("listAliases", ListAliasesRequest.class);

        final Method getTokenMethod = ListAliasesResult.class.getMethod("getNextMarker");
        final Method withTokenMethod = ListAliasesRequest.class.getMethod("withMarker", String.class);
        final Method resultCollectionMethod = ListAliasesResult.class.getMethod("getAliases");

        final ClassGenerator classGenerator = Mockito.mock(ClassGenerator.class);
        final ServiceGenerator serviceGenerator = new ServiceGenerator(classGenerator, serviceInterface, "test");
        serviceGenerator.addMethod(testMethod);

        Mockito.verify(classGenerator).createClass("Lambda", "test", serviceInterface);
        Mockito.verify(classGenerator).addMethod("listAliases", ListAliasesResult.class, ListAliasesRequest.class, getTokenMethod, withTokenMethod, resultCollectionMethod);


    }

}