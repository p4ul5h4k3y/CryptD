//Written by Paul Schakel
//This file is the main class of the CryptoParrot project. It handles the options and executes code accordingly


import datatypes.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.util.HashMap;


public class CryptoParrot {
    static String path = "/home/user/Desktop/Programming/Java/Cryptography/src/configuration.conf";     //path to config file -- TODO: need to specify default

    public static void main(String[] args) {       //this method checks arguments and passes them to the proper place
        Security.addProvider(new BouncyCastleProvider());
        SecretKey sessionKey = genSessionKey();     //this is generated now and passed to TextCrypt and FileCrypt as arguments

        HashMap<String, Boolean> flags = new HashMap<String, Boolean>()
        {{
            put("-d", true);
            put("-e", false);
            put("-g", false);
            put("-p", true);
            put("-h", false);
            put("-f", true);
            put("-x", true);
            put("--name", true);
            put("-i", true);
        }};

        ArgCheck checker = new ArgCheck(flags, args);
        HashMap<String, BoolAndPos> checkedArgs = checker.returnCheckedArgs();

        if (checkedArgs.get("-h").bool) {
            ArgCheck.printUsage();
            System.exit(0);
        }

        if (checkedArgs.get("-e").bool | checkedArgs.get("-d").bool) {       //creates an instance of FileCrypt if true or TextCrypt if false
            if (checkedArgs.get("-p").bool) {
                if (checkedArgs.get("-f").bool) {
                    new FileCrypt(new BoolAndFilename(checkedArgs.get("-e").bool, args[checkedArgs.get("-f").pos]), new BoolAndFilename(checkedArgs.get("-p").bool, args[checkedArgs.get("-p").pos]), sessionKey);
                } else {
                    new TextCrypt(new BoolAndFilename(checkedArgs.get("-e").bool, args[checkedArgs.get("-d").pos]), new BoolAndFilename(checkedArgs.get("-p").bool, args[checkedArgs.get("-p").pos]), sessionKey);
                }
            } else if (checkedArgs.get("-f").bool) {
                new FileCrypt(new BoolAndFilename(checkedArgs.get("-e").bool, args[checkedArgs.get("-f").pos]), new BoolAndFilename(checkedArgs.get("-p").bool, "NO PATH"), sessionKey);
            } else {
                new TextCrypt(new BoolAndFilename(checkedArgs.get("-e").bool, args[checkedArgs.get("-d").pos]), new BoolAndFilename(checkedArgs.get("-p").bool, "NO PATH"), sessionKey);
            }
        }

        if (checkedArgs.get("-i").bool | checkedArgs.get("-x").bool) {
            if (checkedArgs.get("-i").bool) {
                if (checkedArgs.get("--name").bool) {
                    new ContactUtils(new BoolAndFilename(checkedArgs.get("-x").bool, args[checkedArgs.get("-i").pos]), args[checkedArgs.get("--name").pos]);
                } else {
                    new ContactUtils(new BoolAndFilename(checkedArgs.get("-x").bool, args[checkedArgs.get("-i").pos]), "NO NAME");
                }
            } else {
                new ContactUtils(new BoolAndFilename(checkedArgs.get("-x").bool, args[checkedArgs.get("-x").pos]), "NO NAME");
            }
        }
    }

    public static SecretKey genSessionKey() {
        /* This method generates the javax.crypto.SecretKey which is
        * encrypted by the RSA key before the encrypted data is saved */
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