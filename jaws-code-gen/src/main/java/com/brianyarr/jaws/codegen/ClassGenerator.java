package com.brianyarr.jaws.codegen;

import java.io.IOException;
import java.lang.reflect.Method;

public interface ClassGenerator {
    void createClass(String name, String packageName, Class<?> serviceInterface);

    void addMethod(String name, Class<?> responseType, Class<?> requestType, Method tokenMethod, Method setTokenMethod, Method resultCollectionMethod, final boolean createZeroArgVersion);

    void build() throws IOException;
}
