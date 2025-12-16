# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room classes
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Keep database entities
-keep class com.delivery.database.entity.** { *; }

# Keep TypeConverters
-keep class com.delivery.database.converter.** { *; }
