package com.github.jberkel.whassup.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

public class DBCrypto {
    private static final String HEXKEY = "346a23652a46392b4d73257c67317e352e3372482177652c";

    public void decryptDB(File input, File output) throws IOException, GeneralSecurityException {
        if (input == null)  throw new IllegalArgumentException("input cannot be null");
        if (output == null) throw new IllegalArgumentException("output cannot be null");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            byte[] byteOut = decrypt(readStream(new FileInputStream(input)));
            fos.write(byteOut);
        } finally {
            if (fos != null) fos.close();
        }
    }

    public void encryptDB(File input, File output) throws IOException, GeneralSecurityException {
        if (input == null ) throw new IllegalArgumentException("input cannot be null");
        if (output == null) throw new IllegalArgumentException("output cannot be null");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            fos.write(encrypt(readStream(new FileInputStream(input))));
        } finally {
            if (fos != null) fos.close();
        }
    }

    private byte[] decrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        return cipher.doFinal(input);
    }

    private byte[] encrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        return cipher.doFinal(input);
    }

    private static Cipher getCipher(int mode) throws GeneralSecurityException {
        SecretKeySpec keyspec = new SecretKeySpec(new BigInteger(HEXKEY, 16).toByteArray(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, keyspec);
        return cipher;
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        if (args.length < 2) {
            error("[decrypt|encrypt] <input>");
        }
        final String mode = args[0];
        final File input  = new File(args[1]);
        if (!input.exists()) {
            error("file "+input+ " does not exist");
        }
        File output = new File(input.getName()+"."+mode+".sqlite");

        DBCrypto decryptor = new DBCrypto();
        if ("encrypt".equals(mode)) {
            decryptor.encryptDB(input, output);
            System.out.println("Encrypted to "+output);
        } else if ("decrypt".equals(mode)) {
            decryptor.decryptDB(input, output);
            System.out.println("Decrypted to "+output);
        } else {
            error("unknown mode "+mode);
        }
    }

    private static void error(String message) {
        System.err.println(DBCrypto.class.getSimpleName()+" "+message);
        System.exit(1);
    }

    private static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        try {
            while ((n = is.read(buffer)) != -1) {
                bos.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return bos.toByteArray();
    }
}
