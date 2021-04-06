package me.p4ul5h4k3y.cryptoparrot.encryption;

import me.p4ul5h4k3y.cryptoparrot.CryptoParrot;
import me.p4ul5h4k3y.cryptoparrot.util.datatypes.TextAndKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Properties;

public class Encrypt {

    public static Key getKey(boolean isPrivate) {         //gets either the public or the private RSA key from secure storage
        System.out.println("Enter the password to your Keystore : ");
        char[] password = System.console().readPassword();
        try {
            Properties conf = new Properties();
            conf.load(new FileInputStream(CryptoParrot.PATH));      //grabs the configuration file so that it can find the path to the key storage
            String ksPath = conf.getProperty("KEYSTORE");
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(ksPath), password);
            if (isPrivate) {
                return ks.getKey("private-key", password);      //get the private key from secure KeyStore
            } else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(conf.getProperty("PUBKEY")));
                PublicKey pubKey = (PublicKey) ois.readObject();        //read PublicKey from storage. Public key is just stored as a java object in a file
                ois.close();
                return pubKey;
            }
        } catch (Exception ex) {
            System.out.println("E: Error loading keystore or private key, your password was probably incorrect");
            System.exit(1);
            return null;
        }
    }

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

    public static void storeTextAndKey(TextAndKey textAndKey, PublicKey pubKey, String filename) {        //stores encrypted data and current session key in a specified file
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

}
