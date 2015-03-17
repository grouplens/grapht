/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht.annotation;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

public class AnnotationValidatorTest {

  private static final String QUALIFIER_WHICH_IS_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION = "package org.grouplens.grapht.annotation;"
          + "import java.lang.annotation.Documented;"
          + "import java.lang.annotation.Retention;"
          + "import java.lang.annotation.RetentionPolicy;"
          + "import javax.inject.Qualifier;"
          + "@Documented "
          + "@Retention(RetentionPolicy.RUNTIME)"
          + "@Qualifier "
          + "public @interface QualifierWhichIsDocumentedAndHasRuntimeRetention {}";

  private static final String QUALIFIER_WHICH_IS_DOCUMENTEND_AND_HAS_NO_RETENTION = "package org.grouplens.grapht.annotation;"
          + "import java.lang.annotation.Documented;"
          + "import java.lang.annotation.RetentionPolicy;"
          + "import javax.inject.Qualifier;"
          + "@Documented "
          + "@Qualifier "
          + "public @interface QualifierWhichIsDocumentedAndHasNoRetention {}";

  private static final String QUALIFIER_WHICH_IS_NOT_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION = "package org.grouplens.grapht.annotation;"
          + "import java.lang.annotation.Retention;"
          + "import java.lang.annotation.RetentionPolicy;"
          + "import javax.inject.Qualifier;"
          + "@Retention(RetentionPolicy.RUNTIME)"
          + "@Qualifier "
          + "public @interface QualifierWhichIsNotDocumentedAndHasRuntimeRetention {}";

  private static final String ATTRIBUTE_WHICH_IS_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION = "package org.grouplens.grapht.annotation;"
          + "import java.lang.annotation.Documented;"
          + "import java.lang.annotation.Retention;"
          + "import java.lang.annotation.RetentionPolicy;"
          + "import org.grouplens.grapht.annotation.Attribute;"
          + "@Documented "
          + "@Retention(RetentionPolicy.RUNTIME)"
          + "@Attribute "
          + "public @interface AttributeWhichIsDocumentedAndHasRuntimeRetention {}";

  private static final String ATTRIBUTE_WHICH_IS_DOCUMENTEND_AND_HAS_NO_RETENTION = "package org.grouplens.grapht.annotation;"
          + "import java.lang.annotation.Documented;"
          + "import java.lang.annotation.RetentionPolicy;"
          + "import org.grouplens.grapht.annotation.Attribute;"
          + "@Documented "
          + "@Attribute "
          + "public @interface AttributeWhichIsDocumentedAndHasNoRetention {}";

  private static final String ATTRIBUTE_WHICH_IS_NOT_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION = "package org.grouplens.grapht.annotation;"
          + "import java.lang.annotation.Retention;"
          + "import java.lang.annotation.RetentionPolicy;"
          + "import org.grouplens.grapht.annotation.Attribute;"
          + "@Retention(RetentionPolicy.RUNTIME)"
          + "@Attribute "
          + "public @interface AttributeWhichIsNotDocumentedAndHasRuntimeRetention {}";

  private static final String ALIAS_WHICH_IS_QUALIFIED_HAS_RUNTIME_RETENTION_AND_IS_DOCUMENTED = "package org.grouplens.grapht.annotation;"
      + "import java.lang.annotation.Documented;"
      + "import java.lang.annotation.Retention;"
      + "import java.lang.annotation.RetentionPolicy;"
      + "import javax.inject.Qualifier;"
      + "import org.grouplens.grapht.annotation.AliasFor;"
      + "@Documented "
      + "@Retention(RetentionPolicy.RUNTIME) "
      + "@Qualifier "
      + "@AliasFor(Qualifier.class) "
      + "public @interface AliasWhichIsQualifiedHasRuntimeRetentionAndIsDocumented {}";

  private static final String ALIAS_WHICH_IS_QUALIFIED_HAS_RUNTIME_RETENTION_IS_DOCUMENTED_AND_HAS_ALLOW_UNQUALIFIED_MATCH = "package org.grouplens.grapht.annotation;"
      + "import java.lang.annotation.Documented;"
      + "import java.lang.annotation.Retention;"
      + "import java.lang.annotation.RetentionPolicy;"
      + "import javax.inject.Qualifier;"
      + "import org.grouplens.grapht.annotation.AliasFor;"
      + "import org.grouplens.grapht.annotation.AllowUnqualifiedMatch;"
      + "@Documented "
      + "@Retention(RetentionPolicy.RUNTIME) "
      + "@Qualifier "
      + "@AllowUnqualifiedMatch "
      + "@AliasFor(Qualifier.class) "
      + "public @interface AliasWhichIsQualifiedHasRuntimeRetentionIsDocumentedAndAllowsUnqualifiedMatch {}";

