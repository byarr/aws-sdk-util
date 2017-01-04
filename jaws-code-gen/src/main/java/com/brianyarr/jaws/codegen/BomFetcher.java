package com.brianyarr.jaws.codegen;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BomFetcher {

    public static final String AWS_GROUP_ID = "com.amazonaws";
    public static final String AWS_JAVA_SDK = "aws-java-sdk";

    private static URL url(final String version) throws MalformedURLException {
        return new URL(String.format("http://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bom/%1$s/aws-java-sdk-bom-%1$s.pom", version));
    }

    public static List<String> getAllModules(final String version) throws IOException, ParserConfigurationException, SAXException {
        final List<String> result = new ArrayList<>();

        final URL pom = url(version);
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final Document document = dbf.newDocumentBuilder().parse(pom.openStream());
        final NodeList deps = document.getElementsByTagName("dependency");
        for (int i = 0; i < deps.getLength(); i++) {
            final Element element = (Element) deps.item(i);
            final String groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
            if (!groupId.equals(AWS_GROUP_ID)) {
                continue;
            }

            final String artifactId = element.getElementsByTagName("artifactId").item(0).getTextContent();
            if (!artifactId.startsWith(AWS_JAVA_SDK)) {
                continue;
            }
            result.add(artifactId);

        }
        return result;
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        final List<String> modules = getAllModules("1.11.0");
        System.out.println(modules);
    }

}
