#!/usr/bin/bash
echo Initializing the encryption engines
sleep 2
echo Vroom Vroom...
sleep 1
echo Ready for liftoff

/usr/lib/jvm/java-8-openjdk-amd64/bin/java -classpath /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/bcprov-jdk15on-164.jar:/home/user/Desktop/Programming/Java/Cryptography/out/production/Cryptography Encrypt "$*"