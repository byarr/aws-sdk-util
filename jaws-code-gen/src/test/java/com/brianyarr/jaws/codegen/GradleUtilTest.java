package com.brianyarr.jaws.codegen;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class GradleUtilTest {

    @Test
    public void shouldDoNothingIfNotPresent() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradleUtil.removeModuleFromSettings(contents, "module4");

        assertThat(result, is(equalTo(contents)));
    }

    @Test
    public void shouldRemoveFromStartOfImports() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradleUtil.removeModuleFromSettings(contents, "module1");

        assertThat(result, is(equalTo("include 'module2', 'module3'")));
    }

    @Test
    public void shouldRemoveFromMiddleOfImports() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradleUtil.removeModuleFromSettings(contents, "module2");

        assertThat(result, is(equalTo("include 'module1', 'module3'")));
    }

    @Test
    public void shouldRemoveFromEndOfImports() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradleUtil.removeModuleFromSettings(contents, "module3");

        assertThat(result, is(equalTo("include 'module1', 'module2'")));
    }


}