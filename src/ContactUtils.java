//Written by Paul Schakel
//This class provides tools for both exporting your public key and importing somebody else's public key.
//Other people's public keys are stored in a file in the config directory and are saved with whatever nickname you give the key TODO: make config dir

import datatypes.*;
import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class ContactUtils {
    static String contactPath = "/home/user/Desktop/Programming/Java/Cryptography/src/contacts/";

    public ContactUtils(BoolAndFilename exportCheck, String newContactName) {
        if (exportCheck.bool) {
            exportPubKey(exportCheck.filename);
        } else {
            if (newContactName.equals("NO NAME")) {
                Scanner sc = new Scanner(System.in);
                while (true) {
                    System.out.println("Enter a nickname for the new contact:");
                    String name = sc.nextLine();
                    System.out.println("\nThe nickname for your new contact is : " + name);
                    System.out.println("Are you satisfied with this name? Y/n");
                    String option = sc.nextLine();
                    if (!option.toLowerCase().equals("n")) {
                        importPubKey(exportCheck.filename, name);
                        break;
                    }
                }
            } else {
                importPubKey(exportCheck.filename, newContactName);
            }
        }
    }

    public void exportPubKey(String outFilename) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream(CryptoParrot.path));

            InputStream in = new BufferedInputStream(new FileInputStream(config.getProperty("PUBKEY")));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename));
            byte[] buffer = new byte[1024];
            int len;

            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
                out.flush();
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importPubKey(String importDest, String name) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream(contactPath + "contacts.conf"));

            InputStream in  = new BufferedInputStream(new FileInputStream(importDest));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(contactPath + name));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
                out.flush();
            }
            in.close();
            out.close();

            config.setProperty(name, contactPath + name);
            out = new BufferedOutputStream(new FileOutputStream(contactPath + "contacts.conf"));
            config.store(out, "");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}