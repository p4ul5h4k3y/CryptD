#!/usr/bin/bash

#Written by Paul Schakel
#This bash script runs the encryption program

if [ $1 == "-g" ]; then
    /usr/lib/jvm/java-8-openjdk-amd64/bin/java -classpath /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/bcprov-jdk15on-164.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/bcpkix-jdk15on-164.jar:/home/user/Desktop/Programming/Java/Cryptography/out/production/Cryptography CreateKeypair $*
else
    /usr/lib/jvm/java-8-openjdk-amd64/bin/java -classpath /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/bcprov-jdk15on-164.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/bcpkix-jdk15on-164.jar:/home/user/Desktop/Programming/Java/Cryptography/out/production/Cryptography CryptD $*
fi