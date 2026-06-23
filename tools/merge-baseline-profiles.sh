#!/usr/bin/env zsh
#
# Merges baseline profile outputs from connected test runs into a single
# baseline-prof.txt for the app module.
#
# Usage: ./tools/merge-baseline-profiles.sh
#
# Finds all non-timestamped profile files from the benchmark output directory,
# concatenates them, removes duplicates, and writes to app/src/main/baseline-prof.txt.

set -euo pipefail

SCRIPT_DIR="${0:A:h}"
PROJECT_ROOT="${SCRIPT_DIR:h}"

OUTPUT_BASE="$PROJECT_ROOT/benchmark/build/outputs/connected_android_test_additional_output"
TARGET_FILE="$PROJECT_ROOT/app/src/main/baseline-prof.txt"

# Prefer baselineProfile variant output; fall back to benchmark
if [ -d "$OUTPUT_BASE/baselineProfile" ]; then
    OUTPUT_DIR="$OUTPUT_BASE/baselineProfile"
elif [ -d "$OUTPUT_BASE/benchmark" ]; then
    OUTPUT_DIR="$OUTPUT_BASE/benchmark"
elif [ -d "$OUTPUT_BASE" ]; then
    OUTPUT_DIR="$OUTPUT_BASE"
else
    echo "Error: No profile output directory found at:"
    echo "  $OUTPUT_BASE"
    echo "Run the BaselineProfileGenerator tests first:"
    echo "  ./gradlew :benchmark:connectedBaselineProfileAndroidTest"
    exit 1
fi

echo "Using output directory: $OUTPUT_DIR"

# Find non-timestamped profile files (no date pattern like -2026-06-23-15-48-34)
profile_files=()
while IFS= read -r f; do
    profile_files+=("$f")
done < <(find "$OUTPUT_DIR" -name "*-prof.txt" -type f | grep -v '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]')

if [ ${#profile_files[@]} -eq 0 ]; then
    echo "Error: No profile files found in $OUTPUT_DIR"
    exit 1
fi

echo "Found profile files:"
for f in "${profile_files[@]}"; do
    echo "  $(basename "$f") ($(wc -l < "$f") lines)"
done
echo ""

# Concatenate all files (ensuring trailing newlines), sort, and remove duplicate lines
for f in "${profile_files[@]}"; do
    cat "$f"
    echo ""
done | sort -u | sed '/^$/d' > "$TARGET_FILE"

LINE_COUNT=$(wc -l < "$TARGET_FILE")
echo "Merged into: $TARGET_FILE"
echo "Total unique rules: $LINE_COUNT"
