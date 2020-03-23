//Written by Paul Schakel
//This file is the main class of the CryptD project. It handles the options and executes code accordingly


import datatypes.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;


public class CryptD {
    static String path = "/home/user/Desktop/Programming/Java/Cryptography/src/configuration.conf";

    public static void main(String[] args) {       //this function takes care of user interaction
        Security.addProvider(new BouncyCastleProvider());
        SecretKey sessionKey = genSessionKey();

        String[] flags = {"-d", "-e", "-g", "-p", "-h"};
        ArgCheck checker = new ArgCheck(flags);
        BoolAndPos encryptCheck = checker.checkIfEncrypt(args);
        BoolAndPos pathCheck = checker.checkIfPath(args);
        BoolAndPos fileCheck = checker.checkIfFile(args);

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

        if (fileCheck.bool) {
            new FileCrypt(new BoolAndFilename(encryptCheck.bool, args[fileCheck.pos + 1]), new BoolAndFilename(pathCheck.bool, args[pathCheck.pos + 1]), sessionKey);
        } else {
            new TextCrypt(new BoolAndFilename(encryptCheck.bool, args[encryptCheck.pos + 1]), new BoolAndFilename(pathCheck.bool, args[pathCheck.pos + 1]), sessionKey);
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