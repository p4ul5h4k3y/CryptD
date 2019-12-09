//Written by Paul Schakel
//This class creates and stores the keypair for encryption and saves the path to the public and private keys in configuration.conf


import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class CreateKeypair {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        String path = parseArgs(args, "-p");
        if (path.equals("NO ARGS")) {
            System.out.println("E: Path argument necessary to create keypair");
            System.exit(0);
        }
        System.out.println("Enter the password for keypair generation: ");
        String pass1 = sc.nextLine();
        System.out.println("\nEnter the password again for confirmation : ");
        String pass2 = sc.nextLine();
        if (pass1.equals(pass2)) {
            createKeys(pass1, "/home/user/Desktop/Programming/Java/Cryptography", path);
        } else {
            System.out.println("Passwords do not match!!\nExiting...");
            System.exit(0);
        }
    }


    public static void createKeys(String password, String keyStorePath, String confPath) throws IOException, NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        SecureRandom random = createFixedRandom();
        generator.initialize(2048, random);

        KeyPair pair = generator.generateKeyPair();
        Key pubKey = pair.getPublic();
        Key privKey = pair.getPrivate();

        System.out.println("Public Key (Share this one) -- " + Base64.getEncoder().encodeToString(pubKey.getEncoded()));
        System.out.println("Private Key (Keep this one secret) -- " + Base64.getEncoder().encodeToString(privKey.getEncoded()));

        try {
            X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

            X500Principal dnName = new X500Principal("CN=ROOT");
            Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            Date validityEndDate = new Date(System.currentTimeMillis() + 3 * 365 * 24 * 60 * 60 * 1000);

            certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            certGen.setSubjectDN(dnName);
            certGen.setIssuerDN(dnName);
            certGen.setNotBefore(validityBeginDate);
            certGen.setNotAfter(validityEndDate);
            certGen.setPublicKey((PublicKey) pubKey);
            certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

            X509Certificate rootCertificate = certGen.generate((PrivateKey) privKey, "BC");
            X509Certificate middleCertificate = certGen.generate((PrivateKey) privKey, "BC");

            X509Certificate[] chain = new X509Certificate[2];
            chain[0] = rootCertificate;
            chain[1] = middleCertificate;

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

        Properties config = new Properties();
        FileInputStream fis = new FileInputStream(confPath);
        config.load(fis);
        fis.close();

        FileOutputStream fos = new FileOutputStream(confPath);
        config.setProperty("KEYSTORE", keyStorePath + "/store.jks");
        config.store(fos, "");
        fos.close();
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
}
