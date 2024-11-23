#!/bin/bash

THREADS=(1 5 10 20)
DORKS_FILES=("tests/dorks_20.txt" "tests/dorks_50.txt" "tests/dorks_80.txt")
TOKENS_FILES=("tests/token_1.txt" "tests/token_2.txt" "tests/token_3.txt")

for dorks in "${DORKS_FILES[@]}"; do
  for tokens in "${TOKENS_FILES[@]}"; do
    for threads in "${THREADS[@]}"; do
      echo "Executando com $threads threads, dorks: $dorks, tokens: $tokens"
      java -jar API-Key-Scraper.jar -d "$dorks" -t "$tokens" -n "$threads" | tail -n 5 >> "Resultado_teste.txt"
    done
  done
done
