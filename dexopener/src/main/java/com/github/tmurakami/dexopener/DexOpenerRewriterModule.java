package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.AnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.EncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.value.IntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.AnnotationElementRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.AnnotationRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.ClassDefRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.MethodRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.Rewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.RewriterModule;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.RewriterUtils;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.Rewriters;

import java.lang.reflect.Modifier;
import java.util.Set;

final class DexOpenerRewriterModule extends RewriterModule {

    @Override
    public Rewriter<Annotation> getAnnotationRewriter(Rewriters rewriters) {
        return new AnnotationRewriter(rewriters) {
            @Override
            public Annotation rewrite(Annotation annotation) {
                if (!annotation.getType().equals("Ldalvik/annotation/InnerClass;")) {
                    return super.rewrite(annotation);
                }
                return new RewrittenAnnotation(annotation) {
                    @Override
                    public Set<? extends AnnotationElement> getElements() {
                        return RewriterUtils.rewriteSet(new AnnotationElementRewriter(rewriters) {
                            @Override
                            public AnnotationElement rewrite(AnnotationElement annotationElement) {
                                String name = annotationElement.getName();
                                if (!name.equals("accessFlags")) {
                                    return annotationElement;
                                }
                                EncodedValue value = annotationElement.getValue();
                                int accessFlags = ((IntEncodedValue) value).getValue();
                                int nonFinal = accessFlags & ~Modifier.FINAL;
                                if (nonFinal == accessFlags) {
                                    return annotationElement;
                                }
                                return new ImmutableAnnotationElement(name, new ImmutableIntEncodedValue(nonFinal));
                            }
                        }, annotation.getElements());
                    }
                };
            }
        };
    }

    @Override
    public Rewriter<ClassDef> getClassDefRewriter(Rewriters rewriters) {
        return new ClassDefRewriter(rewriters) {
            @Override
            public ClassDef rewrite(ClassDef classDef) {
                return new RewrittenClassDef(classDef) {
                    @Override
                    public int getAccessFlags() {
                        return super.getAccessFlags() & ~Modifier.FINAL;
                    }
                };
            }
        };
    }

    @Override
    public Rewriter<Method> getMethodRewriter(Rewriters rewriters) {
        return new MethodRewriter(rewriters) {
            @Override
            public Method rewrite(Method method) {
                return new RewrittenMethod(method) {
                    @Override
                    public int getAccessFlags() {
                        return super.getAccessFlags() & ~Modifier.FINAL;
                    }
                };
            }
        };
    }

}
