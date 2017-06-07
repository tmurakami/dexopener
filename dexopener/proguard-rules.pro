-dontoptimize

-dontwarn android.support.annotation.**
-dontwarn com.beust.jcommander.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe

-keepattributes Exceptions,InnerClasses,Signature,SourceFile,LineNumberTable,EnclosingMethod

-keepnames class ** {
    *;
}

-keep class com.github.tmurakami.classinjector.ClassInjector {
    com.github.tmurakami.classinjector.ClassInjector from(com.github.tmurakami.classinjector.ClassSource);
    void into(java.lang.ClassLoader);
}
-keep class com.github.tmurakami.classinjector.ClassSources {
    <init>(java.lang.Iterable);
}
-keep class com.github.tmurakami.classinjector.android.DexClassFile {
    <init>(java.lang.String, dalvik.system.DexFile);
}
-keep class org.jf.dexlib2.Opcodes {
    org.jf.dexlib2.Opcodes getDefault();
}
-keep class org.jf.dexlib2.dexbacked.DexBackedDexFile {
    <init>(org.jf.dexlib2.Opcodes, byte[]);
    java.util.Set getClasses();
}
-keep class org.jf.dexlib2.iface.ClassDef {
    *;
}
-keep class org.jf.dexlib2.immutable.ImmutableClassDef {
    <init>(java.lang.String, int, java.lang.String, java.util.Collection, java.lang.String, java.util.Collection, java.lang.Iterable, java.lang.Iterable);
    org.jf.dexlib2.immutable.ImmutableClassDef of(org.jf.dexlib2.immutable.ImmutableClassDef);
}
-keep class org.jf.dexlib2.writer.io.FileDataStore {
    <init>(java.io.File);
}
-keep class org.jf.dexlib2.writer.pool.DexPool {
    <init>(org.jf.dexlib2.Opcodes);
    void internClass(org.jf.dexlib2.iface.ClassDef);
    void writeTo(org.jf.dexlib2.writer.io.DexDataStore);
}
