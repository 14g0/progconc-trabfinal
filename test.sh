#!/bin/bash

THREADS=(1 5 10 20)
DORKS_FILES=("src/tests/dorks_20.txt" "src/tests/dorks_50.txt" "src/tests/dorks_80.txt")
TOKENS_FILES=("src/tests/token_1.txt" "src/tests/token_2.txt" "src/tests/token_3.txt")

for threads in "${THREADS[@]}"; do
    for dorks in "${DORKS_FILES[@]}"; do
        for tokens in "${TOKENS_FILES[@]}"; do
            for i in {1..15}; do
                echo "Executando com $threads threads, dorks: $dorks, tokens: $tokens"
                java -jar dorks.jar -d "$dorks" -t "$tokens" -n "$threads" | tail -n 5 >> "Resultado_teste.txt"
                sleep 90
            done
        done
    done
done