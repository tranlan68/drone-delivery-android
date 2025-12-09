# Room ProGuard rules for consumers
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
