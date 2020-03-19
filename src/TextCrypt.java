//Written by Paul Schakel
//This contains the code used for encrypting and decrypting text

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class TextCrypt {
    public TextCrypt(BoolAndFilename isEncrypt, BoolAndFilename hasFilePath, SecretKey currentSessionKey) {
        Security.addProvider(new BouncyCastleProvider());

        if (isEncrypt.bool) {
            PublicKey key = (PublicKey) getKey(false);
            String toEncrypt = TextCrypt.getPlainText();
            byte[] encrypted = TextCrypt.encrypt(toEncrypt, currentSessionKey);
            if (hasFilePath.bool) {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey), key, hasFilePath.filename);
                System.out.println("\nSaved encrypted text to file : " + filename);
            } else {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey), key, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now()));
                System.out.println("\nSaved encrypted text to file : " + filename);
            }
        } else {
            try {
                PrivateKey key = (PrivateKey) getKey(true);
                TextAndKey cryptTextAndKey = TextCrypt.getTextAndKey(isEncrypt.filename, key);
                assert cryptTextAndKey != null;
                SecretKey oldSessionKey = cryptTextAndKey.sessionKey;
                String plainText = decrypt(TextCrypt.hexToBytes(cryptTextAndKey.text), oldSessionKey);
                if (hasFilePath.bool) {
                    FileWriter wr = new FileWriter(hasFilePath.filename);
                    assert plainText != null;
                    wr.write(plainText);
                    wr.close();
                    System.out.println("Decrypted Text written to file: " + hasFilePath.filename);
                } else {
                    System.out.println("Decrypted Text : " + plainText);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("E: Path to .crypt file required");
            } catch (IOException ex) {
                System.out.println("E: Error while writing output to file");
            }
        }

    }

    public static String getPlainText() {       //receives plain text to encrypt and returns it
        System.out.println("Enter the text you wish to Encrypt : ");
        return CryptD.sc.nextLine();
    }

    public static byte[] encrypt(String plainText, SecretKey cryptKey) {         //takes the plaintext to encrypt and the secret key and encrypts the text with the key
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.ENCRYPT_MODE, cryptKey);
            return aesCipher.doFinal(plainText.getBytes());
        } catch (Exception ex) {
            System.out.println("E: Error occurred during text encryption");
            ex.printStackTrace();
            return null;
        }
    }

    public static String decrypt(byte[] encryptedText, SecretKey cryptKey) {         //takes the encrypted byte array and decrypts it with the secret key provided
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.DECRYPT_MODE, cryptKey);
            byte[] bytePlainText = aesCipher.doFinal(encryptedText);
            return new String(bytePlainText);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during decryption");
            ex.printStackTrace();
            return null;
        }
    }

    public static String bytesToHex(byte[] hash) {         //makes the encrypted byte array into a hex string
        return DatatypeConverter.printHexBinary(hash);
    }

    public static byte[] hexToBytes(String hex) {         //converts the hex string back to a byte array
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static Key getKey(boolean isPrivate) {         //gets the password which corresponds to the secret key and fetches the key
        System.out.println("Enter the password to your Keystore : ");
        char[] password = CryptD.sc.nextLine().toCharArray();
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
            ex.printStackTrace();
            return null;
        }
    }


    public static String storeTextAndKey(TextAndKey textAndKey, PublicKey pubKey, String filename) {
        String cryptSessionKey = bytesToHex(CryptD.wrapSessionKey(textAndKey.sessionKey, pubKey));
        Properties conf = new Properties();

        try {
            FileWriter wr = new FileWriter(filename + ".crypt", true);
            BufferedWriter bwr = new BufferedWriter(wr);
            conf.setProperty("TEXT", textAndKey.text);
            conf.setProperty("SESSIONKEY", cryptSessionKey);
            conf.store(bwr, "");
            return filename;
        } catch (IOException ex) {
            System.out.println("E: Error occurred while writing encrypted text to file");
            ex.printStackTrace();
            return null;
        }
    }

    public static TextAndKey getTextAndKey(String path, PrivateKey cryptKey) {
        Properties conf = new Properties();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            conf.load(br);
            String cryptSessionKey = conf.getProperty("SESSIONKEY");
            SecretKey sessionKey = CryptD.unwrapSessionKey(hexToBytes(cryptSessionKey), cryptKey);
            String cryptText = conf.getProperty("TEXT");
            return new TextAndKey(cryptText, sessionKey);
        } catch (IOException ex) {
            System.out.println("E: Error while getting sessionKey and encrypted text");
            ex.printStackTrace();
            return null;
        }
    }
}
