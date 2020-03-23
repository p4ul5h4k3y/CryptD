//Written by Paul Schakel
//This contains the code used for encrypting and decrypting text

import datatypes.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextCrypt extends Crypt {
    public TextCrypt(BoolAndFilename isEncrypt, BoolAndFilename hasFilePath, SecretKey currentSessionKey) {
        Security.addProvider(new BouncyCastleProvider());

        if (isEncrypt.bool) {
            PublicKey key = (PublicKey) getKey(false);
            String toEncrypt = TextCrypt.getPlainText();
            byte[] encrypted = TextCrypt.encrypt(toEncrypt, currentSessionKey);
            if (hasFilePath.bool) {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey), key, hasFilePath.filename);
                System.out.println("\nSaved encrypted text to file : " + filename);
            } else {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey), key, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now()) + ".crypt");
                System.out.println("\nSaved encrypted text to file : " + filename);
            }
        } else {
            try {
                PrivateKey key = (PrivateKey) getKey(true);
                TextAndKey cryptTextAndKey = TextCrypt.getTextAndKey(isEncrypt.filename, key);
                assert cryptTextAndKey != null;
                SecretKey oldSessionKey = cryptTextAndKey.sessionKey;
                String plainText = decrypt(TextCrypt.hexToBytes(cryptTextAndKey.text), oldSessionKey);
                if (hasFilePath.bool) {
                    FileWriter wr = new FileWriter(hasFilePath.filename);
                    assert plainText != null;
                    wr.write(plainText);
                    wr.close();
                    System.out.println("Decrypted Text written to file: " + hasFilePath.filename);
                } else {
                    System.out.println("Decrypted Text : " + plainText);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("E: Path to .crypt file required");
                System.exit(1);
            } catch (IOException ex) {
                System.out.println("E: Error while writing output to file");
                System.exit(1);
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

    public static String decrypt(byte[] encryptedText, SecretKey cryptKey) {         //takes the encrypted byte array and decrypts it with the secret key provided
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.DECRYPT_MODE, cryptKey);
            byte[] bytePlainText = aesCipher.doFinal(encryptedText);
            return new String(bytePlainText);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during decryption");
            System.exit(1);
            return null;
        }
    }
}
