package com.github.jberkel.whassup.crypto;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface Decryptor {
    void decryptDB(File input, File output) throws IOException, GeneralSecurityException;
}
