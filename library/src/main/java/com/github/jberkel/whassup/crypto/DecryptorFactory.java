package com.github.jberkel.whassup.crypto;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Patterns;

import java.io.File;
import java.util.regex.Pattern;

public class DecryptorFactory {
    private static final Pattern EMAIL = Patterns.EMAIL_ADDRESS;
    private String email;

    public DecryptorFactory(Context context) {
        AccountManager manager = AccountManager.get(context);
        final Account[] accounts = manager.getAccountsByType("com.google");
        for (Account account : accounts) {
            if (EMAIL.matcher(account.name).matches()) {
                email = account.name;
                break;
            }
        }
    }

    public Decryptor getDecryptorForFile(File file) {
        if (file.getName().endsWith(".crypt5") && email != null) {
            return new DBDecryptor5(email);
        } else {
            return new DBDecryptor();
        }
    }
}
