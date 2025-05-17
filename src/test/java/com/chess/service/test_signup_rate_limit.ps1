# PowerShell script to test rate limiting on signup endpoint
$baseUrl = "http://localhost:8080"
$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "Testing rate limiting on signup endpoint..."
Write-Host "Making 11 requests to create guest users..."

for ($i = 1; $i -le 11; $i++) {
    $body = @{
        username = "guest_test_$i"
        email = "guest_test_$i@example.com"
        password = "password123"
    } | ConvertTo-Json

    Write-Host "`nRequest $i :"
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/signup" -Method Post -Headers $headers -Body $body
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