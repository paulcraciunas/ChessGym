# ProGuard rules for the benchmark build type.
# The app benchmark variant uses R8 for realistic performance measurement.

# --- Benchmark activity and supporting classes (constructed without Hilt) ---
-keep class com.paulcraciunas.chessgym.benchmark.** { *; }

# --- ViewModels instantiated via ViewModelProvider.Factory (reflection) ---
-keep class com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakViewModel {
    <init>(...);
}
-keep class com.paulcraciunas.screens.puzzles.streak.vm.PuzzleStreakViewModel2 {
    <init>(...);
}

# --- Test fixture fakes used from benchmark source set ---
-keep class com.paulcraciunas.domain.api.general.FakeTimer { *; }
-keep class com.paulcraciunas.settings.application.api.FakeAppSettingsRepository { *; }

# --- Compose: keep semantics/test tags accessible to UI Automator ---
-keep class androidx.compose.ui.semantics.** { *; }
