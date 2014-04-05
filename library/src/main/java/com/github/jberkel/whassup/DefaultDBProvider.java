package com.github.jberkel.whassup;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import static com.github.jberkel.whassup.Whassup.TAG;

public class DefaultDBProvider implements DBProvider {
    private static final String CURRENT_DB   = "msgstore.db.crypt";
    private static final String CURRENT_DB_5 = "msgstore.db.crypt5";

    private static final File DB_PATH = new File(Environment.getExternalStorageDirectory(),
        "Whatsapp/Databases");

    @Override
    public File getDBFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            for (String name : new String[] { CURRENT_DB_5, CURRENT_DB }) {
                File db = new File(DB_PATH, name);
                if (db.exists() && db.canRead()) {
                    return db;
                }
            }
            Log.d(TAG, "could not find db");
            return null;
        } else {
            Log.w(TAG, "external storage not mounted");
            return null;
        }
    }
}
