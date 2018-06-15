/*
 * Copyright 2016 Tsuyoshi Murakami
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.AnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Field;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.MethodParameter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.IntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableMethod;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;

import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FinalModifierRemoverModuleTest {

    @Test
    public void classDefRewriter_should_remove_final_modifier_from_the_given_class() {
        ClassDef in = new ImmutableClassDef("Lfoo/Bar;",
                                            Modifier.FINAL,
                                            "Ljava/lang/Object;",
                                            null,
                                            null,
                                            Collections.<Annotation>emptySet(),
                                            Collections.<Field>emptySet(),
                                            Collections.<Method>emptySet());
        DexRewriter rewriter = new DexRewriter(new FinalModifierRemoverModule());
        ClassDef out = rewriter.getClassDefRewriter().rewrite(in);
        assertEquals(0, out.getAccessFlags());
    }

    @Test
    public void methodRewriter_should_remove_final_modifier_from_the_given_method() {
        Method in = new ImmutableMethod("Lfoo/Bar;",
                                        "f",
                                        Collections.<MethodParameter>emptySet(),
                                        "V",
                                        Modifier.FINAL,
                                        Collections.<Annotation>emptySet(),
                                        null);
        DexRewriter rewriter = new DexRewriter(new FinalModifierRemoverModule());
        Method out = rewriter.getMethodRewriter().rewrite(in);
        assertEquals(0, out.getAccessFlags());
    }

    @Test
    public void annotationRewriter_should_remove_final_modifier_from_the_given_inner_class_annotation() {
        Set<AnnotationElement> elements = new HashSet<>();
        elements.add(new ImmutableAnnotationElement("name", new ImmutableStringEncodedValue("Lfoo/Bar;")));
        elements.add(new ImmutableAnnotationElement("accessFlags", new ImmutableIntEncodedValue(Modifier.FINAL)));
        Annotation in = new ImmutableAnnotation(0, "Ldalvik/annotation/InnerClass;", elements);
        DexRewriter rewriter = new DexRewriter(new FinalModifierRemoverModule());
        Annotation out = rewriter.getAnnotationRewriter().rewrite(in);
        int accessFlags = -1;
        for (AnnotationElement e : out.getElements()) {
            if (e.getName().equals("accessFlags")) {
                accessFlags = ((IntEncodedValue) e.getValue()).getValue();
            }
        }
        assertEquals(0, accessFlags);
    }

}
