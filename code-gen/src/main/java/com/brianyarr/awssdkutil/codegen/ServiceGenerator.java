package com.brianyarr.awssdkutil.codegen;

import com.amazonaws.services.lambda.AWSLambda;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ServiceGenerator {

    private final ClassGenerator classGenerator;

    public ServiceGenerator(final ClassGenerator classGenerator, final Class<?> serviceInterface) {
        final String name = generateName(serviceInterface);
        this.classGenerator = classGenerator;
        classGenerator.createClass(name, serviceInterface);
    }

    private static String generateName(final Class<?> serviceInterface) {
        final String name;
        if (serviceInterface.getSimpleName().startsWith("AWS")) {
            name = serviceInterface.getSimpleName().substring(3);
        }
        else {
            name = serviceInterface.getSimpleName() + "Util";
        }
        return name;
    }

    public void addMethod(final Method method) {
        final Class<?> responseType = method.getReturnType();

        if (method.getParameterCount() != 1) {
            throw new IllegalStateException("No request param");
        }
        final Class<?> requestType = method.getParameterTypes()[0];

        final Method tokenMethod = getTokenMethod(responseType);
        final Method setTokenMethod = getSetTokenMethod(requestType);

        final Method resultCollectionMethod = getResultCollectionMethod(responseType);

        classGenerator.addMethod(method.getName(),responseType, requestType, tokenMethod, setTokenMethod, resultCollectionMethod);
    }

    public void build() throws IOException {
        classGenerator.build();
    }

    private Method getTokenMethod(final Class<?> responseType) {
        final List<Method> candidates = Arrays.stream(responseType.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("get")) //should be called getX
                .filter(m -> m.getReturnType().isAssignableFrom(String.class)) // should return a string (not always actually)
                .filter(m -> m.getName().contains("Marker") || m.getName().contains("Token"))
                .collect(toList());
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        else {
            throw new IllegalStateException("Failed to find a get token method on " + responseType.getCanonicalName());
        }
    }

    private Method getSetTokenMethod(final Class<?> requestType) {
        final List<Method> candidates = Arrays.stream(requestType.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("with")) //should be called getX
                .filter(m -> m.getReturnType().isAssignableFrom(requestType)) // should check param)
                .filter(m -> m.getName().contains("Marker") || m.getName().contains("Token"))
                .collect(toList());
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        else {
            throw new IllegalStateException("Failed to find a set token method on " + requestType.getCanonicalName());
        }
    }

    private Method getResultCollectionMethod(final Class<?> responseType) {
        final List<Method> methods = Arrays.stream(responseType.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("get"))
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> Collection.class.isAssignableFrom(m.getReturnType()))
                .collect(toList());
        if (methods.size() == 1) {
            return methods.get(0);
        }
        else {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        final ServiceGenerator serviceGenerator = new ServiceGenerator(new JavaPoetClassGenerator(), AWSLambda.class);

        Arrays.stream(AWSLambda.class.getDeclaredMethods()).forEach(method -> {
            try {
                serviceGenerator.addMethod(method);
            }
            catch (IllegalStateException ex) {
                System.out.println("Failed to add method '" + method.getName() + "', " + ex.getMessage());
            }
        });

        serviceGenerator.build();
    }

}

