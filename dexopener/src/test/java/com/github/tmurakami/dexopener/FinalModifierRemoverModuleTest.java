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

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.AccessFlags;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.AnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.IntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;

import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

public class FinalModifierRemoverModuleTest {

    private static final int ACCESS_FLAGS_FINAL = org.jf.dexlib2.AccessFlags.FINAL.getValue();

    @Test
    public void classDefRewriter_should_remove_final_modifier_from_the_given_class()
            throws IOException {
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      ACCESS_FLAGS_FINAL,
                                                      "Ljava/lang/Object;",
                                                      null,
                                                      null,
                                                      Collections.<ImmutableAnnotation>emptySet(),
                                                      Collections.<ImmutableField>emptySet(),
                                                      Collections.<ImmutableMethod>emptySet());
        byte[] bytecode = DexPoolUtils.toBytecode(new ImmutableDexFile(org.jf.dexlib2.Opcodes.getDefault(),
                                                                       Collections.singleton(def)));
        DexRewriter rewriter = new DexRewriter(new FinalModifierRemoverModule());
        ClassDef out = rewriter.getClassDefRewriter()
                               .rewrite(new DexBackedDexFile(null, bytecode)
                                                .getClasses()
                                                .iterator()
                                                .next());
        assertFalse(AccessFlags.FINAL.isSet(out.getAccessFlags()));
    }

    @Test
    public void methodRewriter_should_remove_final_modifier_from_the_given_method()
            throws IOException {
        ImmutableMethod method = new ImmutableMethod("Lfoo/Bar;",
                                                     "f",
                                                     Collections.<ImmutableMethodParameter>emptySet(),
                                                     "V",
                                                     ACCESS_FLAGS_FINAL,
                                                     Collections.<ImmutableAnnotation>emptySet(),
                                                     null);
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      0,
                                                      "Ljava/lang/Object;",
                                                      null,
                                                      null,
                                                      Collections.<ImmutableAnnotation>emptySet(),
                                                      Collections.<ImmutableField>emptySet(),
                                                      Collections.singleton(method));
        byte[] bytecode = DexPoolUtils.toBytecode(new ImmutableDexFile(org.jf.dexlib2.Opcodes.getDefault(),
                                                                       Collections.singleton(def)));
        DexRewriter rewriter = new DexRewriter(new FinalModifierRemoverModule());
        Method out = rewriter.getMethodRewriter()
                             .rewrite(new DexBackedDexFile(null, bytecode)
                                              .getClasses()
                                              .iterator()
                                              .next()
                                              .getVirtualMethods()
                                              .iterator()
                                              .next());
        assertFalse(AccessFlags.FINAL.isSet(out.getAccessFlags()));
    }

    @Test
    public void annotationRewriter_should_remove_final_modifier_from_the_given_inner_class_annotation()
            throws IOException {
        Set<ImmutableAnnotationElement> elements = new HashSet<>();
        elements.add(new ImmutableAnnotationElement("name", new ImmutableStringEncodedValue("Lfoo/Bar;")));
        elements.add(new ImmutableAnnotationElement("accessFlags", new ImmutableIntEncodedValue(ACCESS_FLAGS_FINAL)));
        ImmutableAnnotation annotation = new ImmutableAnnotation(0, "Ldalvik/annotation/InnerClass;", elements);
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      0,
                                                      "Ljava/lang/Object;",
                                                      null,
                                                      null,
                                                      Collections.singleton(annotation),
                                                      Collections.<ImmutableField>emptySet(),
                                                      Collections.<ImmutableMethod>emptySet());
        byte[] bytecode = DexPoolUtils.toBytecode(new ImmutableDexFile(org.jf.dexlib2.Opcodes.getDefault(),
                                                                       Collections.singleton(def)));
        DexRewriter rewriter = new DexRewriter(new FinalModifierRemoverModule());
        Annotation out = rewriter.getAnnotationRewriter()
                                 .rewrite(new DexBackedDexFile(null, bytecode)
                                                  .getClasses()
                                                  .iterator()
                                                  .next()
                                                  .getAnnotations()
                                                  .iterator()
                                                  .next());
        int accessFlags = -1;
        for (AnnotationElement e : out.getElements()) {
            if (e.getName().equals("accessFlags")) {
                accessFlags = ((IntEncodedValue) e.getValue()).getValue();
                break;
            }
        }
        assertNotSame(-1, accessFlags);
        assertFalse(AccessFlags.FINAL.isSet(accessFlags));
    }

}
