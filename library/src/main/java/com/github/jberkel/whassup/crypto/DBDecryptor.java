package com.github.jberkel.whassup.crypto;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class DBDecryptor implements Decryptor {
    private static final byte[] ENCRYPTION_KEY = hexStringToByteArray("346a23652a46392b4d73257c67317e352e3372482177652c");
    private static final String CRYPTO_SPEC = "AES";

    public void decryptDB(File input, File output) throws IOException, GeneralSecurityException {
        if (input == null)  throw new IllegalArgumentException("input cannot be null");
        if (output == null) throw new IllegalArgumentException("output cannot be null");

        decryptStream(new FileInputStream(input), new FileOutputStream(output));
    }

    /**
     * @param in encrypted input stream, will be closed automatically
     * @param out the outputstream to write decrypted data to, will be closed automatically
     * @throws GeneralSecurityException
     * @throws IOException
     */
    protected void decryptStream(InputStream in, OutputStream out) throws GeneralSecurityException, IOException {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        CipherInputStream cis = null;
        try {
            cis = new CipherInputStream(in, cipher);
            byte[] buffer = new byte[8192];
            int n;
            while ((n = cis.read(buffer)) != -1)  {
                out.write(buffer, 0, n);
            }
        } finally {
            try {
                if (cis != null) cis.close();
                out.close();
            } catch (IOException ignored) {}
        }
    }

    private void encryptDB(File input, File output) throws IOException, GeneralSecurityException {
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

    private byte[] encrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        return cipher.doFinal(input);
    }

    private static Cipher getCipher(int mode) throws GeneralSecurityException {
        SecretKeySpec keyspec = new SecretKeySpec(ENCRYPTION_KEY, CRYPTO_SPEC);
        Cipher cipher = Cipher.getInstance(CRYPTO_SPEC);
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

        DBDecryptor decryptor = new DBDecryptor();
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
        System.err.println(DBDecryptor.class.getSimpleName()+" "+message);
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

    protected static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
