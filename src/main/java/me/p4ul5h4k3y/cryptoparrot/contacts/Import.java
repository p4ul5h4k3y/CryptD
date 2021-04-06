package me.p4ul5h4k3y.cryptoparrot.contacts;

import java.io.*;
import java.util.Properties;

public class Import {

    static String contactPath = "/home/user/Desktop/Programming/Java/Cryptography/contacts/";

    public Import(String importFrom, String nickname) {
        if (nickname.equals("NOT SPECIFIED")) {     //gets name from user if not specified
            while (true) {
                System.out.println("Enter a nickname for the new contact:");
                String name = System.console().readLine();
                System.out.println("\nThe nickname for your new contact is : " + name);
                System.out.println("Are you satisfied with this name? Y/n");
                String option = System.console().readLine();
                if (!option.equalsIgnoreCase("n")) {
                    importPubKey(importFrom, name);
                    break;
                }
            }
        } else {
            importPubKey(importFrom, nickname);
        }
    }

    public void importPubKey(String importDest, String name) {       //imports somebody else's public key from a specified destination, saving the key with a specified nickname
        try {
            Properties config = new Properties();
            config.load(new FileInputStream(contactPath + "contacts.conf"));    //gets the contact list

            InputStream in = new BufferedInputStream(new FileInputStream(importDest));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(contactPath + name));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {       //write the key to the filesystem
                out.write(buffer, 0, len);
                out.flush();
            }
            in.close();
            out.close();

            config.setProperty(name, contactPath + name);       //save the contact to the contact list
            out = new BufferedOutputStream(new FileOutputStream(contactPath + "contacts.conf"));
            config.store(out, "");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
