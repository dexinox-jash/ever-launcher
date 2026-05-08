# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ---- DataStore Preferences (key/value store, not protobuf) ----
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---- Kotlin enums used via reflection (.valueOf / .values) ----
-keepclassmembers enum com.everlauncher.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---- Domain models and DB entities (accessed by Room + DataStore) ----
-keepclassmembers class com.everlauncher.domain.model.** { *; }
-keepclassmembers class com.everlauncher.data.db.entities.** { *; }

# ---- BroadcastReceiver used in PendingIntent (AlarmManager) ----
-keep class com.everlauncher.receiver.ModeTransitionReceiver { *; }

# ---- Kotlin coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ---- Jetpack Compose ----
-dontwarn androidx.compose.**

# ---- Debug info for crash reporting ----
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
