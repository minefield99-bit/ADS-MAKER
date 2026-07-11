# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class **$$serializer {
    *** descriptor;
}
-keepclasseswithmembers class com.adsmaker.app.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.adsmaker.app.data.remote.**$$serializer { *; }
-keepclassmembers class com.adsmaker.app.data.remote.** {
    <fields>;
}

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
