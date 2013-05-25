package com.github.jberkel.whassup.model;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Date;

/**
 * Represents a whatsapp message
 */
public class Message {
    public static final String TABLE = "messages";

    public static final String FIELD_KEY_REMOTE_JID = "key_remote_jid";
    public static final String FIELD_KEY_FROM_ME    = "key_from_me";
    public static final String FIELD_TIMESTAMP      = "timestamp";
    public static final String FIELD_DATA           = "data";

    public Message() {}

    public Message(Cursor c) {
        this._id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        this.key_remote_jid = c.getString(c.getColumnIndex(FIELD_KEY_REMOTE_JID));
        this.key_from_me = c.getInt(c.getColumnIndex(FIELD_KEY_FROM_ME));
        this.timestamp = c.getLong(c.getColumnIndex(FIELD_TIMESTAMP));
        this.data = c.getString(c.getColumnIndex(FIELD_DATA));
    }

    long _id;

    /**
     * 4915775302629@s.whatsapp.net
     */
    String key_remote_jid;

    /**
     * 0 = received, 1 = sent
     */
    int key_from_me;

    /**
     * whatsapp internal
     */
    String key_id;

    /**
     * 5 = recipient received message
     */
    int status;

    int needs_push;

    /**
     * textual content of the message
     */
    String data;
    String raw_data;

    /**
     * epoch in seconds
     */
    long timestamp;

    Media media;
    Receipt receipt;

    double longitude;
    double latitude;

    String thumb_image;
    String remote_resource;

    int recipient_count;

    public Date getTimestamp() {
        return new Date(timestamp);
    }

    public String getNumber() {
        if (TextUtils.isEmpty(key_remote_jid)) return  null;

        String[] components = key_remote_jid.split("@", 2);

        if (components.length > 1) {
            return components[0];
        } else {
            return null;
        }
    }
}
