//Written by Paul Schakel
//This file is the main class of the EncryptionCli project. It handles the encryption of text.


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
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class Encrypt {
    static Scanner sc = new Scanner(System.in);
    static String path = "/home/user/Desktop/Programming/Java/Cryptography/src/configuration.conf";

    public static void main(String[] args) {       //this function takes care of user interaction
        Security.addProvider(new BouncyCastleProvider());

        SecretKey sessionKey = genSessionKey();
        System.out.println("Welcome to the Encryption Program");
        while (true) {
            System.out.println("1. Encrypt text \n 2. Decrypt text \n\n Enter Selection : ");
            String option = sc.nextLine();
            if (option.equals("1")) {
                PrivateKey key = (PrivateKey) getKey(true);
                String toEncrypt = getPlainText();
                byte[] encrypted = encrypt(toEncrypt, sessionKey);
                String filename = storeTextAndKey(bytesToHex(encrypted), sessionKey, key);
                System.out.println("\nSaved encrypted text to file : " + filename);
                System.out.println("\n\n1. Encrypt/Decrypt another message\n2. Exit\n\nEnter Selection : ");
                option = sc.nextLine();
                if (option.equals("2")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                } else if (option.equals("1")) {
                    System.out.println("Redirecting...");
                } else {
                    System.out.println("Invalid Option!\nRedirecting...");
                }
            } else if (option.equals("2")) {
                System.out.println("Enter the Encrypted text : ");
                byte[] encryptedText = hexToBytes(sc.nextLine());
                PublicKey key = (PublicKey) getKey(false);
                String plainText = decrypt(encryptedText, key);
                System.out.println("Decrypted Text : " + plainText);
                System.out.println("\n\n1. Encrypt/Decrypt another message\n2. Exit\n\nEnter Selection : ");
                option = sc.nextLine();
                if (option.equals("2")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                } else if (option.equals("1")) {
                    System.out.println("Redirecting...");
                } else {
                    System.out.println("Invalid Option!\nRedirecting...");
                }
            }
        }
    }

    public static String getPlainText() {       //receives plain text to encrypt and returns it
        System.out.println("Enter the text you wish to Encrypt : ");
        String plainText = sc.nextLine();
        return plainText;
    }

    public static byte[] encrypt(String plainText, SecretKey cryptKey) {         //takes the plaintext to encrypt and the secret key and encrypts the text with the key
        try {
            Cipher aesCipher = Cipher.getInstance("AES", "BC");
            aesCipher.init(Cipher.ENCRYPT_MODE, cryptKey);
            byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
            return byteCipherText;
        } catch (Exception ex) {
            System.out.println("E: Error occured during text encryption");
            ex.printStackTrace();
            return null;
        }
    }

    public static byte[] encryptSessionKey(String keyString, PrivateKey cryptKey){
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA", "BC");
            rsaCipher.init(Cipher.ENCRYPT_MODE, cryptKey);
            byte[] cryptSessionKey = rsaCipher.doFinal(keyString.getBytes());
            return cryptSessionKey;
        } catch (Exception ex) {
            System.out.println("E: Error occured during sessionkey encryption");
            ex.printStackTrace();
            return null;
        }
    }

    public static String decrypt(byte[] encryptedText, Key cryptKey) {         //takes the encrypted byte array and decrypts it with the secret key provided
        try {
            Cipher rsaCipher = Cipher.getInstance("AES", "BC");
            rsaCipher.init(Cipher.DECRYPT_MODE, cryptKey);
            byte[] bytePlainText = rsaCipher.doFinal(encryptedText);
            return new String(bytePlainText);
        } catch (Exception ex) {
            System.out.println("E: Error occured during decryption");
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
                PrivateKey privKey = (PrivateKey) ks.getKey("private-key", password);
                return privKey;
            } else {
                PublicKey pubKey = (PublicKey) ks.getKey("public-key", password);
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
            SecretKey sec = gen.generateKey();
            return sec;
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            System.out.println("E: Error while generating session key");
            ex.printStackTrace();
            return null;
        }
    }

    public static String storeTextAndKey(String cryptText, SecretKey sessionKey, PrivateKey privKey) {
        String  cryptSessionKey = bytesToHex(encryptSessionKey(sessionKey.toString(), privKey));
        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now());
        Properties conf = new Properties();

        try {
            FileWriter wr = new FileWriter(time + ".crypt", true);
            BufferedWriter bwr = new BufferedWriter(wr);
            conf.setProperty("TEXT", cryptText);
            conf.setProperty("SESSIONKEY", cryptSessionKey);
            conf.store(bwr, "");
            return time + ".crypt";
        } catch (IOException ex) {
            System.out.println("E: Error occurred while writing encrypted text to file");
            ex.printStackTrace();
            return null;
        }
    }
}
