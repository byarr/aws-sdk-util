package com.brianyarr.jaws.codegen;

import java.util.Optional;

public class GradeUtil {

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
}
