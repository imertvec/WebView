-keep class ru.vagavagus.customwebview.ResponseJson
-keep class android.webkit.**

#### OkHttp, Retrofit and Moshi
-dontwarn okhttp3.**
-dontwarn retrofit2.Platform.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Kotlin reflect
-dontwarn kotlin.reflect.jvm.internal.**
-keep class kotlin.reflect.jvm.internal.** { *; }