//Written by Paul Schakel
//This file is the main class of the CryptD project. It handles the options and executes code accordingly


import datatypes.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.util.Scanner;


public class CryptD {
    static Scanner sc = new Scanner(System.in);
    static String path = "/home/user/Desktop/Programming/Java/Cryptography/src/configuration.conf";

    public static void main(String[] args) {       //this function takes care of user interaction
        Security.addProvider(new BouncyCastleProvider());
        SecretKey sessionKey = genSessionKey();

        String[] flags = {"-d", "-e", "-g", "f", "-h"};
        ArgCheck checker = new ArgCheck(flags);
        BoolAndPos encryptCheck = checker.checkIfEncrypt(args);
        BoolAndPos fileCheck = checker.checkIfPath(args);

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
            System.exit(0);
        }

        new TextCrypt(new BoolAndFilename(encryptCheck.bool, args[encryptCheck.pos + 1]), new BoolAndFilename(fileCheck.bool, args[fileCheck.pos + 1]), sessionKey);
    }

    public static byte[] wrapSessionKey(SecretKey keyToWrap, PublicKey cryptKey) {      //encrpyts the AES-256 key with RSA-4096 for transport
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

    public static SecretKey unwrapSessionKey(byte[] wrappedSessionKey, PrivateKey cryptKey) {       //unencrypts the AES-256 key which is encrypted with RSA-4096 for transport
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


    public static SecretKey genSessionKey() {       //generates the AES-256 bit key which encrypts the text
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