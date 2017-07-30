package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.AnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.IntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableMethod;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

final class ClassOpener {

    ClassDef openClass(ClassDef def) {
        return new ImmutableClassDef(def.getType(),
                                     removeFinalModifier(def.getAccessFlags()),
                                     def.getSuperclass(),
                                     def.getInterfaces(),
                                     def.getSourceFile(),
                                     openInnerClassAnnotation(def.getAnnotations()),
                                     def.getFields(),
                                     openMethods(def.getMethods()));
    }

    private static int removeFinalModifier(int accessFlags) {
        return accessFlags & ~Modifier.FINAL;
    }

    private static Set<Annotation> openInnerClassAnnotation(Set<? extends Annotation> annotations) {
        Set<Annotation> openedAnnotations = new HashSet<>();
        for (Annotation a : annotations) {
            String type = a.getType();
            if (type.equals("Ldalvik/annotation/InnerClass;")) {
                Set<AnnotationElement> elementSet = new HashSet<>();
                for (AnnotationElement e : a.getElements()) {
                    String name = e.getName();
                    if (name.equals("accessFlags")) {
                        IntEncodedValue value = (IntEncodedValue) e.getValue();
                        int accessFlags = removeFinalModifier(value.getValue());
                        IntEncodedValue newValue = new ImmutableIntEncodedValue(accessFlags);
                        elementSet.add(new ImmutableAnnotationElement(name, newValue));
                    } else {
                        elementSet.add(e);
                    }
                }
                openedAnnotations.add(new ImmutableAnnotation(a.getVisibility(), type, elementSet));
            } else {
                openedAnnotations.add(a);
            }
        }
        return openedAnnotations;
    }

    private static Set<Method> openMethods(Iterable<? extends Method> methods) {
        Set<Method> openedMethods = new HashSet<>();
        for (Method m : methods) {
            openedMethods.add(new ImmutableMethod(m.getDefiningClass(),
                                                  m.getName(),
                                                  m.getParameters(),
                                                  m.getReturnType(),
                                                  removeFinalModifier(m.getAccessFlags()),
                                                  m.getAnnotations(),
                                                  m.getImplementation()));
        }
        return openedMethods;
    }

}
