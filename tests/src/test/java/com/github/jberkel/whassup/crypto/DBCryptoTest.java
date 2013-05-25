package com.github.jberkel.whassup.crypto;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.jberkel.whassup.model.Fixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DBCryptoTest {
    private DBCrypto dbCrypto;

    @Before
    public void before() throws Exception {
        dbCrypto = new DBCrypto();
    }

    @Test
    public void shouldDecryptDatabase() throws Exception {
        File out = File.createTempFile("db-test", ".sql");
        dbCrypto.decryptDB(Fixtures.TEST_DB_1, out);
        verifyDB(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWithNullInput() throws Exception {
        dbCrypto.decryptDB(null, new File(("/out")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWithNullOutput() throws Exception {
        dbCrypto.decryptDB(new File(("/in")), null);
    }

    private void verifyDB(File out) {
        assertThat(out.exists()).isTrue();
        assertThat(out.length()).isGreaterThan(0);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(out.getAbsolutePath(), null, 0);
        Cursor cursor = db.query("messages", null, null, null, null, null, null);
        assertThat(cursor).isNotNull();
        assertThat(cursor.getCount()).isGreaterThan(0);
        db.close();
    }
}
