# PowerShell script to test rate limiting
$baseUrl = "http://localhost:8080"
$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "Testing rate limiting..."
Write-Host "Making 11 requests to create games with unique usernames..."

for ($i = 1; $i -le 11; $i++) {
    $body = @{
        username = "test_user_$i"  # Use a unique username for each request
        gameType = "standard"
        timeControlMinutes = 10
        isRated = $false
    } | ConvertTo-Json

    Write-Host "`nRequest $i :"
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/games" -Method Post -Headers $headers -Body $body
        Write-Host "Success! Response: $($response | ConvertTo-Json)"
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $statusDescription = $_.Exception.Response.StatusDescription
        Write-Host "Error: $statusCode - $statusDescription"
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $reader.BaseStream.Position = 0
            $reader.DiscardBufferedData()
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response body: $responseBody"
        }
    }
    
    # Small delay between requests
    Start-Sleep -Milliseconds 100
}

Write-Host "`nRate limit test completed!" 