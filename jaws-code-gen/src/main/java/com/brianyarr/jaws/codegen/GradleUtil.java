package com.brianyarr.jaws.codegen;

import java.util.Optional;

public class GradleUtil {

    public static String removeModuleFromSettings(final String settingsFileContents, final String moduleName) {
        String result = settingsFileContents.replaceAll("'" + moduleName + "',?\\s*", "");
        result = result.replaceAll(",\\s*,", ",");
        result = result.replaceAll(",\\s*$", "");
        return result;
    }

    public static Optional<String> ensureModuleInSettings(final String settingsFileContents, final String moduleName) {
        if (!settingsFileContents.contains(moduleName)) {
            return Optional.of(settingsFileContents + ", '" + moduleName + "'");
        }
        else {
            return Optional.empty();
        }
    }

    public static String getGradleFile(final String awsModuleName) {
        return String.format("sourceSets {%n" +
                "    gen {%n" +
                "        java {%n" +
                "            srcDir '${build}/src/gen/java'%n" +
                "        }%n" +
                "    }%n" +
                "}%n" +
                "%n" +
                "dependencies {%n" +
                "    compile project(':jaws-core')%n" +
                "    compile 'com.amazonaws:aws-java-sdk-%s'%n" +
                "    genCompile project(':jaws-core')%n" +
                "    genCompile 'com.amazonaws:aws-java-sdk-%s'%n" +
                "}", awsModuleName, awsModuleName);
    }
}
