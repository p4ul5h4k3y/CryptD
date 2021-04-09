package me.p4ul5h4k3y.cryptoparrot;

//Written by p4ul5h4k3y
//This file is the main class of the CryptoParrot project. It handles the options and executes code accordingly


import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import me.p4ul5h4k3y.cryptoparrot.contacts.Export;
import me.p4ul5h4k3y.cryptoparrot.contacts.Import;
import me.p4ul5h4k3y.cryptoparrot.encryption.Decrypt;
import me.p4ul5h4k3y.cryptoparrot.encryption.FileCrypt;
import me.p4ul5h4k3y.cryptoparrot.encryption.TextCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class CryptoParrot {

    public static String PATH = "/home/user/Desktop/Programming/Java/Cryptography/src/main/java/me/p4ul5h4k3y/cryptoparrot/configuration.conf";     //path to config file -- TODO: need to specify default

    public static void main(String[] args) {       //this method checks arguments and passes them to the proper place
        Security.addProvider(new BouncyCastleProvider());
        SecretKey sessionKey = genSessionKey();     //this is generated now and passed to TextCrypt and FileCrypt as arguments

        //create option checker
        OptionParser parser = new OptionParser();
        parser.accepts("d").withRequiredArg();  //decrypt
        parser.accepts("e");    //encrypt
        parser.accepts("t").withOptionalArg();    //specifically encrypt text
        parser.accepts("f").withRequiredArg();  //specifically encrypt a file/dir
        parser.accepts("g");    //generate keypair
        parser.accepts("p").withRequiredArg();  //set path for saving message
        parser.accepts("i").withRequiredArg();  //import a public key
        parser.accepts("name").withRequiredArg();   //set nickname for public key being imported
        parser.accepts("x").withRequiredArg();  //export your public key
        parser.accepts("h").forHelp();

        try {
            OptionSet options = parser.parse(args);

            if (options.has("e")) {     //handle encrypt options
                String encryptDestination;
                if (options.has("p")) {
                    encryptDestination = (String) options.valueOf("p");
                } else {
                    encryptDestination = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now()) + ".crypt";
                }

                if (options.has("t")) {     //encrypt text
                    String encryptText;
                    if (options.hasArgument("t")) {
                        encryptText = (String) options.valueOf("t");
                    } else {
                        System.out.println("Enter the text you wish to Encrypt : ");
                        encryptText = System.console().readLine();
                    }

                    new TextCrypt(encryptText, encryptDestination, sessionKey);
                }

                if (options.has("f")) {     //encrypt file/dir
                    String encryptPath = (String) options.valueOf("f");

                    if (!options.has("p")) {
                        encryptDestination = "NOT SPECIFIED";
                    }

                    new FileCrypt(encryptPath, encryptDestination, sessionKey);
                }

                if (!options.has("f") || !options.has("t")) {   //display warning message
                    System.out.println("No options specified for encryption.\nUse -t to encrypt text and -f to encrypt a file or directory.\nSee help (-h) for more details");
                }
            } else if (options.has("d")) {  //handle decrypt options
                String toDecrypt = (String) options.valueOf("d");
                String decryptDestination;
                if (options.has("p")) {     //checks if user specified output path
                    decryptDestination = (String) options.valueOf("p");
                } else {
                    decryptDestination = "NOT SPECIFIED";
                }

                new Decrypt(toDecrypt, decryptDestination);
            } else if (options.has("g")) {      //generates a new RSA keypair
                new CreateKeypair();
            } else if (options.has("i")) {      //handle options for importing public keys
                String name;
                if (options.has("name")) {
                    name = (String) options.valueOf("name");
                } else {
                    name = "NOT SPECIFIED";
                }

                new Import((String) options.valueOf("i"), name);
            } else if (options.has("x")) {  //handle exporting public key
                new Export((String) options.valueOf("x"));
            } else if (options.has("h")) {
                printUsage();
            }
        } catch (OptionException ex) {
            System.out.println("\nE: Unrecognized Option");
            printUsage();
        }
    }

    public static void printUsage() {
        System.out.println("Usage: encrypt [OPTION] [ARGS] \n" +
                "      -d [path-to-encrypted-data]       decrypt any data\n" +
                "      -t                                encrypt text\n" +
                "      -i [path-to-public-key]           imports somebody else's public\n" +
                "                                        key from a specified file and prompts\n" +
                "                                        the user to name the new contact\n" +
                "           --name [nickname-for contact]      set the name for the new contact\n" +
                "      -x [filename]                     exports your public key and saves it to\n" +
                "                                        a specified filename so you can share it with others\n" +
                "      -f [path-to-file]                 encrypt a specified file or directory\n" +
                "      -g                                generate keypair for encryption\n" +
                "      -p [path-to-file]                 set path for saving message to\n" +
                "                                        (default is current-dir/date_time) when encrypting\n" +
                "                                        when decrypting it saves the decrypted message to the file\n" +
                "      -h                                display this info and exit\n");
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