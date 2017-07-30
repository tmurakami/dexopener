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

import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ClassOpenerTest {
    @Test
    public void openClass_should_remove_final_modifier() throws Exception {
        Set<AnnotationElement> innerClassElements = new HashSet<>();
        innerClassElements.add(new ImmutableAnnotationElement("name", new ImmutableStringEncodedValue("Lfoo/Bar;")));
        innerClassElements.add(new ImmutableAnnotationElement("accessFlags", new ImmutableIntEncodedValue(Modifier.FINAL)));
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(new ImmutableAnnotation(0, "Ldalvik/annotation/InnerClass;", innerClassElements));
        annotations.add(new ImmutableAnnotation(0, "Lfoo/BarAnnotation;", Collections.<AnnotationElement>emptyList()));
        String className = "Lfoo/Bar$Baz;";
        Method finalMethod = new ImmutableMethod(className,
                                                 "finalMethod",
                                                 Collections.<MethodParameter>emptySet(),
                                                 "V",
                                                 Modifier.FINAL,
                                                 Collections.<Annotation>emptySet(),
                                                 null);
        ClassDef in = new ImmutableClassDef(className,
                                            Modifier.FINAL,
                                            "Ljava/lang/Object;",
                                            null,
                                            null,
                                            annotations,
                                            Collections.<Field>emptySet(),
                                            Collections.singleton(finalMethod));
        ClassDef out = new ClassOpener().openClass(in);
        assertEquals(0, out.getAccessFlags());
        assertEquals(0, out.getMethods().iterator().next().getAccessFlags());
        int accessFlags = -1;
        for (Annotation a : out.getAnnotations()) {
            if (a.getType().equals("Ldalvik/annotation/InnerClass;")) {
                for (AnnotationElement e : a.getElements()) {
                    if (e.getName().equals("accessFlags")) {
                        accessFlags = ((IntEncodedValue) e.getValue()).getValue();
                    }
                }
            }
        }
        assertEquals(0, accessFlags);
    }
}
