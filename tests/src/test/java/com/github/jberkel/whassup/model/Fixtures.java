package com.github.jberkel.whassup.model;

import com.github.jberkel.whassup.crypto.DBCrypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Fixtures {
    public static final File TEST_DB_1 =
            new File(DBCrypto.class.getResource("/msgstore.db.crypt").getFile());

    public static final File THUMB_IMAGE =
            new File(DBCrypto.class.getResource("/thumb_image.ser").getFile());


    public static byte[] fileToBytes(File in) throws IOException {
        byte[] buffer = new byte[8192];
        FileInputStream fis = new FileInputStream(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int n;
        while ((n = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, n);
        }
        fis.close();
        return bos.toByteArray();
    }
}
