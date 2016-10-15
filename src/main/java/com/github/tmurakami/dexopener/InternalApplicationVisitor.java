package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ClassVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.MethodVisitor;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_ENUM;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_INTERFACE;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_PRIVATE;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_STATIC;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class InternalApplicationVisitor extends ApplicationVisitor {

    InternalApplicationVisitor(ApplicationVisitor av) {
        super(ASM4, av);
    }

    @Override
    public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces) {
        if ((access & (ACC_INTERFACE | ACC_ENUM | ACC_PRIVATE)) == 0) {
            return new InternalClassVisitor(api, super.visitClass(access & ~ACC_FINAL, name, signature, superName, interfaces));
        } else {
            return null;
        }
    }

    private static class InternalClassVisitor extends ClassVisitor {

        InternalClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, (access & (ACC_ENUM | ACC_PRIVATE)) == 0 ? access & ~ACC_FINAL : access);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String[] signature, String[] exceptions) {
            return super.visitMethod((access & (ACC_STATIC | ACC_PRIVATE)) == 0 ? access & ~ACC_FINAL : access, name, desc, signature, exceptions);
        }

    }

}
