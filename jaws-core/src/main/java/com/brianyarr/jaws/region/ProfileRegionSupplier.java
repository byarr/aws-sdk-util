package com.brianyarr.jaws.region;

import com.amazonaws.auth.profile.internal.AbstractProfilesConfigFileScanner;
import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ProfileRegionSupplier implements RegionSupplier {

    private final File configFile;
    private final String profileName;

    public ProfileRegionSupplier() {
        configFile = new File(new File(System.getProperty("user.home")), ".aws/config");
        profileName = "default";
    }


    @Override
    public Regions getRegion() {
        final ProfileConfigReader configReader;
        try {
            configReader = new ProfileConfigReader(configFile);
            final Map<String, String> props = configReader.profiles.get(profileName);
            if (props == null) {
                return null;
            }
            return RegionUtil.fromName(props.get("region")).orElse(null);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        final Regions region = new ProfileRegionSupplier().getRegion();
        System.out.println(region);
    }

    private static class ProfileConfigReader extends AbstractProfilesConfigFileScanner {

        final Map<String, Map<String, String>> profiles = new HashMap<>();

        public ProfileConfigReader(final File configFile) throws FileNotFoundException {
            final Scanner scanner = new Scanner(new FileInputStream(configFile), Charset.defaultCharset().name());
            run(scanner);
        }

        @Override
        protected void onEmptyOrCommentLine(final String profileName, final String line) {

        }

        @Override
        protected void onProfileStartingLine(final String newProfileName, final String line) {

        }

        @Override
        protected void onProfileEndingLine(final String prevProfileName) {

        }

        @Override
        protected void onEndOfFile() {

        }

        private static String trimProfileName(final String newProfileName) {
            String profileName = newProfileName.trim();
            if (profileName.startsWith("profile ")) {
                profileName = profileName.substring("profile ".length());
            }
            profileName = profileName.trim();
            return profileName;
        }

        @Override
        protected void onProfileProperty(final String profileName, final String propertyName, final String propertyValue, final boolean isSupportedProperty, final String line) {
            profiles.computeIfAbsent(trimProfileName(profileName), s -> new HashMap<>()).put(propertyName, propertyValue);
        }
    }
}
