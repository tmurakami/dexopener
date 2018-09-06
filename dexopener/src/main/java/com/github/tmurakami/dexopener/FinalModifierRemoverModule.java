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

import java.util.Set;

final class FinalModifierRemoverModule extends RewriterModule {

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
                                if (!AccessFlags.FINAL.isSet(accessFlags)) {
                                    return annotationElement;
                                }
                                int nonFinal = accessFlags & ~AccessFlags.FINAL.getValue();
                                EncodedValue newValue = new ImmutableIntEncodedValue(nonFinal);
                                return new ImmutableAnnotationElement(name, newValue);
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
                        return super.getAccessFlags() & ~AccessFlags.FINAL.getValue();
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
                        return super.getAccessFlags() & ~AccessFlags.FINAL.getValue();
                    }
                };
            }
        };
    }

}
