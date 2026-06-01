# Kotlinx Serialization — keep generated serializers and @Serializable classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class * {
    static **$* *;
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.alexanderglueck.urlpusher.**$$serializer { *; }
-keepclassmembers class com.alexanderglueck.urlpusher.** {
    *** Companion;
}
-keepclasseswithmembers class com.alexanderglueck.urlpusher.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit + OkHttp (also pulls in default rules from the libraries)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Keep generic signatures used by Retrofit/serialization
-keepattributes Signature, Exceptions, EnclosingMethod
