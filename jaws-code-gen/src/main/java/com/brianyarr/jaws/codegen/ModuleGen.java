package com.brianyarr.jaws.codegen;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.sns.AmazonSNS;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ModuleGen {

    private final File rootDir;

    public ModuleGen(final File rootDir) {

        this.rootDir = rootDir;
    }

    public void generateModule(final Class<?> serviceInterface) throws IOException {

        final String awsModuleName = Util.getAwsModuleName(serviceInterface).toLowerCase();

        final String moduleName = "jaws-" + awsModuleName;
        final File moduleDir = new File(rootDir, moduleName);
        if (!moduleDir.exists()) {
            moduleDir.mkdir();
        }

        writeGradleFile(moduleDir, awsModuleName);
        updateGradleSettings(moduleName);

        final File genSrcDir = new File(moduleDir, "src/gen/java");
        if (genSrcDir.exists()) {
            FileUtils.cleanDirectory(genSrcDir);
        } else {
            genSrcDir.mkdirs();
        }

        final ServiceGenerator serviceGenerator = new ServiceGenerator(new JavaPoetClassGenerator(genSrcDir), serviceInterface, "com.brianyarr.jaws." + awsModuleName);
        serviceGenerator.tryAddAllMethods();
        serviceGenerator.build();

    }

    public void cleanModule(final Class<?> serviceInterface) throws IOException {
        final String awsModuleName = Util.getAwsModuleName(serviceInterface).toLowerCase();

        final String moduleName = "jaws-" + awsModuleName;
        final File moduleDir = new File(rootDir, moduleName);
        if (moduleDir.exists()) {
            FileUtils.cleanDirectory(moduleDir);
        }

    }

    private void updateGradleSettings(final String moduleName) throws IOException {
        final File file = new File(rootDir, "settings.gradle");
        if (Files.lines(file.toPath()).anyMatch(l -> l.contains(moduleName))) {
            // already has module name
        } else {
            Files.write(file.toPath(), (", '" + moduleName + "'").getBytes(), APPEND);
        }

    }

    private void writeGradleFile(final File moduleDir, final String awsModuleName) throws IOException {
        final File file = new File(moduleDir, "build.gradle");
        if (!file.exists()) {
            Files.write(file.toPath(), getGradleFile(awsModuleName).getBytes(), CREATE_NEW);
        }
    }

    private String getGradleFile(final String awsModuleName) {
        return String.format("sourceSets {\n" +
                "    gen {\n" +
                "        java {\n" +
                "            srcDir '${build}/src/gen/java'\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "dependencies {\n" +
                "    compile project(':jaws-core')\n" +
                "    compile 'com.amazonaws:aws-java-sdk-%s'\n" +
                "    genCompile project(':jaws-core')\n" +
                "    genCompile 'com.amazonaws:aws-java-sdk-%s'\n" +
                "}", awsModuleName, awsModuleName);
    }

    public static void main(String[] args) throws IOException {
        final ModuleGen moduleGen = new ModuleGen(new File("/Users/brian.yarr/code/jaws/"));
        moduleGen.generateModule(AWSLambda.class);
        moduleGen.generateModule(AmazonSNS.class);

//        moduleGen.cleanModule(AWSLambda.class);
//        moduleGen.cleanModule(AmazonSNS.class);
    }

}
