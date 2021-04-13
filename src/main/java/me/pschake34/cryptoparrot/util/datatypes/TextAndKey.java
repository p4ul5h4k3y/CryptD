package me.p4ul5h4k3y.cryptoparrot.util.datatypes;

//Written by p4ul5h4k3y
//This class provides a custom datatype which can store: a String, a SecretKey, and a metadata String.

import javax.crypto.SecretKey;

public class TextAndKey {
    public TextAndKey(String newText, SecretKey newSessionKey, String newMetadata) {
        text = newText;
        sessionKey = newSessionKey;
        metadata = newMetadata;
    }
    public String text;
    public SecretKey sessionKey;
    public String metadata;
}