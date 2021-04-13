package me.pschake34.cryptoparrot;

//Written by p4ul5h4k3y
//This file is the main class of the CryptoParrot project. It handles the options and executes code accordingly


import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import me.pschake34.cryptoparrot.keys.Export;
import me.pschake34.cryptoparrot.keys.Import;
import me.pschake34.cryptoparrot.encryption.Decrypt;
import me.pschake34.cryptoparrot.encryption.FileCrypt;
import me.pschake34.cryptoparrot.encryption.TextCrypt;
import me.pschake34.cryptoparrot.keys.KeyFinder;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


public class CryptoParrot {

    public static String PATH = "/home/user/Desktop/Programming/Java/CryptoParrot/configuration.conf";     //path to config file -- TODO: need to specify default

    public static void main(String[] args) {       //this method checks arguments and passes them to the proper place

        //create option checker
        OptionParser parser = new OptionParser();
        parser.acceptsAll(Arrays.asList("d", "decrypt")).withRequiredArg();  //decrypt
        parser.acceptsAll(Arrays.asList("e", "encrypt"));    //encrypt
        parser.acceptsAll(Arrays.asList("r", "recipient")).withRequiredArg();
        parser.acceptsAll(Arrays.asList("t", "text")).withOptionalArg();    //specifically encrypt text
        parser.acceptsAll(Arrays.asList("f", "file", "files", "dir", "directory")).withRequiredArg();  //specifically encrypt a file/dir
        parser.acceptsAll(Arrays.asList("g", "gen-keypair"));    //generate keypair
        parser.acceptsAll(Arrays.asList("p", "path")).withRequiredArg();  //set path for saving message
        parser.acceptsAll(Arrays.asList("i", "import")).withRequiredArg();  //import a public key
        parser.accepts("name").withRequiredArg();   //set nickname for public key being imported
        parser.acceptsAll(Arrays.asList("x", "export")).withRequiredArg();  //export your public key
        parser.acceptsAll(Arrays.asList("h", "help")).forHelp();

        KeyFinder x = new KeyFinder(false, "self");

        try {
            OptionSet options = parser.parse(args);

            if (options.has("e")) {     //handle encrypt options

                PublicKey encryptionKey;
                KeyFinder kf = new KeyFinder();
                if (options.has("r")) {     //figure out which public key to use
                    encryptionKey = kf.getPublicKey((String) options.valueOf("r"));
                } else {
                    encryptionKey = kf.getPublicKey("self");
                }

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

                    new TextCrypt(encryptText, encryptDestination, encryptionKey);
                }

                if (options.has("f")) {     //encrypt file/dir
                    String encryptPath = (String) options.valueOf("f");

                    if (!options.has("p")) {
                        encryptDestination = "NOT SPECIFIED";
                    }

                    new FileCrypt(encryptPath, encryptDestination, encryptionKey);
                }

                if (!options.has("f") && !options.has("t")) {   //display warning message
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
            System.out.println("\nE: Invalid option or option additional option was required");
            printUsage();
        }
    }

    public static void printUsage() {
        System.out.println("Usage: encrypt [OPTION] [ARGS] \n" +
                "      -d, --decrypt=FILEPATH            decrypt any data\n" +
                "      -e, --encrypt                     set mode to encrypt (requires additional options)\n" +
                "      -t, --text=(TEXT)                 encrypt text\n" +
                "      -f, --file=PATH                   encrypt a specified file or directory\n" +
                "      -g, --gen-keypair                 generate keypair for encryption\n" +
                "      -p, --path=PATH                   set path for saving message to\n" +
                "                                        (default is current-dir/date_time) when encrypting\n" +
                "                                        when decrypting it saves the decrypted message to the file\n" +
                "      -i, --import=FILEPATH             imports somebody else's public\n" +
                "                                        key from a specified file and prompts\n" +
                "                                        the user to name the new contact\n" +
                "          --name=NICKNAME               set the name for the new contact\n" +
                "      -x, --export=PATH                     exports your public key and saves it to\n" +
                "                                        a specified filename so you can share it with others\n" +
                "      -h, --help                        display this info and exit\n\n" +
                "If an argument is in parentheses it is optional.");
    }
}