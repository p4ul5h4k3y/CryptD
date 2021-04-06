package me.p4ul5h4k3y.cryptoparrot.contacts;


import me.p4ul5h4k3y.cryptoparrot.CryptoParrot;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.*;
import java.util.Properties;

public class Export {

    public Export(String exportTo) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream(CryptoParrot.PATH));        //loads the global config to find where the public key is stored

            InputStream in = new BufferedInputStream(new FileInputStream(config.getProperty("PUBKEY")));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(exportTo));
            byte[] buffer = new byte[1024];
            int len;

            while ((len = in.read(buffer)) > 0) {       //writes the file
                out.write(buffer, 0, len);
                out.flush();
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
