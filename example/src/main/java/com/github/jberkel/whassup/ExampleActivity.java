package com.github.jberkel.whassup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


/**
 * This is an example activity. It will launch an activity
 * defined in the library project to verify that the manifest
 * merging works correctly.
 */
public class ExampleActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("ExampleActivity", "onCreate()");

        // launch an activity from the library
        startActivity(new Intent(this, LibraryActivity.class));
    }
}