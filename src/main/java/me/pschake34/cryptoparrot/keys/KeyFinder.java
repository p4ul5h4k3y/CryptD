package me.pschake34.cryptoparrot.keys;

import me.pschake34.cryptoparrot.CryptoParrot;
import me.pschake34.cryptoparrot.CryptoParrot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

public class KeyFinder {

    public Key quickAccessKey;  //used to easily get a key

    public KeyFinder() {}   //used for a general instantiation of KeyFinder which can be used to generate several keys

    public KeyFinder(boolean isPrivate) {   //used to get the private key only
        if (isPrivate) {
            quickAccessKey = getPrivateKey();
        }
    }

    public KeyFinder(boolean isPrivate, String keyName) {   //used to get the public key, or the private key if you want to
        if (isPrivate) {
            quickAccessKey = getPrivateKey();
        } else {
            quickAccessKey = getPublicKey(keyName);
        }
    }

    public PublicKey getPublicKey(String keyName) {     //gets the specified public key
        Properties keyConfig = new Properties();
        String keyDir = "";

        try {
            Properties generalConfig = new Properties();
            generalConfig.load(new FileInputStream(CryptoParrot.PATH));
            keyDir = generalConfig.getProperty("KEYDIR");

            keyConfig.load(new FileInputStream(keyDir + "keys.conf"));

            if (keyConfig.containsKey(keyName)) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyConfig.getProperty(keyName)));
                PublicKey pubKey = (PublicKey) ois.readObject();        //read PublicKey from storage. Public key is just stored as a java object in a file
                ois.close();
                return pubKey;
            } else {
                System.out.println("E: Specified public key is invalid");
                return null;
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("E: Error occurred while getting public key");
            ex.printStackTrace();
            return null;
        }
    }

    public PrivateKey getPrivateKey() {     //gets the private key
        System.out.println("Enter the password to your Keystore : ");
        char[] password = System.console().readPassword();
        try {
            Properties conf = new Properties();
            conf.load(new FileInputStream(CryptoParrot.PATH));      //grabs the configuration file so that it can find the path to the key storage
            String ksPath = conf.getProperty("KEYSTORE");
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(ksPath), password);
            return (PrivateKey) ks.getKey("private-key", password);      //get the private key from secure KeyStore
        } catch (Exception ex) {
            System.out.println("E: Error loading keystore or private key, your password was probably incorrect");
            System.exit(1);
            return null;
        }
    }
}
