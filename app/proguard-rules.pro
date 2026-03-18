# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/android-sdk/tools/proguard/proguard-android.txt

# Keep ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep data classes
-keep class com.guardian.app.data.** { *; }
