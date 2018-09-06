-dontoptimize

-dontnote com.google.common.**
-dontnote org.jf.util.jcommander.**
-dontnote sun.misc.Unsafe

-dontwarn com.beust.jcommander.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe

-keepattributes Exceptions,InnerClasses,Signature,SourceFile,LineNumberTable,EnclosingMethod

-keepnames class ** {
    *;
}

-keep class com.github.tmurakami.classinjector.ClassInjector {
    public static com.github.tmurakami.classinjector.ClassInjector from(com.github.tmurakami.classinjector.ClassSource);
    public void into(java.lang.ClassLoader);
}
-keep class com.github.tmurakami.classinjector.android.DexClassFile {
    public <init>(java.lang.String, dalvik.system.DexFile);
}
-keep class org.jf.dexlib2.Opcodes {
    public static org.jf.dexlib2.Opcodes getDefault();
}
-keep class org.jf.dexlib2.dexbacked.DexBackedDexFile {
    public <init>(org.jf.dexlib2.Opcodes, byte[]);
}
-keep class org.jf.dexlib2.rewriter.DexRewriter {
    public <init>(org.jf.dexlib2.rewriter.RewriterModule);
    public org.jf.dexlib2.iface.DexFile rewriteDexFile(org.jf.dexlib2.iface.DexFile);
}
-keep class org.jf.dexlib2.rewriter.RewriterModule
-keep class org.jf.dexlib2.writer.io.FileDataStore {
    public <init>(java.io.File);
}
-keep class org.jf.dexlib2.writer.pool.DexPool {
    public static void writeTo(org.jf.dexlib2.writer.io.DexDataStore, org.jf.dexlib2.iface.DexFile);
}
