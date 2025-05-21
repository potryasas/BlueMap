$url = "https://cdn.getbukkit.org/craftbukkit/craftbukkit-1.5.2-R1.0.jar"
$output = "libs/bukkit-1.5.2-R1.0.jar"

Write-Host "Downloading Bukkit 1.5.2-R1.0..."
Invoke-WebRequest -Uri $url -OutFile $output
Write-Host "Download complete!" 