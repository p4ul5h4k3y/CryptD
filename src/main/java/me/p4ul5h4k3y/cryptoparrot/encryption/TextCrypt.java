package me.p4ul5h4k3y.cryptoparrot;

//Written by p4ul5h4k3y
//Extends Crypt and contains the methods used for encrypting text.

import me.p4ul5h4k3y.cryptoparrot.datatypes.BoolAndFilename;
import me.p4ul5h4k3y.cryptoparrot.datatypes.TextAndKey;
import me.p4ul5h4k3y.cryptoparrot.util.Default;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class TextCrypt extends Decrypt {

    static BoolAndFilename isEncrypt;
    static BoolAndFilename hasFilePath;
    static SecretKey currentSessionKey;

    public TextCrypt(HashMap<String, Object> args) {
        super(args);      //sends requests for decryption to Crypt (TextCrypt only encrypts plaintext)
        Security.addProvider(new BouncyCastleProvider());

        isEncrypt = (BoolAndFilename) args.get("-t");
        hasFilePath = (BoolAndFilename) args.get("-p");
        currentSessionKey = (SecretKey) args.get("sessionKey");
    }

    public static void main() {
        if (isEncrypt.bool) {       //makes sure it's supposed to be encrypting
            PublicKey key = (PublicKey) getKey(false);      //gets public key
            String toEncrypt = TextCrypt.getPlainText();            //gets text to encrypt
            byte[] encrypted = TextCrypt.encrypt(toEncrypt, currentSessionKey);         //encrypts text
            if (hasFilePath.bool) {         //figures out where to save the encrypted text
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "text"), key, hasFilePath.filename);         //user specified
                System.out.println("\nSaved encrypted text to file : " + filename);
            } else {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "text"), key, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now()) + ".crypt");        //generates location: current_dir/timestamp.crypt
                System.out.println("\nSaved encrypted text to file : " + filename);
            }
        }
    }

    public static String getPlainText() {       //reads plain text from the console and returns it
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
