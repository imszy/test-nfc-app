# Add project specific ProGuard rules here.

# ============== NFC相关 ==============
-keep class android.nfc.** { *; }
-keep class com.nfctools.reader.data.local.entity.** { *; }
-keep class com.nfctools.reader.domain.model.** { *; }

# ============== Room数据库 ==============
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# ============== Hilt ==============
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ============== Kotlin Coroutines ==============
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ============== Kotlin Serialization ==============
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ============== Jetpack Compose ==============
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============== Google Play Services ==============
-keep class com.google.android.play.** { *; }
-dontwarn com.google.android.play.**

# ============== DataStore ==============
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# ============== 通用规则 ==============
# 保留注解
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# 保留Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留R文件
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 移除日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ============== 崩溃报告优化 ==============
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable