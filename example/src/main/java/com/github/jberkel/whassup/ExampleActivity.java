package com.github.jberkel.whassup;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.github.jberkel.whassup.model.Message;

import java.io.IOException;
import java.util.List;


public class ExampleActivity extends Activity {
    private static final String TAG = ExampleActivity.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ExampleActivity", "onCreate()");

        Whassup whassup = new Whassup(this);

        fetchMessageCursor(whassup);
        fetchMessages(whassup);
    }

    private void fetchMessages(Whassup whassup) {
        try {
            List<Message> messages = whassup.getMessages();
            Log.d(TAG, "got " + messages);
        } catch (IOException e) {
            Log.e(TAG, "error getting messages", e);
            Toast.makeText(this, "Error decrypting:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fetchMessageCursor(Whassup whassup) {
        Cursor cursor = null;
        try {
            cursor = whassup.queryMessages();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Message m = new Message(cursor);
                    Log.d(TAG, "got "+m);
                }
            } else {
                Log.w(TAG, "no cursor");
            }
        } catch (IOException e) {
            Log.e(TAG, "error getting messages", e);
            Toast.makeText(this, "Error decrypting:" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}