  private static final String ALIAS_WHICH_IS_NOT_QUALIFIED_HAS_RUNTIME_RETENTION_AND_IS_DOCUMENTED = "package org.grouplens.grapht.annotation;"
      + "import java.lang.annotation.Documented;"
      + "import java.lang.annotation.Retention;"
      + "import java.lang.annotation.RetentionPolicy;"
      + "import org.grouplens.grapht.annotation.AliasFor;"
      + "@Documented "
      + "@Retention(RetentionPolicy.RUNTIME) "
      + "@AliasFor(Documented.class) "
      + "public @interface AliasWhichIsNotQualifiedHasRuntimeRetentionAndIsDocumented {}";


  @Test
  public final void whenQualifierAndIsDocumentendAndRetentionIsNotPresentThenShouldNotCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.QualifierWhichIsDocumentedAndHasNoRetention", QUALIFIER_WHICH_IS_DOCUMENTEND_AND_HAS_NO_RETENTION);
    ASSERT.about(javaSource())
          .that(annotation)
          .processedWith(new AnnotationValidator())
          .failsToCompile();
  }

  @Test
  public final void whenQualifierAndIsDocumentendAndRetentionIsRuntimeThenShouldCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.QualifierWhichIsDocumentedAndHasRuntimeRetention", QUALIFIER_WHICH_IS_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION);
    ASSERT.about(javaSource())
           .that(annotation)
           .processedWith(new AnnotationValidator())
           .compilesWithoutError();
  }

  @Test
  public final void whenQualifierAndIsNotDocumentendAndRetentionIsRuntimeThenShouldCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.QualifierWhichIsNotDocumentedAndHasRuntimeRetention", QUALIFIER_WHICH_IS_NOT_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION);
    ASSERT.about(javaSource())
          .that(annotation)
          .processedWith(new AnnotationValidator())
          .compilesWithoutError();
  }

  @Test
  public final void whenAttributeAndIsDocumentendAndRetentionIsNotPresentThenShouldNotCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.AttributeWhichIsDocumentedAndHasNoRetention", ATTRIBUTE_WHICH_IS_DOCUMENTEND_AND_HAS_NO_RETENTION);
    ASSERT.about(javaSource())
          .that(annotation)
          .processedWith(new AnnotationValidator())
          .failsToCompile();
  }

  @Test
  public final void whenAttributeAndIsDocumentendAndRetentionIsRuntimeThenShouldCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.AttributeWhichIsDocumentedAndHasRuntimeRetention", ATTRIBUTE_WHICH_IS_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION);
    ASSERT.about(javaSource())
           .that(annotation)
           .processedWith(new AnnotationValidator())
           .compilesWithoutError();
  }

  @Test
  public final void whenAttributeAndIsNotDocumentendAndRetentionIsRuntimeThenShouldCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.AttributeWhichIsNotDocumentedAndHasRuntimeRetention", ATTRIBUTE_WHICH_IS_NOT_DOCUMENTEND_AND_HAS_RUNTIME_RETENTION);
    ASSERT.about(javaSource())
          .that(annotation)
          .processedWith(new AnnotationValidator())
          .compilesWithoutError();
  }

  @Test
  public final void whenAliasAndIsDocumentendAndRetentionIsRuntimeAndIsQualifiedThenShouldCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.AliasWhichIsQualifiedHasRuntimeRetentionAndIsDocumented", ALIAS_WHICH_IS_QUALIFIED_HAS_RUNTIME_RETENTION_AND_IS_DOCUMENTED);
    ASSERT.about(javaSource())
          .that(annotation)
          .processedWith(new AnnotationValidator())
          .compilesWithoutError();
  }

  @Test
  public final void whenAliasAndIsDocumentendAndRetentionIsRuntimeAndIsQualifiedAndAllowsUnqualifiedMatchesThenShouldNotCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.AliasWhichIsQualifiedHasRuntimeRetentionIsDocumentedAndAllowsUnqualifiedMatch", ALIAS_WHICH_IS_QUALIFIED_HAS_RUNTIME_RETENTION_IS_DOCUMENTED_AND_HAS_ALLOW_UNQUALIFIED_MATCH);
    ASSERT.about(javaSource())
          .that(annotation)
          .processedWith(new AnnotationValidator())
          .failsToCompile();
  }

  @Test
  public final void whenAliasAndIsDocumentendAndRetentionIsRuntimeAndIsNotQualifiedThenShouldNotCompile() {
    final JavaFileObject annotation = JavaFileObjects.forSourceString("org.grouplens.grapht.annoation.AliasWhichIsNotQualifiedHasRuntimeRetentionAndIsDocumented", ALIAS_WHICH_IS_NOT_QUALIFIED_HAS_RUNTIME_RETENTION_AND_IS_DOCUMENTED);
    ASSERT.about(javaSource())
          .that(annotation)
          .processedWith(new AnnotationValidator())
          .failsToCompile();
  }
}
