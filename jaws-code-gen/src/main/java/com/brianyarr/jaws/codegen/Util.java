package com.brianyarr.jaws.codegen;

public class Util {

    private Util() {

    }

    public static String getAwsModuleName(final Class<?> serviceInterface) {
        final String[] prefixes = {"AWS", "Amazon"};

        for (String prefix : prefixes) {
            if (serviceInterface.getSimpleName().startsWith(prefix)) {
                return serviceInterface.getSimpleName().substring(prefix.length());
            }
        }
        throw new IllegalArgumentException("Can't autogenerate AWS module name for " + serviceInterface.getSimpleName());
    }

}
