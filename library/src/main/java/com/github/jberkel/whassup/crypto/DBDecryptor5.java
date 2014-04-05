package com.github.jberkel.whassup.crypto;

import android.text.TextUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.github.jberkel.whassup.crypto.DBDecryptor.hexStringToByteArray;

/**
 *
 * Implementation from thread in
 * <a href="http://www.securitybydefault.com/2014/03/descifrando-msgstoredbcrypt5-la-nueva.html">
 *  DESCIFRANDO MSGSTORE.DB.CRYPT5, LA NUEVA BASE DE DATOS DE WHATSAPP
 * </a>
 */
public class DBDecryptor5 implements Decryptor {
    private static final byte[] INITIALIZATION_VECTOR = hexStringToByteArray("1e39f369e90db33aa73b442bbbb6b0b9");
    private static final byte[] ENCRYPTION_KEY = hexStringToByteArray("8d4b155cc9ff81e5cbf6fa7819366a3ec621a656416cd793");

    private final String email;

    public DBDecryptor5(String email) {
        if (TextUtils.isEmpty(email)) {
            throw new IllegalArgumentException("email is empty");
        }
        this.email = email;
    }


    public void decryptDB(File input, File output) throws GeneralSecurityException, IOException {
        Cipher cipher = getCipher(email);

        CipherInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new CipherInputStream(new FileInputStream(input), cipher);
            outStream = new FileOutputStream(output);

            byte[] buffer = new byte[8192];
            int n;
            while ((n = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, n);
            }
        } finally {
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
        }
    }

    private Cipher getCipher(String email) throws GeneralSecurityException {
        final String emailMD5 = md5(email);
        final byte[] emailMD5Bytes = hexStringToByteArray(emailMD5 + emailMD5);

        final byte[] decryptionKey = new byte[24];
        System.arraycopy(ENCRYPTION_KEY, 0, decryptionKey, 0, decryptionKey.length);

        for (int i = 0; i < decryptionKey.length; i++) {
            decryptionKey[i] ^= emailMD5Bytes[i & 0xF];
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,
            new SecretKeySpec(decryptionKey, "AES"),
            new IvParameterSpec(INITIALIZATION_VECTOR));
        return cipher;
    }


    private static String md5(String md5) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(md5.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }
}
