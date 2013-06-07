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
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DBDecryptorTest {
    private DBDecryptor dbDecryptor;

    @Before
    public void before() throws Exception {
        dbDecryptor = new DBDecryptor();
    }

    @Test
    public void shouldDecryptDatabase() throws Exception {
        File out = File.createTempFile("db-test", ".sql");
        dbDecryptor.decryptDB(Fixtures.TEST_DB_1, out);
        verifyDB(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWithNullInput() throws Exception {
        dbDecryptor.decryptDB(null, new File(("/out")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWithNullOutput() throws Exception {
        dbDecryptor.decryptDB(new File(("/in")), null);
    }

    @Test
    public void shouldDecryptStream() throws Exception {
        File out = File.createTempFile("db-test", ".sql");
        FileOutputStream fos = new FileOutputStream(out);
        dbDecryptor.decryptStream(new FileInputStream(Fixtures.TEST_DB_1), fos);
        verifyDB(out);
    }

    private void verifyDB(File out) {
        assertThat(out).canRead();
        assertThat(out.length()).isGreaterThan(0L);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(out.getAbsolutePath(), null, 0);
        Cursor cursor = db.query("messages", null, null, null, null, null, null);
        assertThat(cursor).isNotNull();
        assertThat(cursor.getCount()).isGreaterThan(0);
        db.close();
    }
}
