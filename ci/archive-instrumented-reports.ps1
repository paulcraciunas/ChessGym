$ErrorActionPreference = "SilentlyContinue"

$reports = @(
    Get-ChildItem -Recurse -Directory -Path '.' |
    Where-Object { $_.FullName -match 'build[\\/]reports[\\/]androidTests$' }
)
if ($reports.Count -gt 0) {
    Compress-Archive -Path $reports.FullName -DestinationPath 'instrumented-test-reports.zip' -Force
    Write-Host "Archived $($reports.Count) report directories into instrumented-test-reports.zip"
} else {
    Write-Host "No androidTests report directories found"
}

$results = @(
    Get-ChildItem -Recurse -Directory -Path '.' |
    Where-Object { $_.FullName -match 'build[\\/]outputs[\\/]androidTest-results$' }
)
if ($results.Count -gt 0) {
    Compress-Archive -Path $results.FullName -DestinationPath 'instrumented-test-results.zip' -Force
    Write-Host "Archived $($results.Count) result directories into instrumented-test-results.zip"
} else {
    Write-Host "No androidTest-results directories found"
}
