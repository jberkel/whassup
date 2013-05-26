package com.github.jberkel.whassup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.os.Environment;
import android.util.Log;
import com.github.jberkel.whassup.crypto.DBCrypto;
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

    private final Context mContext;
    private final DBCrypto dbCrypto;
    private final DBProvider dbProvider;

    public Whassup(Context context) {
        this(context, new DBCrypto(), new FileSystemDBProvider());
    }

    public Whassup(Context context, DBCrypto decryptor, DBProvider dbProvider) {
        this.mContext = context.getApplicationContext();
        this.dbCrypto = decryptor;
        this.dbProvider = dbProvider;
    }

    /**
     * @return a cursor with all available messages
     * @throws IOException
     */
    public Cursor queryMessages() throws IOException {
        return queryMessagesSince(0);
    }

    /**
     * @param timestamp a timestamp, epoch format
     * @return a cursor with messages after timestamp
     * @throws IOException
     */
    public Cursor queryMessagesSince(long timestamp) throws IOException {
        File currentDB = dbProvider.getCurrent();
        if (currentDB == null) {
            return null;
        } else {
            return getCursorFromDB(decryptDB(currentDB), timestamp);
        }
    }

    /**
     * Convenience method which reads all messages and converts them into model objects.
     * @param timestamp fetch all message since timestamp
     * @return a list of messages
     * @throws IOException
     */
    public List<WhatsAppMessage> getMessagesSince(long timestamp) throws IOException {
        Cursor cursor = queryMessagesSince(timestamp);
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
        return getMessagesSince(0);
    }

    /**
     * @return if there is a whatsapp backup available
     */
    public boolean hasBackupDB() {
        return dbProvider.getCurrent() != null;
    }

    private Cursor getCursorFromDB(final File dbFile, long since) throws IOException {
        Log.d(TAG, "using DB "+dbFile);
        SQLiteDatabase db = getSqLiteDatabase(dbFile);
        String selection = null;
        String[] selectionArgs = null;
        if (since > 0) {
            selection = String.format("%s > ?", WhatsAppMessage.Fields.TIMESTAMP);
            selectionArgs = new String[]{String.valueOf(since)};
        }
        return db.query(WhatsAppMessage.TABLE, null, selection, selectionArgs, null, null, null);
    }

    private SQLiteDatabase getSqLiteDatabase(final File dbFile) {
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
        }, SQLiteDatabase.OPEN_READONLY);
    }

    private File decryptDB(File in) throws IOException {
        File out = File.createTempFile("decrypted-db", ".sqlite");
        try {
            dbCrypto.decryptDB(in, out);
            return out;
        } catch (GeneralSecurityException e) {
            throw new IOException("Could not decrypt db", e);
        }
    }

    public static class FileSystemDBProvider implements DBProvider {
        @Override
        public File getCurrent() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File db = new File(DB_PATH, CURRENT_DB);
                if (db.exists()) {
                    return db;
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
}
