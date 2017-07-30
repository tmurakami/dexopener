-dontoptimize

-dontnote com.google.common.**
-dontnote org.jf.util.jcommander.**
-dontnote sun.misc.Unsafe

-dontwarn android.support.annotation.**
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
    public java.util.Set getClasses();
}
-keep class org.jf.dexlib2.iface.ClassDef {
    public int getAccessFlags();
    public java.util.Set getAnnotations();
    public java.lang.Iterable getFields();
    public java.util.List getInterfaces();
    public java.lang.Iterable getMethods();
    public java.lang.String getSourceFile();
    public java.lang.String getSuperclass();
    public java.lang.String getType();
}
-keep class org.jf.dexlib2.immutable.ImmutableClassDef {
    public <init>(java.lang.String, int, java.lang.String, java.util.Collection, java.lang.String, java.util.Collection, java.lang.Iterable, java.lang.Iterable);
    public static org.jf.dexlib2.immutable.ImmutableClassDef of(org.jf.dexlib2.iface.ClassDef);
}
-keep class org.jf.dexlib2.writer.DexWriter {
    public void writeTo(org.jf.dexlib2.writer.io.DexDataStore);
}
-keep class org.jf.dexlib2.writer.io.FileDataStore {
    public <init>(java.io.File);
}
-keep class org.jf.dexlib2.writer.pool.DexPool {
    public <init>(org.jf.dexlib2.Opcodes);
    public void internClass(org.jf.dexlib2.iface.ClassDef);
}
