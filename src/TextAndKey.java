import javax.crypto.SecretKey;

public class TextAndKey {
    public TextAndKey(String newText, SecretKey newSessionKey) {
        text = newText;
        sessionKey = newSessionKey;
    }
    public String text;
    public SecretKey sessionKey;
}