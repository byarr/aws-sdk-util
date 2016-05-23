package com.brianyarr.awssdkutil.codegen;

import java.io.IOException;
import java.lang.reflect.Method;

public interface ClassGenerator {
    void createClass(String name, Class<?> serviceInterface);

    void addMethod(String name, Class<?> responseType, Class<?> requestType, Method tokenMethod, Method setTokenMethod, Method resultCollectionMethod);

    void build() throws IOException;
}
