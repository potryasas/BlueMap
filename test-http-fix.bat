@echo off
echo ===== BlueMap HTTP Fix Test =====
echo.
echo This test will verify that the HTTP server streaming fix works properly.
echo.

echo === 1. Checking server is running ===
powershell "Get-Process | Where-Object {$_.ProcessName -eq 'java'} | Select-Object Id, ProcessName | Measure-Object"

echo.
echo === 2. Testing basic connectivity ===
powershell "try { $response = Invoke-WebRequest -Uri 'http://localhost:8100/' -Method Head -TimeoutSec 3; Write-Host 'Web server responding: HTTP' $response.StatusCode } catch { Write-Host 'Web server test failed:' $_.Exception.Message }"

echo.
echo === 3. Testing large file download (JS file) ===
powershell "try { $response = Invoke-WebRequest -Uri 'http://localhost:8100/assets/index-D09h1JRU.js' -TimeoutSec 10; Write-Host 'Large JS file test: SUCCESS - Size:' $response.Content.Length 'bytes' } catch { Write-Host 'Large file test failed:' $_.Exception.Message }"

echo.
echo === 4. Testing settings.json ===
powershell "try { $response = Invoke-WebRequest -Uri 'http://localhost:8100/settings.json' -TimeoutSec 5; Write-Host 'Settings.json test: SUCCESS' } catch { Write-Host 'Settings.json test failed:' $_.Exception.Message }"

echo.
echo === 5. Testing textures.json ===
powershell "try { $response = Invoke-WebRequest -Uri 'http://localhost:8100/maps/world/textures.json' -TimeoutSec 5; Write-Host 'Textures.json test: SUCCESS - Size:' $response.Content.Length 'bytes' } catch { Write-Host 'Textures.json test failed:' $_.Exception.Message }"

echo.
echo ===== RESULTS =====
echo If all tests show SUCCESS, the HTTP streaming fix is working properly.
echo The large JS file test is especially important - it should download without errors.
echo.
echo To complete the test:
echo 1. Open browser and go to http://localhost:8100/
echo 2. Check that the map loads without "stream closed" errors in server console
echo 3. Look for "Successfully sent X bytes" messages in server logs
echo.
pause 