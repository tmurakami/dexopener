package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ClassVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.MethodVisitor;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class ApplicationOpener extends ApplicationVisitor {

    ApplicationOpener(ApplicationVisitor av) {
        super(ASM4, av);
    }

    @Override
    public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces) {
        return new ClassOpener(api, super.visitClass(access & ~ACC_FINAL, name, signature, superName, interfaces));
    }

    private static class ClassOpener extends ClassVisitor {

        ClassOpener(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access & ~ACC_FINAL);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String[] signature, String[] exceptions) {
            return super.visitMethod(access & ~ACC_FINAL, name, desc, signature, exceptions);
        }

    }

}
