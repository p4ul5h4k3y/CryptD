//Written by Paul Schakel
//This class creates and stores the keypair for encryption and saves the path to the public and private keys in TODO: set default


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;


public class CreateKeypair {
    static Scanner sc = new Scanner(System.in);     //used to read the password from stdin

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());       //needed for key generation

        String path = parseArgs(args, "-g");        //gets the path to the configuration.conf file TODO: update with defaults
        if (path.equals("NO ARGS")) {
            System.out.println("E: Path argument necessary to create keypair");
            System.exit(1);
        }
        String pass1 = getPassword();
        genAndStoreKeys(pass1, "/home/user/Desktop/Programming/Java/Cryptography", path);   //TODO: remove hard-coded path
    }


    public static void genAndStoreKeys(String password, String keyStorePath, String confPath) throws NoSuchAlgorithmException, NoSuchProviderException {

        /* This code will generate an RSA-4096 key pair and store them in a java keystore (private)
        * and a key.pub file (public). Then it will save the paths to key.pub and store.jks in configuration.conf
        * TODO: probably need a better name for configuration.conf, and congruency in error checking (i.e. to printStackTrace or not)*/

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(4096);

        KeyPair pair = generator.generateKeyPair();
        PublicKey pubKey = pair.getPublic();
        PrivateKey privKey = pair.getPrivate();

        System.out.println("Public and Private keys have been generated successfully");

        try {
            X509Certificate[] chain = genCertChain(pubKey, privKey);                //this stuff stores the private key in a secure keystore
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] pwdChars = password.toCharArray();
            ks.load(null, pwdChars);
            ks.setKeyEntry("private-key", privKey, pwdChars, chain);
            FileOutputStream fos = new FileOutputStream(keyStorePath + "/store.jks");
            ks.store(fos, pwdChars);

        } catch (Exception ex) {
            System.out.println("Something  went wrong while storing the private key.");
            ex.printStackTrace();
        }

        try {
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(keyStorePath + "/key.pub"));      //and this stuff saves the public key to a plain file
            oout.writeObject(pubKey);
            oout.close();
        } catch (IOException ex) {
            System.out.println("E: Error occured while storing the public key");
            ex.printStackTrace();
        }


        try {
            Properties config = new Properties();                                   //and this stuff writes the paths into configuration.conf TODO: still need better name
            FileInputStream fis = new FileInputStream(confPath);
            config.load(fis);
            fis.close();

            FileOutputStream fos = new FileOutputStream(confPath);
            config.setProperty("KEYSTORE", keyStorePath + "/store.jks");
            config.setProperty("PUBKEY", keyStorePath + "/key.pub");
            config.store(fos, "");
            fos.close();
        } catch (IOException ex) {
            System.out.println("E: Error occured while storing new config");
        }
    }

    public static X509Certificate[] genCertChain(PublicKey pubKey, PrivateKey privKey) {        //creates the certificates for the keypair. TODO: set up with a cert authority
        try {
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pubKey.getEncoded());
            X500Name dnName = new X500Name("CN=ROOT");
            Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            Date validityEndDate = new Date(System.currentTimeMillis() + 3 * 365 * 24 * 60 * 60 * 1000);

            X509v1CertificateBuilder certBuilder = new X509v1CertificateBuilder(
                    dnName,
                    BigInteger.valueOf(System.currentTimeMillis()),
                    validityBeginDate,
                    validityEndDate,
                    dnName,
                    subPubKeyInfo
            );

            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256WithRSAEncryption");
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            ContentSigner signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(privKey.getEncoded()));

            X509CertificateHolder holder = certBuilder.build(signer);

            X509Certificate rootCertificate = new JcaX509CertificateConverter().getCertificate(holder);
            X509Certificate middleCertificate = new JcaX509CertificateConverter().getCertificate(holder);

            X509Certificate[] chain = new X509Certificate[2];
            chain[0] = rootCertificate;
            chain[1] = middleCertificate;

            return chain;
        } catch (Exception ex) {
            System.out.println("E: Error occurred during certificate generation");
            ex.printStackTrace();
            return null;
        }
    }

    public static String parseArgs(String[] args, String flag) {        //basically just gets the arg after the specified flag
        int i = 0;
        for (String arg : args) {
            if (arg.equals(flag)) {
                return args[i + 1];
            } else {
                i++;
            }
        }
        return "NO ARGS";
    }

    public static String getPassword() {        //prompts the user to enter a password to use for the locking of the keyStore. TODO: probably need to require a better password
        String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        String[] specialChars = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "~", "`", "{", "}", "[", "]", "|", ":", ";", "'", "\"", "<", ">", ",", ".", "?", "/", "-", "_", "+", "="};
        String[] CAPITALS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        while (true) {
            System.out.println("Enter a password for key generation : ");
            String input1 = new String(System.console().readPassword());
            System.out.println("Enter the same password for confirmation : ");
            String input2 = new String(System.console().readPassword());
            if (input1.equals(input2)) {
                if (input1.length() < 8) {
                    System.out.println("Enter a longer password.");
                } else if (checkIfCharInString(input1, CAPITALS)) {
                    System.out.println("Your password contains no capital letters. Please capitalize some of your letters and try again.");
                } else if (checkIfCharInString(input1, nums)) {
                    System.out.println("Your password contains no numbers. Please add at least one more number and try again.");
                } else if (checkIfCharInString(input1, specialChars)) {
                    System.out.println("Your password contains no special characters. Please add at least one special character and try again.");
                } else {
                    System.out.println("Password Confirmed");
                    return input1;
                }
            } else {
                System.out.println("Passwords did not match. Try Again.\n");
            }
        }
    }

    public static boolean checkIfCharInString(String toCheck, String[] list) {

        for (String s : list) {
            if (toCheck.contains(s)) {
                return false;
            }
        }
        return true;
    }
}
