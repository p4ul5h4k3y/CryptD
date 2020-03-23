//Written by Paul Schakel
//This class is an extension of TextCrypt which provides support for the encryption of files


import datatypes.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.security.*;

public class FileCrypt extends Crypt {
    public FileCrypt(BoolAndFilename isEncrypt, BoolAndFilename hasFilePath, SecretKey currentSessionKey) {
        Security.addProvider(new BouncyCastleProvider());

        if (isEncrypt.bool) {
            PublicKey key = (PublicKey) getKey(false);
            File toEncrypt = new File(isEncrypt.filename);
            byte[] encrypted = encrypt(toEncrypt, currentSessionKey);
            if (hasFilePath.bool) {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey), key, hasFilePath.filename);
                System.out.println("\nSaved encrypted text to file : " + filename);
            } else {
                String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey), key, toEncrypt.getName() + ".crypt");
                System.out.println("\nSaved encrypted text to file : " + filename);
            }
        } else {
            try {
                PrivateKey key = (PrivateKey) getKey(true);
                TextAndKey cryptTextAndKey = TextCrypt.getTextAndKey(isEncrypt.filename, key);
                assert cryptTextAndKey != null;
                SecretKey oldSessionKey = cryptTextAndKey.sessionKey;
                byte[] plainFileBytes = decrypt(TextCrypt.hexToBytes(cryptTextAndKey.text), oldSessionKey);
                if (hasFilePath.bool) {
                    FileOutputStream fos = new FileOutputStream(hasFilePath.filename);
                    assert plainFileBytes != null;
                    fos.write(plainFileBytes);
                    fos.close();
                    System.out.println("Decrypted File saved at: " + hasFilePath.filename);
                } else {
                    String[] filename = new File(isEncrypt.filename).getName().split("\\.(?=[^\\.]+$)");
                    FileOutputStream fos = new FileOutputStream(filename[0]);
                    assert plainFileBytes != null;
                    fos.write(plainFileBytes);
                    fos.close();
                    System.out.println("Decrypted File saved at: " + filename[0]);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("E: Path to .crypt file required");
                System.exit(1);
            } catch (IOException ex) {
                System.out.println("E: Error while writing output to file");
                System.exit(1);
            }
        }
    }

    public static byte[] encrypt(File plainFile, SecretKey cryptKey) {
        try {
            byte[] fileContent = Files.readAllBytes(plainFile.toPath());

            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.ENCRYPT_MODE, cryptKey);
            return aesCipher.doFinal(fileContent);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during file encryption");
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
}
