@echo off
cd bin
java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Xmx512m -classpath bin;"../lib/*"; org.mron.lolparser.Main --debug --mission-control
pause