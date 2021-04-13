package me.p4ul5h4k3y.cryptoparrot.encryption;

import me.p4ul5h4k3y.cryptoparrot.util.datatypes.TextAndKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.util.Properties;

public class Encrypt {

    final SecretKey currentSessionKey = genSessionKey();    //AES-256 key

    public static String bytesToHex(byte[] bytes) {         //converts a byte array into a hex string
        StringBuilder result = new StringBuilder();
        for (byte currentByte : bytes) {
            result.append(String.format("%02X", currentByte));
        }
        return result.toString();
    }

    public static byte[] hexToBytes(String hex) {         //converts a hex string back to a byte array
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static void storeDataAndKey(TextAndKey textAndKey, PublicKey pubKey, String filename) {        //stores encrypted data and current session key in a specified file
        String cryptSessionKey = bytesToHex(wrapSessionKey(textAndKey.sessionKey, pubKey));     //prepare session key for writing to file
        Properties conf = new Properties();

        try {
            FileWriter wr = new FileWriter(filename, true);     // from here down is just writing stuff to the file
            BufferedWriter bwr = new BufferedWriter(wr);
            conf.setProperty("TEXT", textAndKey.text);
            conf.setProperty("SESSIONKEY", cryptSessionKey);
            conf.setProperty("META", textAndKey.metadata);
            conf.store(bwr, "");
        } catch (IOException ex) {
            System.out.println("E: Error occurred while writing encrypted text to file");
            System.exit(1);
        }
    }

    public static byte[] wrapSessionKey(SecretKey keyToWrap, PublicKey cryptKey) {      //encrypts an AES-256 key with RSA-4096 so that it can be transported
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", "BC");
            rsaCipher.init(Cipher.WRAP_MODE, cryptKey);
            return rsaCipher.wrap(keyToWrap);       //encrypts (or "wraps") the key
        } catch (Exception ex) {
            System.out.println("E: Error occurred during sessionkey wrapping");
            System.exit(1);
            return null;
        }
    }

    public static SecretKey genSessionKey() {
        /* This method generates the javax.crypto.SecretKey which is
         * encrypted by the RSA key before the encrypted data is saved */

        Security.addProvider(new BouncyCastleProvider());       //needed for key generation

        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES", "BC");
            gen.init(256);
            return gen.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            System.out.println("E: Error while generating session key");
            ex.printStackTrace();
            return null;
        }
    }
}
