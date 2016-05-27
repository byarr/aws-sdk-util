package com.brianyarr.jaws.codegen;

public class GradeUtil {

    public static String removeModuleFromSettings(final String settingsFileContents, final String moduleName) {
        String result = settingsFileContents.replaceAll("'" + moduleName + "',?\\s*", "");
        result = result.replaceAll(",\\s*,", ",");
        result = result.replaceAll(",\\s*$", "");
        return result;
    }
}
