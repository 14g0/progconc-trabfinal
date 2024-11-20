#!/bin/bash

THREADS=(1 5 10 20)
DORKS_FILES=("Dorks/dorks_20.txt" "Dorks/dorks_50.txt" "Dorks/dorks_80.txt")
TOKENS_FILES=("Tokens/tokens_1.txt" "Tokens/tokens_2.txt" "Tokens/tokens_3.txt")

for dorks in "${DORKS_FILES[@]}"; do
  for tokens in "${TOKENS_FILES[@]}"; do
    for threads in "${THREADS[@]}"; do
      echo "Executando com $threads threads, dorks: $dorks, tokens: $tokens"
      java -jar API-Key-Scraper.jar -d "$dorks" -t "$tokens" -n "$threads" | tail -n 5 >> "Resultado_teste.txt"
    done
  done
done
