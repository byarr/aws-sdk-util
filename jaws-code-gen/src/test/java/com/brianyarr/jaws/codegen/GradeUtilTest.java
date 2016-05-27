package com.brianyarr.jaws.codegen;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class GradeUtilTest {

    @Test
    public void shouldDoNothingIfNotPresent() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradeUtil.removeModuleFromSettings(contents, "module4");

        assertThat(result, is(equalTo(contents)));
    }

    @Test
    public void shouldRemoveFromStartOfImports() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradeUtil.removeModuleFromSettings(contents, "module1");

        assertThat(result, is(equalTo("include 'module2', 'module3'")));
    }

    @Test
    public void shouldRemoveFromMiddleOfImports() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradeUtil.removeModuleFromSettings(contents, "module2");

        assertThat(result, is(equalTo("include 'module1', 'module3'")));
    }

    @Test
    public void shouldRemoveFromEndOfImports() {
        final String contents = "include 'module1', 'module2', 'module3'";

        final String result = GradeUtil.removeModuleFromSettings(contents, "module3");

        assertThat(result, is(equalTo("include 'module1', 'module2'")));
    }


}