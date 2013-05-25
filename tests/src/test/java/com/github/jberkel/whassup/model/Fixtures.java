package com.github.jberkel.whassup.model;

import com.github.jberkel.whassup.crypto.DBCrypto;

import java.io.File;

public class Fixtures {
    public static final File TEST_DB_1 =
            new File(DBCrypto.class.getResource("/msgstore.db.crypt").getFile());
}
