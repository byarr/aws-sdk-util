package com.brianyarr.jaws.codegen;

import com.amazonaws.services.lambda.AWSLambda;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ServiceGenerator {

    private final ClassGenerator classGenerator;
    private final Class<?> serviceInterface;

    private static final String[] MARKER_NAMES = {"Marker", "Token", "Position"};

    public ServiceGenerator(final ClassGenerator classGenerator, final Class<?> serviceInterface, final String packageName) {
        this.serviceInterface = serviceInterface;
        final String name = Util.getAwsModuleName(serviceInterface);
        this.classGenerator = classGenerator;
        classGenerator.createClass(name, packageName, serviceInterface);
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

        final Method zeroArgMethod = getZeroArgVersion(method);

        classGenerator.addMethod(method.getName(),responseType, requestType, tokenMethod, setTokenMethod, resultCollectionMethod, zeroArgMethod != null);
    }

    private Method getZeroArgVersion(final Method method) {
        try {
            return serviceInterface.getMethod(method.getName());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public void build() throws IOException {
        classGenerator.build();
    }

    private Method getTokenMethod(final Class<?> responseType) {
        final List<Method> candidates = Arrays.stream(responseType.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("get")) //should be called getX
                .filter(m -> m.getReturnType().isAssignableFrom(String.class)) // should return a string (not always actually)
                .filter(ServiceGenerator::isMarkerMethod)
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
                .filter(ServiceGenerator::isMarkerMethod)
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

    public void tryAddAllMethods() {
        Arrays.stream(serviceInterface.getDeclaredMethods()).forEach(method -> {
            try {
                addMethod(method);
            }
            catch (IllegalStateException ex) {
                System.out.println("Failed to add method '" + method.getName() + "', " + ex.getMessage());
            }
        });
    }

    private static boolean isMarkerMethod(final Method method) {
        return Arrays.stream(MARKER_NAMES).anyMatch(marker -> method.getName().contains(marker));
    }

    public static void main(String[] args) throws IOException {
        final ServiceGenerator serviceGenerator = new ServiceGenerator(new JavaPoetClassGenerator(null), AWSLambda.class, "com.brianyarr.aws");
        serviceGenerator.tryAddAllMethods();
        serviceGenerator.build();
    }

}

