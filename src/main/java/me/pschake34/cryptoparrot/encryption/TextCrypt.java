package me.p4ul5h4k3y.cryptoparrot.encryption;

//Written by p4ul5h4k3y
//Extends Encrypt and provides functionality for encrypting text.

import me.p4ul5h4k3y.cryptoparrot.util.datatypes.TextAndKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.*;


public class TextCrypt extends Encrypt {

    public TextCrypt(String toEncrypt, String filenameDestination, PublicKey encryptionKey) {
        Security.addProvider(new BouncyCastleProvider());

        byte[] encrypted = TextCrypt.encrypt(toEncrypt, currentSessionKey);         //encrypts text
        storeDataAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "text"), encryptionKey, filenameDestination);   //save encrypted data
        System.out.println("\nSaved encrypted text to file : " + filenameDestination);
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
