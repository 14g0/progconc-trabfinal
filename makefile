arg1 := $(word 1, $(MAKECMDGOALS))
arg2 := $(word 2, $(MAKECMDGOALS))
arg3 := $(word 3, $(MAKECMDGOALS))
arg4 := $(word 4, $(MAKECMDGOALS))

# .PHONY: all matriz seq

compile: ./src/java/CredentialManager.java ./src/java/GitHubSearchTask.java ./src/java/Main.java ./src/java/Token.java
	javac -cp "src/lib/jackson-core-2.15.2.jar:src/lib/jackson-databind-2.15.2.jar" -d src/class src/java/*.java

run: ./src/class/Main.class ./src/lib/jackson-core-2.15.2.jar
	java -cp "src/class:src/lib/jackson-core-2.15.2.jar:src/lib/jackson-databind-2.15.2.jar" Main

jar: ./src/class/Main.class ./src/class/CredentialManager.class ./src/class/GitHubSearchTask.class ./src/class/Token.class
	jar cvfm dorks.jar -C src/class . -C src/lib .

test: jar
	@threads="1 5 10 20"; \
	dorksFiles="src/tests/dorks_20.txt src/tests/dorks_50.txt src/tests/dorks_80.txt"; \
	tokensFiles="src/tests/token_1.txt src/tests/token_2.txt src/tests/token_3.txt"; \
	for dorks in $$dorksFiles; do \
		for tokens in $$tokensFiles; do \
			for thread in $$threads; do \
				echo "Executando com $$thread threads, dorks: $$dorks, tokens: $$tokens"; \
				java -jar dorks.jar -d $$dorks -t $$tokens -n $$thread | tail -n 5 >> Resultado_teste.txt; \
			done; \
		done; \
	done

%:
	@:

clean:
	rm -rf src/class/*
	rmdir src/class