//Written by Paul Schakel
//This class is the parent of TextCrypt and FileCrypt and handles the decryption of data


import datatypes.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

public class Crypt {
    public Crypt(BoolAndFilename isEncrypt, BoolAndFilename hasFilePath) {
        if (!isEncrypt.bool) {
            try {
                PrivateKey key = (PrivateKey) getKey(true);             //gets the RSA key for data decryption
                TextAndKey cryptTextAndKey = TextCrypt.getTextAndKey(isEncrypt.filename, key);
                assert cryptTextAndKey != null;
                SecretKey oldSessionKey = cryptTextAndKey.sessionKey;
                byte[] decryptedData = decrypt(hexToBytes(cryptTextAndKey.text), oldSessionKey);
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

    public static Key getKey(boolean isPrivate) {         //gets the password which corresponds to the secret key and fetches the key
        System.out.println("Enter the password to your Keystore : ");
        char[] password = System.console().readPassword();
        try {
            Properties conf = new Properties();
            conf.load(new FileInputStream(CryptD.path));
            String ksPath = conf.getProperty("KEYSTORE");
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(ksPath), password);
            if (isPrivate) {
                return ks.getKey("private-key", password);
            } else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(conf.getProperty("PUBKEY")));
                PublicKey pubKey = (PublicKey) ois.readObject();
                ois.close();
                return pubKey;
            }
        } catch (Exception ex) {
            System.out.println("E: Error loading keystore or private key");
            System.exit(1);
            return null;
        }
    }


    public static String storeTextAndKey(TextAndKey textAndKey, PublicKey pubKey, String filename) {
        String cryptSessionKey = bytesToHex(wrapSessionKey(textAndKey.sessionKey, pubKey));
        Properties conf = new Properties();

        try {
            FileWriter wr = new FileWriter(filename, true);
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

    public static TextAndKey getTextAndKey(String path, PrivateKey cryptKey) {
        Properties conf = new Properties();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            conf.load(br);
            String cryptSessionKey = conf.getProperty("SESSIONKEY");
            SecretKey sessionKey = unwrapSessionKey(hexToBytes(cryptSessionKey), cryptKey);
            String cryptText = conf.getProperty("TEXT");
            String meta = conf.getProperty("META");
            return new TextAndKey(cryptText, sessionKey, meta);
        } catch (IOException ex) {
            System.out.println("E: Error while getting sessionKey and encrypted text (the data you wanted to decrypt wasn't there)");
            System.exit(1);
            return null;
        }
    }

    public static byte[] wrapSessionKey(SecretKey keyToWrap, PublicKey cryptKey) {      //encrpyts the AES-256 key with RSA-4096 for transport
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", "BC");
            rsaCipher.init(Cipher.WRAP_MODE, cryptKey);
            return rsaCipher.wrap(keyToWrap);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during sessionkey wrapping");
            System.exit(1);
            return null;
        }
    }

    public static SecretKey unwrapSessionKey(byte[] wrappedSessionKey, PrivateKey cryptKey) {       //decrypts the AES-256 key which is encrypted with RSA-4096 for transport
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", "BC");
            rsaCipher.init(Cipher.UNWRAP_MODE, cryptKey);
            return (SecretKey) rsaCipher.unwrap(wrappedSessionKey, "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING", Cipher.SECRET_KEY);
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
            return aesCipher.doFinal(encryptedFile);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during decryption");
            System.exit(1);
            return null;
        }
    }

    public static void saveDecryptedData(BoolAndFilename hasPath, String metadata, byte[] decryptedData) {
        if (hasPath.bool | metadata.equals("file/dir")) {
            if (metadata.equals("text")) {
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
            } else {
                try {
                    String filename = hasPath.filename;
                    if (!hasPath.bool) {
                        String[] splitFilename = new File(hasPath.filename).getName().split("\\.(?=[^\\.]+$)");
                        filename = splitFilename[0];
                    }
                    FileCrypt.unzipAndWriteDir(decryptedData, filename);
                    System.out.println("Decrypted File saved at: " + filename);
                } catch (Exception e) {
                    System.out.println("E: Error while writing decrypted data");
                    System.exit(1);
                }
            }
        } else {
            String plaintext = new String(decryptedData);
            System.out.println("Decrypted Text : " + plaintext);
        }
    }
}
