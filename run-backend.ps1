$envFile = Join-Path $PSScriptRoot ".env"

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()

    if ($line -and !$line.StartsWith("#") -and $line.Contains("=")) {
        $name, $value = $line -split "=", 2

        $name = $name.Trim()
        $value = $value.Trim().Trim('"').Trim("'")

        Set-Item -Path "Env:$name" -Value $value
    }
}

Set-Location (Join-Path $PSScriptRoot "backend")

mvn spring-boot:run "-Dspring-boot.run.profiles=local"
