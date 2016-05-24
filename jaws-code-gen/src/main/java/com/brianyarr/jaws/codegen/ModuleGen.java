package com.brianyarr.jaws.codegen;

import com.amazonaws.services.lambda.AWSLambda;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardOpenOption.APPEND;

public class ModuleGen {

    private final File rootDir;

    public ModuleGen(final File rootDir) {

        this.rootDir = rootDir;
    }

    public void generateModule(final Class<?> serviceInterface) throws IOException {

        final String awsModuleName = getAwsModuleName(serviceInterface);

        final String moduleName = "jaws-" + awsModuleName;
        final File moduleDir = new File(rootDir, moduleName);
        if (!moduleDir.exists()) {
            moduleDir.mkdir();
            writeGradleFile(moduleDir, awsModuleName);
            updateGradleSettings(moduleName);

            final File genSrcDir = new File(moduleDir, "src/gen/java");
            genSrcDir.mkdirs();

            final ServiceGenerator serviceGenerator = new ServiceGenerator(new JavaPoetClassGenerator(), serviceInterface);
            serviceGenerator.tryAddAllMethods();
            serviceGenerator.build();
        }
    }

    private static String getAwsModuleName(final Class<?> serviceInterface) {
        if (serviceInterface.getSimpleName().startsWith("AWS")) {
            return serviceInterface.getSimpleName().substring(3).toLowerCase();
        }
        return null;
    }

    private void updateGradleSettings(final String moduleName) throws IOException {
        final File file = new File(rootDir, "settings.gradle");
        Files.write(file.toPath(), (", '" + moduleName + "'").getBytes(), APPEND);
    }

    private void writeGradleFile(final File moduleDir, final String awsModuleName) throws IOException {
        final File file = new File(moduleDir, "build.gradle");
        file.createNewFile();
        Files.write(file.toPath(), getGradleFile(awsModuleName).getBytes(), APPEND);
    }

    private String getGradleFile(final String awsModuleName) {
        return "dependencies {\n" +
                "    compile 'com.amazonaws:aws-java-sdk-" + awsModuleName + "'\n" +
                "}";
    }

    public static void main(String[] args) throws IOException {
        final ModuleGen moduleGen = new ModuleGen(new File("/Users/brian.yarr/code/jaws/"));
        moduleGen.generateModule(AWSLambda.class);
    }

}
