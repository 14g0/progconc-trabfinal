arg1 := $(word 1, $(MAKECMDGOALS))
arg2 := $(word 2, $(MAKECMDGOALS))
arg3 := $(word 3, $(MAKECMDGOALS))
arg4 := $(word 4, $(MAKECMDGOALS))

# .PHONY: all matriz seq

compile: ./src/java/CredentialManager.java ./src/java/GitHubSearchTask.java ./src/java/Main.java ./src/java/Token.java
	javac -cp "src/lib/jackson-core-2.15.2.jar:src/lib/jackson-databind-2.15.2.jar" -d src/class src/java/*.java

run: ./src/class/Main.class ./src/lib/jackson-core-2.15.2.jar
	java -cp "src/class:src/lib/jackson-core-2.15.2.jar:src/lib/jackson-databind-2.15.2.jar" Main


%:
	@:

clean:
	rm -rf src/class/*
	rmdir src/class