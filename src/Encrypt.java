//Written by Paul Schakel
//This file is the main class of the EncryptionCli project. It handles the encryption of text.


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;


public class Encrypt {
    static Scanner sc = new Scanner(System.in);
    static String path = "/home/user/Desktop/Programming/Java/Cryptography/src/configuration.conf";

    public static void main(String[] args) {       //this function takes care of user interaction
        Security.addProvider(new BouncyCastleProvider());
        SecretKey sessionKey = genSessionKey();


        String[] flags = {"-d", "-e", "-g", "f", "-h"};
        ArgCheck checker = new ArgCheck(flags);

        try {
            if (!checker.checkIfPresent(args[0])) {
                System.out.println(args[1]);
                System.out.println("E: Argument invalid");
                ArgCheck.printUsage();
                System.exit(1);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("E: No arguments");
            ArgCheck.printUsage();
            System.exit(1);
        }

        if (checker.checkIfHelp(args)) {
            ArgCheck.printUsage();
        }

        ArgCheck.BoolAndPos decryptCheck = checker.checkIfDecrypt(args);
        if (decryptCheck.bool) {
            try {
                String path = args[decryptCheck.pos + 1];
                PrivateKey key = (PrivateKey) getKey(true);
                TextAndKey cryptTextAndKey = getTextAndKey(path, key);
                assert cryptTextAndKey != null;
                SecretKey oldSessionKey = cryptTextAndKey.sessionKey;
                String plainText = decrypt(hexToBytes(cryptTextAndKey.text), oldSessionKey);
                ArgCheck.BoolAndPos fileCheck = checker.checkIfPath(args);
                if (fileCheck.bool) {
                    String filename = args[fileCheck.pos + 1];
                    FileWriter wr = new FileWriter(filename);
                    assert plainText != null;
                    wr.write(plainText);
                    wr.close();
                    System.out.println("Decrypted Text written to file: " + filename);
                } else {
                    System.out.println("Decrypted Text : " + plainText);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("E: Path to .crypt file required");
            } catch (IOException ex) {
                System.out.println("E: Error while writing output to file");
            }
        }

        if (checker.checkIfEncrypt(args)) {
            PublicKey key = (PublicKey) getKey(false);
            String toEncrypt = getPlainText();
            byte[] encrypted = encrypt(toEncrypt, sessionKey);
            ArgCheck.BoolAndPos fileCheck = checker.checkIfPath(args);
            if (fileCheck.bool) {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), sessionKey), key, args[fileCheck.pos + 1]);
                System.out.println("\nSaved encrypted text to file : " + filename);
            } else {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), sessionKey), key, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now()));
                System.out.println("\nSaved encrypted text to file : " + filename);
            }
        }
    }

    public static String getPlainText() {       //receives plain text to encrypt and returns it
        System.out.println("Enter the text you wish to Encrypt : ");
        return sc.nextLine();
    }

    public static byte[] encrypt(String plainText, SecretKey cryptKey) {         //takes the plaintext to encrypt and the secret key and encrypts the text with the key
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.ENCRYPT_MODE, cryptKey);
            return aesCipher.doFinal(plainText.getBytes());
        } catch (Exception ex) {
            System.out.println("E: Error occured during text encryption");
            ex.printStackTrace();
            return null;
        }
    }

    public static byte[] wrapSessionKey(SecretKey keyToWrap, PublicKey cryptKey) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", "BC");
            rsaCipher.init(Cipher.WRAP_MODE, cryptKey);
            return rsaCipher.wrap(keyToWrap);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during sessionkey wrapping");
            ex.printStackTrace();
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
            System.out.println("E: Error occured during decryption");
            ex.printStackTrace();
            return null;
        }
    }

    public static SecretKey unwrapSessionKey(byte[] wrappedSessionKey, PrivateKey cryptKey) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", "BC");
            rsaCipher.init(Cipher.UNWRAP_MODE, cryptKey);
            return (SecretKey) rsaCipher.unwrap(wrappedSessionKey, "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", Cipher.SECRET_KEY);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during sessionkey unwrapping");
            ex.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] hash) {         //makes the encrypted byte array into a hex string
        return DatatypeConverter.printHexBinary(hash);
    }

    private static byte[] hexToBytes(String hex) {         //converts the hex string back to a byte array
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static Key getKey(boolean isPrivate) {         //gets the password which corresponds to the secret key and fetches the key
        System.out.println("Enter the password to your Keystore : ");
        char[] password = sc.nextLine().toCharArray();
        try {
            Properties conf = new Properties();
            conf.load(new FileInputStream(path));
            String ksPath = conf.getProperty("KEYSTORE");
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(ksPath), password);
            if (isPrivate) {
                return ks.getKey("private-key", password);
            } else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(conf.getProperty("PUBKEY")));
                PublicKey pubKey = (PublicKey) ois.readObject();
                ois.close();
                return pubKey;
            }
        } catch (Exception ex) {
            System.out.println("E: Error loading keystore or private key");
            ex.printStackTrace();
            return null;
        }
    }

    public static SecretKey genSessionKey() {
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

    public static String storeTextAndKey(TextAndKey textAndKey, PublicKey pubKey, String filename) {
        String cryptSessionKey = bytesToHex(wrapSessionKey(textAndKey.sessionKey, pubKey));
        //String time = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now());
        Properties conf = new Properties();

        try {
            FileWriter wr = new FileWriter(filename + ".crypt", true);
            BufferedWriter bwr = new BufferedWriter(wr);
            conf.setProperty("TEXT", textAndKey.text);
            conf.setProperty("SESSIONKEY", cryptSessionKey);
            conf.store(bwr, "");
            return filename;
        } catch (IOException ex) {
            System.out.println("E: Error occurred while writing encrypted text to file");
            ex.printStackTrace();
            return null;
        }
    }

    public static TextAndKey getTextAndKey(String path, PrivateKey cryptKey) {
        Properties conf = new Properties();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            conf.load(br);
            String cryptSessionKey = conf.getProperty("SESSIONKEY");
            SecretKey sessionKey = unwrapSessionKey(hexToBytes(cryptSessionKey), cryptKey);
            String cryptText = conf.getProperty("TEXT");
            return new TextAndKey(cryptText, sessionKey);
        } catch (IOException ex) {
            System.out.println("E: Error while getting sessionKey and encrypted text");
            ex.printStackTrace();
            return null;
        }
    }

    public static class TextAndKey {
        public TextAndKey(String newText, SecretKey newSessionKey) {
            text = newText;
            sessionKey = newSessionKey;
        }

        private String text;
        private SecretKey sessionKey;
    }
}
