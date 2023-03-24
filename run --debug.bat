@echo off
cd bin
java -Xmx512m -classpath bin;"../lib/*"; org.mron.lolparser.Main --debug
pause