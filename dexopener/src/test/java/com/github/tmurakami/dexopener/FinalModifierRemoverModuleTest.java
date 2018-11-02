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

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.AnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.IntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
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

    @Test
    public void should_remove_final_modifier_from_the_given_class() throws IOException {
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      AccessFlags.FINAL.getValue(),
                                                      "Ljava/lang/Object;",
                                                      null, null, null, null, null);
        byte[] bytecode = DexPoolUtils.toBytecode(new ImmutableDexFile(Opcodes.getDefault(),
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
    public void should_remove_final_modifier_from_the_given_method() throws IOException {
        ImmutableMethod method = new ImmutableMethod("Lfoo/Bar;",
                                                     "f",
                                                     null,
                                                     "V",
                                                     AccessFlags.FINAL.getValue(),
                                                     null, null);
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      0,
                                                      "Ljava/lang/Object;",
                                                      null, null, null, null,
                                                      Collections.singleton(method));
        byte[] bytecode = DexPoolUtils.toBytecode(new ImmutableDexFile(Opcodes.getDefault(),
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
    public void should_remove_final_modifier_from_the_given_inner_class_annotation()
            throws IOException {
        Set<ImmutableAnnotationElement> elements = new HashSet<>();
        elements.add(new ImmutableAnnotationElement("name",
                                                    new ImmutableStringEncodedValue("Lfoo/Bar;")));
        elements.add(new ImmutableAnnotationElement("accessFlags",
                                                    new ImmutableIntEncodedValue(AccessFlags.FINAL.getValue())));
        ImmutableAnnotation annotation =
                new ImmutableAnnotation(0, "Ldalvik/annotation/InnerClass;", elements);
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      0,
                                                      "Ljava/lang/Object;",
                                                      null, null,
                                                      Collections.singleton(annotation),
                                                      null, null);
        byte[] bytecode = DexPoolUtils.toBytecode(new ImmutableDexFile(Opcodes.getDefault(),
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
