package me.p4ul5h4k3y.cryptoparrot;

//Written by p4ul5h4k3y
//This class is the parent of TextCrypt and FileCrypt. It contains all of the methods used for the decryption of data, as well as some shared methods for encryption.

import me.p4ul5h4k3y.cryptoparrot.datatypes.BoolAndFilename;
import me.p4ul5h4k3y.cryptoparrot.datatypes.TextAndKey;
import me.p4ul5h4k3y.cryptoparrot.util.Default;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Properties;

public class Decrypt extends Default {

    static BoolAndFilename isEncrypt;
    static BoolAndFilename hasFilePath;

    public Decrypt(HashMap<String, Object> args) {
        isEncrypt = (BoolAndFilename) args.get("-d");
        hasFilePath = (BoolAndFilename) args.get("-p");
    }

    public static void main() {
        if (!isEncrypt.bool) {      //makes sure it is supposed to decrypt (Crypt only handles decryption)
            try {
                PrivateKey key = (PrivateKey) getKey(true);             //gets the private RSA key for data decryption
                TextAndKey cryptTextAndKey = getTextAndKey(isEncrypt.filename, key);    //finds AES key used to encrypt the data
                assert cryptTextAndKey != null;
                SecretKey oldSessionKey = cryptTextAndKey.sessionKey;
                byte[] decryptedData = decrypt(hexToBytes(cryptTextAndKey.text), oldSessionKey);    //decrypts the data with the AES key
                if (!hasFilePath.bool) {
                    hasFilePath.filename = isEncrypt.filename;
                }
                saveDecryptedData(hasFilePath, cryptTextAndKey.metadata, decryptedData);
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("E: Path to .crypt file required");
                System.exit(1);
            }
        }
    }

    public static String bytesToHex(byte[] hash) {         //converts a byte array into a hex string
        return DatatypeConverter.printHexBinary(hash);
    }

    public static byte[] hexToBytes(String hex) {         //converts a hex string back to a byte array
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static Key getKey(boolean isPrivate) {         //gets either the public or the private RSA key from secure storage
        System.out.println("Enter the password to your Keystore : ");
        char[] password = System.console().readPassword();
        try {
            Properties conf = new Properties();
            conf.load(new FileInputStream(CryptoParrot.path));      //grabs the configuration file so that it can find the path to the key storage
            String ksPath = conf.getProperty("KEYSTORE");
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(ksPath), password);
            if (isPrivate) {
                return ks.getKey("private-key", password);      //get the private key from secure KeyStore
            } else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(conf.getProperty("PUBKEY")));
                PublicKey pubKey = (PublicKey) ois.readObject();        //read PublicKey from storage. Public key is just stored as a java object in a file
                ois.close();
                return pubKey;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("E: Error loading keystore or private key");
            System.exit(1);
            return null;
        }
    }


    public static String storeTextAndKey(TextAndKey textAndKey, PublicKey pubKey, String filename) {        //stores encrypted data and current session key in a specified file
        String cryptSessionKey = bytesToHex(wrapSessionKey(textAndKey.sessionKey, pubKey));     //prepare session key for writing to file
        Properties conf = new Properties();

        try {
            FileWriter wr = new FileWriter(filename, true);     // from here down is just writing stuff to the file
            BufferedWriter bwr = new BufferedWriter(wr);
            conf.setProperty("TEXT", textAndKey.text);
            conf.setProperty("SESSIONKEY", cryptSessionKey);
            conf.setProperty("META", textAndKey.metadata);
            conf.store(bwr, "");
            return filename;
        } catch (IOException ex) {
            System.out.println("E: Error occurred while writing encrypted text to file");
            System.exit(1);
            return null;
        }
    }

    public static TextAndKey getTextAndKey(String path, PrivateKey cryptKey) {      //gets the encrypted data and the SecretKey used to encrypt the data
        Properties conf = new Properties();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            conf.load(br);
            String cryptSessionKey = conf.getProperty("SESSIONKEY");
            SecretKey sessionKey = unwrapSessionKey(hexToBytes(cryptSessionKey), cryptKey);     //get SecretKey
            String cryptText = conf.getProperty("TEXT");        //get encrypted data
            String meta = conf.getProperty("META");
            return new TextAndKey(cryptText, sessionKey, meta);
        } catch (IOException ex) {
            System.out.println("E: Error while getting sessionKey and encrypted text (the data you wanted to decrypt wasn't there)");
            System.exit(1);
            return null;
        }
    }

    public static byte[] wrapSessionKey(SecretKey keyToWrap, PublicKey cryptKey) {      //encrypts an AES-256 key with RSA-4096 so that it can be transported
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", "BC");
            rsaCipher.init(Cipher.WRAP_MODE, cryptKey);
            return rsaCipher.wrap(keyToWrap);       //encrypts (or "wraps") the key
        } catch (Exception ex) {
            System.out.println("E: Error occurred during sessionkey wrapping");
            System.exit(1);
            return null;
        }
    }

    public static SecretKey unwrapSessionKey(byte[] wrappedSessionKey, PrivateKey cryptKey) {       //decrypts an AES-256 key which was encrypted with RSA-4096 so that it could be transported
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", "BC");
            rsaCipher.init(Cipher.UNWRAP_MODE, cryptKey);
            return (SecretKey) rsaCipher.unwrap(wrappedSessionKey, "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", Cipher.SECRET_KEY);     //decrypts or "unwraps" the SecretKey
        } catch (Exception ex) {
            System.out.println("E: Error occurred during sessionkey unwrapping");
            System.exit(1);
            return null;
        }
    }

    public static byte[] decrypt(byte[] encryptedFile, SecretKey cryptKey) {         //takes the encrypted byte array and decrypts it with the secret key provided
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.DECRYPT_MODE, cryptKey);
            return aesCipher.doFinal(encryptedFile);        //decrypts the data
        } catch (Exception ex) {
            System.out.println("E: Error occurred during decryption");
            System.exit(1);
            return null;
        }
    }

    public static void saveDecryptedData(BoolAndFilename hasPath, String metadata, byte[] decryptedData) {      //finds out what type of data the decrypted data is, and saves it accordingly
        if (hasPath.bool | metadata.equals("file/dir")) {       // checks if the user has specified where to save the data or whether the data is a file or directory
            if (metadata.equals("text")) {      //writes text to the user-specified location
                try {
                    String plainText = new String(decryptedData);
                    FileWriter wr = new FileWriter(hasPath.filename);
                    wr.write(plainText);
                    wr.close();
                    System.out.println("Decrypted Text written to file: " + hasPath.filename);
                } catch (IOException e) {
                    System.out.println("E: Error while writing decrypted text to file");
                    System.exit(1);
                }
            } else {        //unzips the files and writes them to the correct place
                try {
                    String filename = hasPath.filename;
                    if (!hasPath.bool) {        //generates a filename for the data if user hasn't specified one
                        String[] splitFilename = new File(hasPath.filename).getName().split("\\.(?=[^\\.]+$)");
                        filename = splitFilename[0];
                    }
                    FileCrypt.unzipAndWriteDir(decryptedData, filename);    //sends the data over to FileCrypt to unzup and write
                    System.out.println("Decrypted File saved at: " + filename);
                } catch (Exception e) {
                    System.out.println("E: Error while writing decrypted data");
                    System.exit(1);
                }
            }
        } else {            //writes the decrypted text to the console (doesn't write anything to the filesystem)
            String plaintext = new String(decryptedData);
            System.out.println("Decrypted Text : " + plaintext);
        }
    }
}
