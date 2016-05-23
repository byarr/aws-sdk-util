package com.brianyarr.awssdkutil.codegen;

import com.amazonaws.services.lambda.AWSLambda;
import com.brianyarr.aws.RequestUtil;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ServiceGenerator {

    private static final Class<RequestUtil> REQUEST_UTIL_CLASS = RequestUtil.class;

    private final TypeSpec.Builder classBuilder;
    private final FieldSpec delegate;

    public ServiceGenerator(final Class<?> serviceInterface) {
        final String name = generateName(serviceInterface);
        classBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
        delegate = FieldSpec.builder(serviceInterface, "delegate", Modifier.PRIVATE, Modifier.FINAL).build();
        classBuilder.addField(delegate);

        addConstructor(serviceInterface);
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

    private void addConstructor(final Class<?> serviceInterface) {
        final MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(serviceInterface, "delegate")
                .addStatement("this.$N = $N", "delegate", "delegate")
                .build();
        classBuilder.addMethod(constructor);
    }

    public void addMethod(final Method method) {
        final Class<?> responseType = method.getReturnType();

        if (method.getParameterCount() != 1) {
            throw new IllegalStateException("No request param");
        }
        final Class<?> requestType = method.getParameterTypes()[0];
        final ParameterSpec request = ParameterSpec.builder(requestType, "request", Modifier.FINAL).build();

        final Method tokenMethod = getTokenMethod(responseType);
        final Method setTokenMethod = getSetTokenMethod(requestType);

        final Method resultCollectionMethod = getResultCollectionMethod(responseType);
        final ParameterizedTypeName returnType;
        if (resultCollectionMethod == null) {
            returnType = ParameterizedTypeName.get(Stream.class, responseType);
        } else {
            final Type type = ((ParameterizedType) resultCollectionMethod.getGenericReturnType()).getActualTypeArguments()[0];
            returnType = ParameterizedTypeName.get(Stream.class, type);
        }

        final StringBuilder methodBody = new StringBuilder("return $T.getStream($N::$N, $N, $T::$N, $T::$N)");
        final List<Object> params = new ArrayList<>(Arrays.asList(REQUEST_UTIL_CLASS,
                delegate, method.getName(),
                request,
                requestType, setTokenMethod.getName(),
                responseType, tokenMethod.getName()));

        if (resultCollectionMethod != null) {
            methodBody.append(".flatMap(r -> r.$N().stream())");
            params.add(resultCollectionMethod.getName());
        }


        final MethodSpec methodSpec = MethodSpec.methodBuilder(method.getName())
                .returns(returnType)
                .addParameter(request)
                .addModifiers(Modifier.PUBLIC)
                .addStatement(methodBody.toString(), params.toArray())
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
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        final ServiceGenerator serviceGenerator = new ServiceGenerator(AWSLambda.class);

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

