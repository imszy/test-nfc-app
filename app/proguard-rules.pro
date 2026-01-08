# Add project specific ProGuard rules here.

# NFC相关类不混淆
-keep class android.nfc.** { *; }
-keep class com.nfctools.reader.data.local.entity.** { *; }

# Room数据库
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
