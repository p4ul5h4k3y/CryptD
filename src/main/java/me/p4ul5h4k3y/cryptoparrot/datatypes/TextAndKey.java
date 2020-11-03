package me.p4ul5h4k3y.cryptoparrot.datatypes;

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