//Written by Paul Schakel
//This class creates and stores the keypair for encryption and saves the path to the public and private keys in configuration.conf


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
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
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;


public class CreateKeypair {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());

        String path = parseArgs(args, "-g");
        if (path.equals("NO ARGS")) {
            System.out.println("E: Path argument necessary to create keypair");
            System.exit(1);
        }
        String pass1 = getPassword();
        genAndStoreKeys(pass1, "/home/user/Desktop/Programming/Java/Cryptography", path);
    }


    public static void genAndStoreKeys(String password, String keyStorePath, String confPath) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(4096);

        KeyPair pair = generator.generateKeyPair();
        PublicKey pubKey = pair.getPublic();
        PrivateKey privKey = pair.getPrivate();

        System.out.println("Public and Private keys have been generated successfully");

        try {
            X509Certificate[] chain = genCertChain(pubKey, privKey);
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
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(keyStorePath + "/key.pub"));
            oout.writeObject(pubKey);
            oout.close();
        } catch (IOException ex) {
            System.out.println("E: Error occured while storing the public key");
            ex.printStackTrace();
        }


        try {
            Properties config = new Properties();
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

    public static X509Certificate[] genCertChain(PublicKey pubKey, PrivateKey privKey) {
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

    public static String parseArgs(String[] args, String flag) {
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

    public static String getPassword() {        //gets the password which corresponds to the secret key
        while (true) {
            System.out.println("Enter a password for key generation : ");
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
}
