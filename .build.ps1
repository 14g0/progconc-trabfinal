task compile {
    javac -cp "src\lib\jackson-core-2.15.2.jar;src\lib\jackson-databind-2.15.2.jar" -d src\class src\java\*.java
}

task run {
    java -cp "src/class;src/lib/jackson-core-2.15.2.jar;src/lib/jackson-databind-2.15.2.jar" Main
}