package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ClassVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.MethodVisitor;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;

final class InternalApplicationVisitor extends ApplicationVisitor {

    private final ClassNameFilter classNameFilter;

    InternalApplicationVisitor(int api, ApplicationVisitor av, ClassNameFilter classNameFilter) {
        super(api, av);
        this.classNameFilter = classNameFilter;
    }

    @Override
    public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces) {
        String className = name.substring(1, name.length() - 1).replace('/', '.');
        if (classNameFilter.accept(className)) {
            return new ClassVisitor(api, super.visitClass(access & ~ACC_FINAL, name, signature, superName, interfaces)) {
                @Override
                public void visitInnerClass(String name, String outerName, String innerName, int access) {
                    super.visitInnerClass(name, outerName, innerName, access & ~ACC_FINAL);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String[] signature, String[] exceptions) {
                    return super.visitMethod(access & ~ACC_FINAL, name, desc, signature, exceptions);
                }
            };
        } else {
            return null;
        }
    }

}
