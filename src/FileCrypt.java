//Written by Paul Schakel
//This class is an extension of TextCrypt which provides support for the encryption of files


import datatypes.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileCrypt extends Crypt {
    public FileCrypt(BoolAndFilename isEncrypt, BoolAndFilename hasFilePath, SecretKey currentSessionKey) {
        super(isEncrypt, hasFilePath);
        Security.addProvider(new BouncyCastleProvider());
        File toEncrypt = new File(isEncrypt.filename);
        if (isEncrypt.bool) {
            PublicKey key = (PublicKey) getKey(false);
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ZipOutputStream zipout = new ZipOutputStream(bos);
                File inputDir = new File(isEncrypt.filename);
                Path inputPath = Paths.get(isEncrypt.filename);
                zipFile(inputDir, inputPath.getFileName().toString(), zipout);
                byte[] bytesToEncrypt = bos.toByteArray();
                zipout.close();
                bos.close();
                byte[] encrypted = encrypt(bytesToEncrypt, currentSessionKey);
                if (hasFilePath.bool) {
                    String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "file/dir"), key, hasFilePath.filename);
                    System.out.println("\nSaved encrypted data to file : " + filename);
                } else {
                    String filename = storeTextAndKey(new TextAndKey(bytesToHex(encrypted), currentSessionKey, "file/dir"), key, toEncrypt.getName() + ".crypt");
                    System.out.println("\nSaved encrypted data to file : " + filename);
                }
            } catch (IOException e) {
                System.out.println("E: Error occurred while compressing the files for encryption");
                System.exit(1);
            }
        }
    }

    public static byte[] encrypt(byte[] plainBytes, SecretKey cryptKey) {
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

    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) {
        try {
            if (fileToZip.isDirectory()) {
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                } else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                }
                zipOut.closeEntry();
                File[] children = fileToZip.listFiles();
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
                return;
            }
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
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

    public static void unzipAndWriteDir(byte[] zipped, String destDir) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(zipped);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(bis));
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                Path newFile = Paths.get(destDir, entry.getName());
                if (!entry.isDirectory()) {
                    unzipFiles(zis, newFile);
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
            while ((read = zinput.read(bytesIn, 0, bytesIn.length)) != -1) {
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
