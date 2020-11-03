#!/usr/bin/bash

#Written by Paul Schakel
#This bash script runs the encryption program TODO: modify so it works on any installation

if [ $1 == "-g" ]; then
    /usr/lib/jvm/java-8-openjdk-amd64/bin/java -classpath /home/user/.jdks/adopt-openjdk-1.8.0_272/jre/lib/ext/bcpkix-jdk15on-164.jar:/home/user/.jdks/adopt-openjdk-1.8.0_272/jre/lib/ext/bcprov-jdk15on-164.jar:/home/user/Desktop/Programming/Java/Cryptography/out/production/Cryptography me.p4ul5h4k3y.cryptoparrot.CreateKeypair $*
else
    /usr/lib/jvm/java-8-openjdk-amd64/bin/java -classpath /home/user/.jdks/adopt-openjdk-1.8.0_272/jre/lib/ext/bcpkix-jdk15on-164.jar:/home/user/.jdks/adopt-openjdk-1.8.0_272/jre/lib/ext/bcprov-jdk15on-164.jar:/home/user/Desktop/Programming/Java/Cryptography/out/production/Cryptography me.p4ul5h4k3y.cryptoparrot.CryptoParrot $*
fi