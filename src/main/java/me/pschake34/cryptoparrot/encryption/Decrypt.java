package me.p4ul5h4k3y.cryptoparrot.encryption;

//Written by p4ul5h4k3y
//This class extends Encrypt and provides functionality for decrypting data. It automatically detects what type of data is being decrypted and outputs the decrypted data to the user

import me.p4ul5h4k3y.cryptoparrot.keys.KeyFinder;
import me.p4ul5h4k3y.cryptoparrot.util.datatypes.TextAndKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.PrivateKey;
import java.util.Properties;

public class Decrypt extends Encrypt {


    public Decrypt(String pathToDecrypt, String filenameDestination) {
        PrivateKey key = (PrivateKey) new KeyFinder(true).quickAccessKey;             //gets the private RSA key for data decryption
        TextAndKey cryptTextAndKey = getTextAndKey(pathToDecrypt, key);    //finds AES key used to encrypt the data
        assert cryptTextAndKey != null;
        SecretKey oldSessionKey = cryptTextAndKey.sessionKey;
        byte[] decryptedData = decrypt(hexToBytes(cryptTextAndKey.text), oldSessionKey);    //decrypts the data with the AES key

        boolean pathSpecified;
        if (filenameDestination.equals("NOT SPECIFIED")) {
            pathSpecified = false;
        } else {
            pathSpecified = true;        }
        saveDecryptedData(pathSpecified, filenameDestination, cryptTextAndKey.metadata, decryptedData);
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

    public static void saveDecryptedData(boolean hasPath, String path, String metadata, byte[] decryptedData) {      //finds out what type of data the decrypted data is, and saves it accordingly
        if (hasPath | metadata.equals("file/dir")) {       // checks if the user has specified where to save the data or whether the data is a file or directory
            if (metadata.equals("text")) {      //writes text to the user-specified location
                try {
                    String plainText = new String(decryptedData);
                    FileWriter wr = new FileWriter(path);
                    wr.write(plainText);
                    wr.close();
                    System.out.println("Decrypted Text written to file: " + path);
                } catch (IOException e) {
                    System.out.println("E: Error while writing decrypted text to file");
                    System.exit(1);
                }
            } else {        //unzips the files and writes them to the correct place
                try {
                    String filename = path;
                    if (!hasPath) {        //generates a filename for the data if user hasn't specified one
                        String[] splitFilename = new File(path).getName().split("\\.(?=[^\\.]+$)");
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
