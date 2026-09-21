# R8 rules for the release build.
#
# Most of what the app depends on (Compose, coroutines, okio, kotlinx-datetime)
# ships its own consumer rules, so this file only covers the two libraries that
# reach code by name at runtime -- the bundled SQLite driver via JNI, and
# DataStore's embedded protobuf -- plus Room's generated implementations.

# --- Bundled SQLite (androidx.sqlite:sqlite-bundled) -------------------------
# The driver binds to its native library through JNI, matching Java methods by
# their fully-qualified names. Renaming or removing them breaks the binding at
# runtime with an UnsatisfiedLinkError, which R8 cannot see.
-keep class androidx.sqlite.driver.bundled.** { *; }
-keepclasseswithmembernames class androidx.sqlite.** {
    native <methods>;
}

# --- Room 3 ------------------------------------------------------------------
# Room instantiates its generated `_Impl` database and DAO classes; on KMP these
# are referenced through the generated RoomDatabaseConstructor, but the runtime
# still looks some of them up reflectively.
-keep class * extends androidx.room3.RoomDatabase { <init>(); }
-keep class androidx.room3.** { *; }
-keep class com.grocemaxxer.app.**_Impl { *; }
-keep class com.grocemaxxer.app.CatalogDatabaseConstructor { *; }
-dontwarn androidx.room3.**

# --- DataStore preferences ---------------------------------------------------
# datastore-preferences-core embeds a repackaged protobuf-javalite, which reads
# and writes message fields reflectively.
-keep class androidx.datastore.preferences.protobuf.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn androidx.datastore.**

# --- App model types ---------------------------------------------------------
# Enum names are persisted (store choice, theme palette, section names in the
# catalog DB and in shared list text), and are read back with valueOf/name
# comparisons, so their names must survive obfuscation.
-keepclassmembers enum com.grocemaxxer.shared.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public java.lang.String name();
}
-keepclassmembers enum com.grocemaxxer.app.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public java.lang.String name();
}
