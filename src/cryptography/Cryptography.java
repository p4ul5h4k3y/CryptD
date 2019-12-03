//Written by Paul Schakel
//This file is the main class of the EncryptionCli project

package cryptography;


import com.sun.istack.internal.NotNull;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import org.json.simple.JSONObject;


public class Cryptography {
    static Scanner sc = new Scanner(System.in);
    static String path = "/home/user/De  sktop/Programming/Java/Cryptography/src/cryptography/KeyAliases.json";

    public static void main(String[] args) throws Exception {       //this function takes care of user interaction
        System.out.println("Welcome to the Encryption Program");
        while (true) {
            System.out.println("1. Encrypt text \n 2. Decrypt text \n\n Enter Selection : ");
            String option = sc.nextLine();
            if (option.equals("1")) {
                //SecretKey key = generateKey();
                String password = getHash(getPassword());
                //storeKey(key, password);
                String toEncrypt = getPlainText();
                //byte[] encrypted = encrypt(toEncrypt, key);
                //String hexKey = bytesToHex(key.getEncoded());
                //System.out.println("Secret Key : " + hexKey + "\nEncypted Text : " + bytesToHex(encrypted));
                System.out.println("\n\n1. Encrypt/Decrypt another message\n2. Exit\n\nEnter Selection : ");
                option = sc.nextLine();
                if (option.equals("2")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                else if (option.equals("1")){
                    System.out.println("Redirecting...");
                }
                else {
                    System.out.println("Invalid Option!\nRedirecting...");
                }
            } else if (option.equals("2")) {
                System.out.println("Enter the Encrypted text : ");
                byte[] encryptedText = hexToBytes(sc.nextLine());
                SecretKey key = getKey();
                String plainText = decrypt(encryptedText, key);
                System.out.println("Decrypted Text : " + plainText);
                System.out.println("\n\n1. Encrypt/Decrypt another message\n2. Exit\n\nEnter Selection : ");
                option = sc.nextLine();
                if (option.equals("2")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                else if (option.equals("1")){
                    System.out.println("Redirecting...");
                }
                else {
                    System.out.println("Invalid Option!\nRedirecting...");
                }
            }
        }
    }

    public static String getPlainText() {       //receives plain text to encrypt and returns it
        System.out.println("Enter the text you wish to Encrypt : ");
        String plainText = sc.nextLine();
        return plainText;
    }

    public static byte[] encrypt(String plainText, SecretKey secKey) throws Exception {         //takes the plaintext to encrypt and the secret key and encrypts the text with the key
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
        return byteCipherText;
    }

    public static String decrypt(byte[] encryptedText, SecretKey secKey) throws Exception {         //takes the encrypted byte array and decrypts it with the secret key provided
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, secKey);
        byte[] bytePlainText = aesCipher.doFinal(encryptedText);
        return new String(bytePlainText);
    }

    private static String bytesToHex(byte[] hash) {         //makes the encrypted byte array into a hex string
        return DatatypeConverter.printHexBinary(hash);
    }

    private static byte[] hexToBytes(String hex){         //converts the hex string back to a byte array
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return bytes;
    }

    public static String  getPassword(){        //gets the password which corresponds to the secret key
        while (true) {
            System.out.println("Enter the password for the key generation : ");
            String input1 = sc.nextLine();
            System.out.println("Enter the same password for confirmation : ");
            String input2 = sc.nextLine();
            if (input1.equals(input2)) {
                if (input1.length() < 8) {
                    System.out.println("Enter a longer password.");
                } else {
                    System.out.println("Password Confirmed");
                    return input1;
                }
            } else {
                System.out.println("Passwords did not match. Try Again.\n");
            }
        }
    }

    public static SecretKey getKey() throws IOException, ClassNotFoundException, NoSuchAlgorithmException{         //gets the password which corresponds to the secret key and fetches the key
        System.out.println("Enter the password to the SecretKey that encrypted this message : ");
        String password = getHash(sc.nextLine());
        BufferedReader br = new BufferedReader(new FileReader(path));
        ArrayList<String> passesKeys = new ArrayList<>();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                passesKeys.add(line);
            }

        } finally {
            br.close();
        }
        int index = findString(passesKeys, password);
        String entryAlias = passesKeys.get(index);
        FileInputStream fis = new FileInputStream(entryAlias);
        ObjectInputStream ois = new ObjectInputStream(fis);
        SecretKey secKey = (SecretKey) ois.readObject();
        ois.close();
        fis.close();
        return secKey;
    }

    public static int findString(@NotNull ArrayList<String> array , String toFindHash){      //takes an array of strings, finds the specified one and returns it
        for (int i = 0; i < array.size(); i++){
            if (toFindHash.equals(array.get(i))){
                return i - 1;
            }
        }
        System.out.println("Find Key Failed. Wrong Password. Exiting...");
        System.exit(0);
        return 0;
    }

    public static String getHash(String toHash) throws NoSuchAlgorithmException{        //creates a SHA1 hash of a string
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] bytes = toHash.getBytes();
        md.update(bytes);
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }
}
