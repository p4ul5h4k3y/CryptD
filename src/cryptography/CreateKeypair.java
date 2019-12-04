//Written by Paul Schakel
//This class creates and stores the keypair for encryption and saves the path to the public and private keys in conf.json

package cryptography;

import java.io.*;

import org.json.simple.JSONObject;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import sun.misc.BASE64Encoder;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.swing.*;


public class CreateKeypair {
    static Scanner sc = new Scanner(System.in);
    static String path = "/home/user/Desktop/Programming/Java/Cryptography/src/cryptography/conf.json";

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String pass = sc.nextLine();
        createKeys(pass, "/home/user/Desktop/Programming/Java/Cryptography");
    }

    public static void createKeys(String password, String keyStorePath) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        BASE64Encoder b64 = new BASE64Encoder();

        SecureRandom random = createFixedRandom();
        generator.initialize(2048, random);

        KeyPair pair = generator.generateKeyPair();
        Key pubKey = pair.getPublic();
        Key privKey = pair.getPrivate();

        System.out.println("Public Key (Share this one) -- " + b64.encode(pubKey.getEncoded()));
        System.out.println("Private Key (Keep this one secret) -- " + b64.encode(privKey.getEncoded()));

        try {
            CertAndKeyGen keyGen=new CertAndKeyGen("RSA","SHA1WithRSA",null);
            X509Certificate rootCertificate = keyGen.getSelfCertificate(new X500Name("CN=ROOT"), (long) 365 * 24 * 60 * 60);

            CertAndKeyGen keyGen1=new CertAndKeyGen("RSA","SHA1WithRSA",null);
            X509Certificate middleCertificate = keyGen1.getSelfCertificate(new X500Name("CN=SERVER"), (long) 365 * 24 * 60 * 60);

            X509Certificate[] chain = new X509Certificate[2];
            chain[0]=rootCertificate;
            chain[1]=middleCertificate;

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] pwdChars = password.toCharArray();
            ks.load(null, pwdChars);
            ks.setKeyEntry("private-key", privKey, pwdChars, chain);
            FileOutputStream fos = new FileOutputStream(keyStorePath + "/store.jks");
            ks.store(fos,pwdChars);

        } catch (Exception ex) {
            System.out.println("Something  went wrong while storing the private key.");
            System.out.println(ex);
        }
    }

    public static SecureRandom createFixedRandom() {
        return new FixedRand();
    }

    private static class FixedRand extends SecureRandom {

        MessageDigest sha;
        byte[] state;

        FixedRand() {
            try {
                this.sha = MessageDigest.getInstance("SHA-1");
                this.state = sha.digest();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("can't find SHA-1!");
            }
        }

        public void nextBytes(byte[] bytes) {

            int off = 0;

            sha.update(state);

            while (off < bytes.length) {
                state = sha.digest();

                if (bytes.length - off > state.length) {
                    System.arraycopy(state, 0, bytes, off, state.length);
                } else {
                    System.arraycopy(state, 0, bytes, off, bytes.length - off);
                }

                off += state.length;

                sha.update(state);
            }
        }
    }
}
