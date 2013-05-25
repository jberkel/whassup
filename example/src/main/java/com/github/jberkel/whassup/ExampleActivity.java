package com.github.jberkel.whassup;

import android.app.Activity;
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

        try {
            List<Message> messages = whassup.getAllMessages();
            Log.d(TAG, "got "+messages);
        } catch (IOException e) {
            Log.e(TAG, "error getting messages", e);
            Toast.makeText(this, "Error decrypting:"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}