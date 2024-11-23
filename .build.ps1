task compile {
    javac -cp "src\lib\jackson-core-2.15.2.jar;src\lib\jackson-databind-2.15.2.jar" -d src\class src\java\*.java
}

task run {
    java -cp "src/class;src/lib/jackson-core-2.15.2.jar;src/lib/jackson-databind-2.15.2.jar" Main
}

task jar {
    $manifest = "Manifest.txt"
    Set-Content -Path $manifest -Value "Main-Class: Main`n"
    jar cvfm dorks.jar $manifest -C src/class . -C src/lib .
    Remove-Item $manifest
}

task test {
    $threads = @(1, 5, 10, 20)
    $dorksFiles = @("src/tests/dorks_20.txt", "src/tests/dorks_50.txt", "src/tests/dorks_80.txt")
    $tokensFiles = @("src/tests/token_1.txt", "src/tests/token_2.txt", "src/tests/token_3.txt")

    foreach ($dorks in $dorksFiles) {
        foreach ($tokens in $tokensFiles) {
            foreach ($thread in $threads) {
                Write-Output "Executando com $thread threads, dorks: $dorks, tokens: $tokens"
                java -jar dorks.jar -d $dorks -t $tokens -n $thread | Select-Object -Last 5 | Out-File -Append -FilePath "Resultado_teste.txt"
            }
        }
    }
}