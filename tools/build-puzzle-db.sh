#!/usr/bin/env bash
#
# Builds three tiered SQLite puzzle databases from the Lichess puzzle CSV:
#   - full:    all puzzles
#   - compact: ~1/3 of puzzles, rating-bucketed sampling
#   - lite:    ~1/10 of puzzles, rating-bucketed sampling
#
# Uses the :tools:puzzle-db-builder Gradle module to ensure binary serialization
# matches the app's PuzzleWriter, producing databases compatible with PuzzleReader.
#
# Prerequisites: JDK 17+, Gradle wrapper in project root

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CSV_FILE="${1:-puzzles.csv}"

if [ ! -f "$CSV_FILE" ]; then
  echo "Error: CSV file not found: $CSV_FILE"
  echo "Usage: $0 <path-to-puzzles.csv>"
  exit 1
fi

CSV_ABSOLUTE="$(cd "$(dirname "$CSV_FILE")" && pwd)/$(basename "$CSV_FILE")"

echo "=== Building Puzzle Databases ==="
echo "Source: $CSV_ABSOLUTE"
echo "Project: $PROJECT_ROOT"
echo ""

cd "$PROJECT_ROOT"
./gradlew :tools:puzzle-db-builder:run --args="$CSV_ABSOLUTE" --quiet

echo ""
echo "Database files generated:"
for tier in full compact lite; do
  if [ -f "puzzles-${tier}.db" ]; then
    size=$(du -h "puzzles-${tier}.db" | cut -f1)
    echo "  puzzles-${tier}.db ($size)"
  fi
done
