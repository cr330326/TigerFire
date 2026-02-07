# TigerFire ProGuard 配置
# 用于发布版本代码混淆和优化

# ============================================
# Kotlin & Coroutines
# ============================================
-keep class kotlin.Metadata { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# ============================================
# Jetpack Compose
# ============================================
-keep class androidx.compose.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# 保留 Compose 生成的 Lambda 类
-keepclassmembers class androidx.compose.** {
    *** compose$lambda$*(...);
}

# ============================================
# SQLDelight
# ============================================
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**
-keep class com.cryallen.tigerfire.database.** { *; }

# ============================================
# Lottie Animation
# ============================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ============================================
# ExoPlayer (Media3)
# ============================================
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ============================================
# Serialization
# ============================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keep class kotlinx.serialization.json.** { *; }
-keep @kotlinx.serialization.Serializable class * {*;}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# ============================================
# Navigation
# ============================================
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ============================================
# 保留数据模型
# ============================================
-keep class com.cryallen.tigerfire.domain.model.** { *; }
-keep class com.cryallen.tigerfire.data.model.** { *; }

# ============================================
# 保留 ViewModel
# ============================================
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# ============================================
# 保留枚举
# ============================================
-keepclassmembers enum com.cryallen.tigerfire.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================
# 原生方法
# ============================================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================
# 优化配置
# ============================================
# 不优化枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================
# 移除日志
# ============================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ============================================
# 保留注解
# ============================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile
-keepattributes LineNumberTable

# ============================================
# 第三方库混淆规则
# ============================================
# OkHttp (如果使用)
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson (如果使用)
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================
# 资源优化
# ============================================
# 保留特定资源（如果需要）
# -keep class com.cryallen.tigerfire.R$* { *; }
