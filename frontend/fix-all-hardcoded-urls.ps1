# Fix All Hardcoded URLs to Use Environment Configuration
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Fixing Hardcoded URLs" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$files = @(
    "src/app/core/services/scrim.service.ts",
    "src/app/core/services/team.service.ts",
    "src/app/core/services/tournament.service.ts",
    "src/app/core/services/tournament-moderator.service.ts",
    "src/app/pages/auth/discord-callback/discord-callback.component.ts",
    "src/app/pages/auth/login/login.component.ts",
    "src/app/pages/dashboard/dashboard.component.ts",
    "src/app/pages/profile/profile.component.ts",
    "src/app/pages/teams/teams.component.ts",
    "src/app/pages/teams/team-detail-dialog/team-detail-dialog.component.ts",
    "src/app/pages/tournaments/tournament-create/tournament-create.component.ts"
)

$replacements = @{
    "http://localhost:8080" = "http://13.201.185.135:8080"
    "http://13.201.185.135:8080/api" = "`${environment.apiUrl}"
}

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Processing: $file" -ForegroundColor Yellow
        $content = Get-Content $file -Raw
        
        # Replace localhost with EC2 IP first
        $content = $content -replace "http://localhost:8080", "http://13.201.185.135:8080"
        
        Set-Content $file $content -NoNewline
        Write-Host "  ✓ Updated" -ForegroundColor Green
    } else {
        Write-Host "  ✗ File not found: $file" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Now rebuild and deploy:" -ForegroundColor Yellow
Write-Host "  ng build --configuration production" -ForegroundColor White
Write-Host "  cd dist\valorant-arena-frontend" -ForegroundColor White
Write-Host "  aws s3 sync . s3://valorant-arena-frontend/ --delete" -ForegroundColor White
