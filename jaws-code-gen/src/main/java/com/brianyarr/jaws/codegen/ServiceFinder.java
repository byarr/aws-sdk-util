package com.brianyarr.jaws.codegen;

import com.amazonaws.AmazonWebServiceClient;
import org.reflections.Reflections;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ServiceFinder {

    public static Set<String> getAllInterfaces() {
        Reflections reflections = new Reflections("com.amazonaws.services");
        final Set<Class<? extends AmazonWebServiceClient>> clients = reflections.getSubTypesOf(AmazonWebServiceClient.class);
        final Set<String> interfaces = clients.stream()
                .filter(c -> !c.getCanonicalName().contains("Async"))
                .flatMap(c -> Arrays.stream(c.getInterfaces()))
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
        return interfaces;

    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        final Set<String> interfaces = getAllInterfaces();
        final Set<String> packages = interfaces.stream().map(i -> {
            final String[] parts = i.split("\\.");
            return parts[parts.length - 2];
        }).collect(Collectors.toSet());


        final List<String> allModules = BomFetcher.getAllModules("1.11.0");
        final Set<String> moduleNames = allModules.stream().map(s -> {
            return s.substring("aws-java-sdk-".length());
        }).collect(Collectors.toSet());

        System.out.println(new TreeSet<>(packages));
        System.out.println(new TreeSet<>(moduleNames));

    }

}
