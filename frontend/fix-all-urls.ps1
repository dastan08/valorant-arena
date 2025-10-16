# PowerShell script to replace all hardcoded URLs with environment configuration
Write-Host "Fixing all hardcoded URLs..." -ForegroundColor Cyan

$files = @(
    "src/app/core/services/team.service.ts",
    "src/app/core/services/tournament-moderator.service.ts",
    "src/app/core/services/scrim.service.ts",
    "src/app/pages/dashboard/dashboard.component.ts",
    "src/app/pages/profile/profile.component.ts",
    "src/app/pages/teams/teams.component.ts",
    "src/app/pages/tournaments/tournament-create/tournament-create.component.ts"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Processing: $file" -ForegroundColor Yellow
        $content = Get-Content $file -Raw
        
        # Replace localhost URLs
        $content = $content -replace "http://localhost:8080/api", "`${environment.apiUrl}"
        $content = $content -replace "http://localhost:8080", "`${environment.apiUrl.replace('/api', '')}"
        
        # Replace EC2 IP URLs  
        $content = $content -replace "http://13.201.185.135:8080/api", "`${environment.apiUrl}"
        $content = $content -replace "http://13.201.185.135:8080", "`${environment.apiUrl.replace('/api', '')}"
        
        Set-Content $file $content -NoNewline
        Write-Host "  ✓ Fixed" -ForegroundColor Green
    }
}

Write-Host "`nDone! Now rebuild:" -ForegroundColor Cyan
Write-Host "  ng build --configuration production" -ForegroundColor White
