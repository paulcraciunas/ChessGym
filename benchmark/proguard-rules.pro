# ProGuard rules for the :benchmark macrobenchmark test APK.
# This APK instruments the target app via UI Automator (separate process).

# Keep the benchmark test class and framework entry points
-keep class com.paulcraciunas.chessgym.macrobenchmark.** { *; }
-keep class androidx.benchmark.** { *; }
