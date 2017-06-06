-dontobfuscate
-dontoptimize

-dontwarn com.beust.jcommander.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe

-keep class org.jf.dexlib2.Opcodes {
    public org.jf.dexlib2.Opcodes getDefault();
}
-keep class org.jf.dexlib2.dexbacked.DexBackedDexFile {
    public <init>(org.jf.dexlib2.Opcodes, byte[]);
    public java.util.Set getClasses();
}
-keep class org.jf.dexlib2.iface.ClassDef {
    public *;
}
-keep class org.jf.dexlib2.immutable.ImmutableClassDef {
    public <init>(java.lang.String, int, java.lang.String, java.util.Collection, java.lang.String, java.util.Collection, java.lang.Iterable, java.lang.Iterable);
    public ImmutableClassDef of(org.jf.dexlib2.immutable.ImmutableClassDef);
}
-keep class org.jf.dexlib2.writer.io.FileDataStore {
    public <init>(java.io.File);
}
-keep class org.jf.dexlib2.writer.pool.DexPool {
    public <init>(org.jf.dexlib2.Opcodes);
    public void internClass(org.jf.dexlib2.iface.ClassDef);
    public void writeTo(org.jf.dexlib2.writer.io.DexDataStore);
}
