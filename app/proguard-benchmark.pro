# ProGuard rules for the benchmark build type.
# The app benchmark variant uses R8 for realistic performance measurement.

# --- Compose: keep semantics/test tags accessible to UI Automator ---
-keep class androidx.compose.ui.semantics.** { *; }
