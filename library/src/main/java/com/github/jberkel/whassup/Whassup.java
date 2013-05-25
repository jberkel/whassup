package com.github.jberkel.whassup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import com.github.jberkel.whassup.crypto.DBCrypto;
import com.github.jberkel.whassup.model.Message;

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


    public Whassup(Context context, DBCrypto decryptor, DBProvider dbProvider) {
        this.mContext = context;
        this.dbCrypto = decryptor;
        this.dbProvider = dbProvider;
    }

    public List<Message> getAllMessages() throws IOException {
        return getMessagesSince(0);
    }

    public List<Message> getMessagesSince(long timestamp) throws IOException {
        File currentDB = dbProvider.getCurrent();
        if (currentDB == null) {
            return Collections.emptyList();
        } else {
            return getMessagesFromDB(currentDB, timestamp);
        }
    }

    private List<Message> getMessagesFromDB(File in, long since) throws IOException {
        File decrypted = decryptDB(in);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(decrypted.getAbsolutePath(), null, 0);

        Cursor cursor = null;
        try {
            String selection = null;
            String[] selectionArgs = null;
            if (since > 0) {
                selection = String.format("%s > ?", Message.FIELD_TIMESTAMP);
                selectionArgs = new String[] { String.valueOf(since) };
            }

            cursor = db.query(Message.TABLE, null, selection, selectionArgs, null, null, null);
            if (cursor != null) {
                List<Message> messages = new ArrayList<Message>(cursor.getCount());
                while (cursor.moveToNext()) {
                    messages.add(new Message(cursor));
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
