package me.p4ul5h4k3y.cryptoparrot;

//Written by p4ul5h4k3y
//This class is an extension of Crypt which provides support for the encryption of files. Files and directories are compressed into a zip and then encrypted.
//In addition, this class contains the methods to unzip the decrypted data.


import me.p4ul5h4k3y.cryptoparrot.datatypes.BoolAndFilename;
import me.p4ul5h4k3y.cryptoparrot.datatypes.TextAndKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileCrypt extends Decrypt {

    static BoolAndFilename isEncrypt;
    static BoolAndFilename hasFilePath;
    static SecretKey currentSessionKey;

    public FileCrypt(HashMap<String, Object> args) {
        super(args);                   //send requests for decryption to Crypt (FileCrypt only encrypts files)
        Security.addProvider(new BouncyCastleProvider());

        isEncrypt = (BoolAndFilename) args.get("-f");
        hasFilePath = (BoolAndFilename) args.get("-p");
        currentSessionKey = (SecretKey) args.get("sessionKey");
    }

    public static void main() {
        File toEncrypt = new File(isEncrypt.filename);
        if (isEncrypt.bool) {           //make sure it's supposed to encrypt the data
            PublicKey key = (PublicKey) getKey(false);
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ZipOutputStream zipout = new ZipOutputStream(bos);
                File inputDir = new File(isEncrypt.filename);
                Path inputPath = Paths.get(isEncrypt.filename);
                zipFile(inputDir, inputPath.getFileName().toString(), zipout);      //compress files
                byte[] bytesToEncrypt = bos.toByteArray();      //convert ByteArrayOutputStream which contains compressed files into a ByteArray
                zipout.close();
                bos.close();
                byte[] encrypted = encrypt(bytesToEncrypt, currentSessionKey);      //encrypt ByteArray
                if (hasFilePath.bool) {         //decide where to save the encrypted data
                    String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "file/dir"), key, hasFilePath.filename);     //if user specified where
                    System.out.println("\nSaved encrypted data to file : " + filename);
                } else {
                    String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "file/dir"), key, toEncrypt.getName() + ".crypt");   //generate location: current_dir/name-of-original-file.crypt
                    System.out.println("\nSaved encrypted data to file : " + filename);
                }
            } catch (IOException e) {
                System.out.println("E: Error occurred while compressing the files for encryption");
                System.exit(1);
            }
        }

    }

    public static byte[] encrypt(byte[] plainBytes, SecretKey cryptKey) {       //encrypts the byte[] passed to it
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            aesCipher.init(Cipher.ENCRYPT_MODE, cryptKey);
            return aesCipher.doFinal(plainBytes);
        } catch (Exception ex) {
            System.out.println("E: Error occurred during file encryption");
            System.exit(1);
            return null;
        }
    }

    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) {       //recursively zips the specified files/directories
        try {
            if (fileToZip.isDirectory()) {          //checks if it needs to recursively zip dir
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                } else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                }
                zipOut.closeEntry();
                File[] children = fileToZip.listFiles();
                for (File childFile : children) {           //zips each of the files within the dir
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);       //creates new instance of this function for each item in dir
                }
                return;
            }
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {           //writes the new zipEntry to the ZipOutputStream
                zipOut.write(bytes, 0, length);
            }
            zipOut.closeEntry();        // <--- if you haven't got this little blighter right here then the data in files won't save (at least 10 hrs lost to this guy)
            fis.close();

        } catch (Exception ex) {
            System.out.println("E: Problem occurred while compressing file for encryption");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void unzipAndWriteDir(byte[] zipped, String destDir) {            //unzips already-decrypted data and writes it to the specified destination
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(zipped);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(bis));      //convert byte[] to collection of zipEntries
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                Path newFile = Paths.get(destDir, entry.getName());
                if (!entry.isDirectory()) {
                    unzipFiles(zis, newFile);           //makes sure entry isn't a directory and sends to unzipFiles() to unzip and write to the filesystem
                } else {
                    Files.createDirectories(newFile);
                }
            }
            zis.closeEntry();
            zis.close();
        } catch (EOFException eof) {
            //do nothing
        } catch (FileAlreadyExistsException ex) {
            System.out.println("E: Those files already exist in this directory. If you don't want them, delete them, otherwise decrypt the files elsewhere");
            ex.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println("E: Error while decompressing directory for decryption");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void unzipFiles(final ZipInputStream zinput, final Path unzipPath) {
        try {
            FileOutputStream fos = new FileOutputStream(unzipPath.toAbsolutePath().toString());
            byte[] bytesIn = new byte[1024];
            int read;
            while ((read = zinput.read(bytesIn, 0, bytesIn.length)) != -1) {        //write decompressed file to filesystem
                fos.write(bytesIn, 0, read);
            }
            fos.close();
        } catch (EOFException eof) {
            //do nothing
        } catch (Exception ex) {
            System.out.println("E: Error occurred while writing the decrypted files");
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
