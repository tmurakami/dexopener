-dontoptimize

-dontnote java.lang.invoke.**
-dontnote com.google.common.**

-dontwarn java.lang.ClassValue
-dontwarn javax.lang.model.element.Modifier
-dontwarn sun.misc.Unsafe

-keepattributes Exceptions,InnerClasses,Signature,SourceFile,LineNumberTable,EnclosingMethod

-keepnames class ** {
    *;
}

-keep class com.google.common.base.Functions {
    public static com.google.common.base.Function constant(java.lang.Object);
}
-keep class com.google.common.base.Predicates {
    public static com.google.common.base.Predicate compose(com.google.common.base.Predicate, com.google.common.base.Function);
}
-keep class com.google.common.collect.Iterables {
    public static java.lang.Iterable filter(java.lang.Iterable, com.google.common.base.Predicate);
    public static java.lang.Iterable partition(java.lang.Iterable, int);
}
-keep class com.google.common.collect.Maps {
    public static com.google.common.collect.ImmutableMap toMap(java.lang.Iterable, com.google.common.base.Function);
    public static java.util.Map transformValues(java.util.Map, com.google.common.base.Function);
}
-keep class com.google.common.io.ByteStreams {
    public static byte[] toByteArray(java.io.InputStream);
}
-keep class org.jf.dexlib2.analysis.reflection.util.ReflectionUtils {
    public static java.lang.String javaToDexName(java.lang.String);
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
