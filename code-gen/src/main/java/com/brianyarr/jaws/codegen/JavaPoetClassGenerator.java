package com.brianyarr.jaws.codegen;

import com.brianyarr.jaws.RequestUtil;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class JavaPoetClassGenerator implements ClassGenerator {

    private static final Class<RequestUtil> REQUEST_UTIL_CLASS = RequestUtil.class;

    private TypeSpec.Builder classBuilder;
    private FieldSpec delegate;

    @Override
    public void createClass(final String name, final Class<?> serviceInterface) {
        classBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
        delegate = FieldSpec.builder(serviceInterface, "delegate", Modifier.PRIVATE, Modifier.FINAL).build();
        classBuilder.addField(delegate);
        addConstructor(serviceInterface);
    }

    private void addConstructor(final Class<?> serviceInterface) {
        final MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(serviceInterface, "delegate")
                .addStatement("this.$N = $N", "delegate", "delegate")
                .build();
        classBuilder.addMethod(constructor);
    }

    @Override
    public void addMethod(final String name, final Class<?> responseType, final Class<?> requestType, final Method tokenMethod, final Method setTokenMethod, final Method resultCollectionMethod) {
        final ParameterSpec request = ParameterSpec.builder(requestType, "request", Modifier.FINAL).build();

        final StringBuilder methodBody = new StringBuilder("return $T.getStream($N::$N, $N, $T::$N, $T::$N)");
        final List<Object> params = new ArrayList<>(Arrays.asList(REQUEST_UTIL_CLASS,
                delegate, name,
                request,
                requestType, setTokenMethod.getName(),
                responseType, tokenMethod.getName()));

        final ParameterizedTypeName returnType;
        if (resultCollectionMethod == null) {
            returnType = ParameterizedTypeName.get(Stream.class, responseType);
        } else {
            final Type type = ((ParameterizedType) resultCollectionMethod.getGenericReturnType()).getActualTypeArguments()[0];
            returnType = ParameterizedTypeName.get(Stream.class, type);
        }

        if (resultCollectionMethod != null) {
            methodBody.append(".flatMap(r -> r.$N().stream())");
            params.add(resultCollectionMethod.getName());
        }

        final MethodSpec methodSpec = MethodSpec.methodBuilder(name)
                .returns(returnType)
                .addParameter(request)
                .addModifiers(Modifier.PUBLIC)
                .addStatement(methodBody.toString(), params.toArray())
                .build();

        classBuilder.addMethod(methodSpec);
    }

    @Override
    public void build() throws IOException {
        JavaFile javaFile = JavaFile.builder("com.brianyarr.aws", classBuilder.build())
                .build();

        javaFile.writeTo(System.out);
    }
}
