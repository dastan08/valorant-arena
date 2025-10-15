# Fix and Upload to S3
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Fix index.html and Upload to S3" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to build directory
$buildDir = "dist\valorant-arena-frontend"

if (!(Test-Path $buildDir)) {
    Write-Host "Error: Build directory not found!" -ForegroundColor Red
    Write-Host "Please run 'ng build' first" -ForegroundColor Yellow
    exit 1
}

Set-Location $buildDir

# Fix double extension
if (Test-Path "index.html.html") {
    Write-Host "Found index.html.html - Renaming to index.html..." -ForegroundColor Yellow
    Rename-Item "index.html.html" "index.html" -Force
    Write-Host "✓ Fixed!" -ForegroundColor Green
}

# Verify index.html exists
if (Test-Path "index.html") {
    Write-Host "✓ index.html found" -ForegroundColor Green
} else {
    Write-Host "✗ index.html NOT found!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Files to upload:" -ForegroundColor Cyan
Get-ChildItem | Select-Object Name, Length | Format-Table

Write-Host ""
Write-Host "Ready to upload to S3!" -ForegroundColor Green
Write-Host ""
Write-Host "Run this command:" -ForegroundColor Yellow
Write-Host "aws s3 sync . s3://your-bucket-name/ --delete" -ForegroundColor White
Write-Host ""
Write-Host "Or enter your bucket name to upload now:"
$bucket = Read-Host "Bucket name (or press Enter to skip)"

if ($bucket) {
    Write-Host ""
    Write-Host "Uploading to s3://$bucket/..." -ForegroundColor Cyan
    aws s3 sync . "s3://$bucket/" --delete
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✓ Upload successful!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Your site should be available at:" -ForegroundColor Cyan
        Write-Host "http://$bucket.s3-website-ap-south-1.amazonaws.com" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "✗ Upload failed!" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
