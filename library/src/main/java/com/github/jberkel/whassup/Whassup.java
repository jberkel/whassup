package com.github.jberkel.whassup;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.os.Environment;
import android.util.Log;
import com.github.jberkel.whassup.crypto.DBDecryptor;
import com.github.jberkel.whassup.model.WhatsAppMessage;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Whassup {
    private static final String TAG = Whassup.class.getSimpleName();

    private static final File DB_PATH = new File(Environment.getExternalStorageDirectory(),
            "Whatsapp/Databases");

    private static final String CURRENT_DB = "msgstore.db.crypt";

    private final DBDecryptor dbDecryptor;
    private final DBProvider  dbProvider;
    private final DBOpener    dbOpener;

    /**
     * Default constructor, tries to automatically find the appropriate db file on
     * the SD card.
     */
    public Whassup() {
        this(new DBDecryptor(), new DefaultDBProvider(), new DBOpener());
    }

    /**
     * @param file path to an encrypted DB file
     */
    public Whassup(final File file) {
        this(new DBDecryptor(), new DBProvider() {
            @Override
            public File getDBFile() {
                return file;
            }
        }, new DBOpener());
    }

    /* package */ Whassup(DBDecryptor decryptor, DBProvider dbProvider, DBOpener dbOpener) {
        this.dbDecryptor = decryptor;
        this.dbProvider = dbProvider;
        this.dbOpener = dbOpener;
    }

    /**
     * @return a cursor with all available messages
     * @throws IOException
     */
    public Cursor queryMessages() throws IOException {
        return queryMessages(0, -1);
    }

    /**
     * @param timestamp a timestamp, epoch format
     * @param max how many messages to fetch or -1 for all
     * @return a cursor with messages after timestamp
     * @throws IOException
     */
    public Cursor queryMessages(long timestamp, int max) throws IOException {
        File currentDB = dbProvider.getDBFile();
        if (currentDB == null) {
            return null;
        } else {
            return getCursorFromDB(decryptDB(currentDB), timestamp, max);
        }
    }

    /**
     * Convenience method which reads all messages and converts them into model objects.
     * @param timestamp fetch all message since timestamp
     * @param max how many messages to fetch, -1 for all
     * @return a list of messages
     * @throws IOException
     */
    public List<WhatsAppMessage> getMessages(long timestamp, int max) throws IOException {
        Cursor cursor = queryMessages(timestamp, max);
        try {
            if (cursor != null) {
                List<WhatsAppMessage> messages = new ArrayList<WhatsAppMessage>(cursor.getCount());
                while (cursor.moveToNext()) {
                    messages.add(new WhatsAppMessage(cursor));
                }
                return messages;
            } else {
                return Collections.emptyList();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<WhatsAppMessage> getMessages() throws IOException {
        return getMessages(0, -1);
    }

    /**
     * @return if there is a whatsapp backup available
     */
    public boolean hasBackupDB() {
        return dbProvider.getDBFile() != null;
    }

    private Cursor getCursorFromDB(final File dbFile, long since, int max) throws IOException {
        Log.d(TAG, "using DB "+dbFile);
        SQLiteDatabase db = getSqLiteDatabase(dbFile);
        String limit = null;
        String selection = null;
        String[] selectionArgs = null;
        if (since > 0) {
            selection = String.format("%s > ?", WhatsAppMessage.Fields.TIMESTAMP);
            selectionArgs = new String[]{String.valueOf(since)};
        }
        if (max > 0) {
            limit = String.valueOf(max);
        }
        final String orderBy = WhatsAppMessage.Fields.TIMESTAMP + " ASC";

        try {
            return db.query(WhatsAppMessage.TABLE, null, selection, selectionArgs, null, null, orderBy, limit);
        } catch (SQLiteException e) {
            Log.w(TAG, "error querying DB", e);
            throw new IOException("Error querying DB: "+e.getMessage());
        }
    }

    private SQLiteDatabase getSqLiteDatabase(final File dbFile) throws IOException {
        try {
            return dbOpener.openDatabase(dbFile);
        } catch (SQLiteException e) {
            Log.w(TAG, "error opening db "+dbFile, e);
            throw new IOException("Error opening database:"+e.getMessage());
        }
    }

    private File decryptDB(File in) throws IOException {
        File out = File.createTempFile("decrypted-db", ".sqlite");
        try {
            dbDecryptor.decryptDB(in, out);
            return out;
        } catch (GeneralSecurityException e) {
            Log.w(TAG, e);
            throw new IOException("Could not decrypt db: "+e.getMessage());
        }
    }

    public static class DefaultDBProvider implements DBProvider {
        @Override
        public File getDBFile() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File db = new File(DB_PATH, CURRENT_DB);
                if (db.exists()) {
                    if (db.canRead()) {
                        return db;
                    } else {
                        Log.d(TAG, "db "+db+" exists but is not readable");
                        return null;
                    }
                } else {
                    Log.d(TAG, "db "+db+" does not exist");
                    return null;
                }
            } else {
                Log.w(TAG, "external storage not mounted");
                return null;
            }
        }
    }
    /* package */ static class DBOpener {
        public SQLiteDatabase openDatabase(final File dbFile) {
            return SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), new SQLiteDatabase.CursorFactory() {
                @Override
                @SuppressWarnings("deprecation")
                public Cursor newCursor(final SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
                    return new SQLiteCursor(db, driver, editTable, query) {
                        @Override
                        public void close() {
                            Log.d(TAG, "closing cursor");
                            super.close();
                            db.close();
                            if (!dbFile.delete()) {
                                Log.w(TAG, "could not delete database " + dbFile);
                            }
                        }
                    };
                }
            }, SQLiteDatabase.OPEN_READWRITE);
        }
    }
}
