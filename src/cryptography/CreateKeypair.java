package cryptography;

import javax.crypto.SecretKey;
import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Random;
import org.json.simple.JSONObject;


public class CreateKeypair {
    static Random generator = new Random();
    static String path = "/home/user/Desktop/Programming/Java/Cryptography/src/cryptography/KeyAliases.json";

    public static void main(String[] args) {

    }

    public static SecretKey generateKey() throws Exception {        //generates secret key
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        SecretKey secret = generator.generateKey();
        return secret;
    }

    public static String generateString(int length) {       //generates a random string of a specified length
        char[] characters = {'q', 'w', 'e', 'r', 't', 'y', 'u',
                'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j',
                'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '1', '2',
                '3', '4', '5', '6', '7', '8', '9', '0', ' ', 'k'};
        char[] string = new char[length];
        for (int i = 0; i < length; i++) {
            string[i] = characters[generator.nextInt(characters.length)];
        }
        return new String(string);
    }

    public static void storeKey(SecretKey key, String passwordHash) throws IOException {        //creates a file to contain the secret key
        String entryAlias = ".r" + generateString(30);
        File file = new File(entryAlias);
        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(entryAlias));
        try {
            oout.writeObject(key);
        } finally {
            oout.close();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
        writer.write(entryAlias);
        writer.newLine();
        writer.write(passwordHash);
        writer.close();

    }
}
