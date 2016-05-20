package com.brianyarr.awssdkutil.codegen;

import com.amazonaws.services.lambda.AWSLambda;
import com.brianyarr.aws.RequestUtil;
import com.squareup.javapoet.*;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


import static java.util.stream.Collectors.toList;

public class ServiceGenerator {
    private final TypeSpec.Builder classBuilder;
    private final FieldSpec delegate;

    private static final Class<RequestUtil> REQUEST_UTIL_CLASS = RequestUtil.class;

    public ServiceGenerator(final Class<?> serviceInterface) {
        final String name;
        if (serviceInterface.getSimpleName().startsWith("AWS")) {
            name = serviceInterface.getSimpleName().substring(3);
        }
        else {
            name = serviceInterface.getSimpleName() + "Util";
        }

        classBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
        delegate = FieldSpec.builder(serviceInterface, "delegate", Modifier.PRIVATE, Modifier.FINAL).build();
        classBuilder.addField(delegate);

        addConstructor(serviceInterface);
    }

    private void addConstructor(final Class<?> serviceInterface) {
        MethodSpec flux = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(serviceInterface, "delegate")
                .addStatement("this.$N = $N", "delegate", "delegate")
                .build();
        classBuilder.addMethod(flux);
    }

    public void addMethod(final Method method) {
        final Class<?> responseType = method.getReturnType();
        final Class<?> requestType = method.getParameterTypes()[0];

        final Method tokenMethod = getTokenMethod(responseType);
        final Method setTokenMethod = getSetTokenMethod(requestType);


        final ParameterizedTypeName returnType = ParameterizedTypeName.get(Stream.class, responseType);
        final ParameterSpec request = ParameterSpec.builder(requestType, "request", Modifier.FINAL).build();

        final MethodSpec methodSpec = MethodSpec.methodBuilder(method.getName())
                .returns(returnType)
                .addParameter(request)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.getStream($N::$N, $N, $T::$N, $T::$N)",
                        REQUEST_UTIL_CLASS,
                        delegate, method.getName(),
                        request,
                        requestType, setTokenMethod.getName(),
                        responseType, tokenMethod.getName())
                .build();

        classBuilder.addMethod(methodSpec);
    }

    public void build() throws IOException {
        JavaFile javaFile = JavaFile.builder("com.brianyarr.aws", classBuilder.build())
                .build();

        javaFile.writeTo(System.out);
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
            throw new IllegalStateException("blah");
        }
    }

    public static void main(String[] args) throws IOException {
        final ServiceGenerator serviceGenerator = new ServiceGenerator(AWSLambda.class);

        final Optional<Method> listFunctions = Arrays.stream(AWSLambda.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("listFunctions"))
                .filter(m -> m.getParameterCount() == 1)
                .findFirst();

        listFunctions.ifPresent(serviceGenerator::addMethod);

        serviceGenerator.build();
    }

}

