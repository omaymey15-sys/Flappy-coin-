# Android
-keep public class android.** { *; }
-keep public class javax.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# App
-keep class com.example.flappycoin.** { *; }
-keep interface com.example.flappycoin.** { *; }

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Models
-keep class com.example.flappycoin.models.** { *; }
-keep class com.example.flappycoin.utils.** { *; }