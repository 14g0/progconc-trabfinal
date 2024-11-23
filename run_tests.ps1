$threads = @(1, 5, 10, 20)
$dorksFiles = @("tests/dorks_20.txt", "tests/dorks_50.txt", "tests/dorks_80.txt")
$tokensFiles = @("tests/token_1.txt", "tests/token_2.txt", "tests/token_3.txt")

foreach ($dorks in $dorksFiles) {
    foreach ($tokens in $tokensFiles) {
        foreach ($thread in $threads) {
            Write-Output "Executando com $thread threads, dorks: $dorks, tokens: $tokens"
            java -jar API-Key-Scraper.jar -d $dorks -t $tokens -n $thread | Select-Object -Last 5 | Out-File -Append -FilePath "Resultado_teste.txt"
        }
    }
}