package main.java;//Written by Paul Schakel
//This contains the code used for encrypting and decrypting text

import datatypes.*;
import main.java.datatypes.BoolAndFilename;
import main.java.datatypes.TextAndKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextCrypt extends Crypt {
    public TextCrypt(BoolAndFilename isEncrypt, BoolAndFilename hasFilePath, SecretKey currentSessionKey) {
        super(isEncrypt, hasFilePath);
        Security.addProvider(new BouncyCastleProvider());

        if (isEncrypt.bool) {
            PublicKey key = (PublicKey) getKey(false);
            String toEncrypt = TextCrypt.getPlainText();
            byte[] encrypted = TextCrypt.encrypt(toEncrypt, currentSessionKey);
            if (hasFilePath.bool) {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "text"), key, hasFilePath.filename);
                System.out.println("\nSaved encrypted text to file : " + filename);
            } else {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "text"), key, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now()) + ".crypt");
                System.out.println("\nSaved encrypted text to file : " + filename);
            }
        }
    }

    public static String getPlainText() {       //receives plain text to encrypt and returns it
        System.out.println("Enter the text you wish to Encrypt : ");
        return System.console().readLine();
    }

    public static byte[] encrypt(String plainText, SecretKey cryptKey) {         //takes the plaintext to encrypt and the secret key and encrypts the text with the key
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.ENCRYPT_MODE, cryptKey);
            return aesCipher.doFinal(plainText.getBytes());
        } catch (Exception ex) {
            System.out.println("E: Error occurred during text encryption");
            System.exit(1);
            return null;
        }
    }
}
