package com.github.jberkel.whassup.model;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a whatsapp message
 */
public class WhatsAppMessage implements Comparable<WhatsAppMessage> {
    public static final String TABLE = "messages";

    public WhatsAppMessage() {}

    public WhatsAppMessage(Cursor c) {
        this._id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        this.key_remote_jid = c.getString(c.getColumnIndex(Fields.KEY_REMOTE_JID.toString()));
        this.key_from_me = c.getInt(c.getColumnIndex(Fields.KEY_FROM_ME.toString()));
        this.timestamp = c.getLong(c.getColumnIndex(Fields.TIMESTAMP.toString()));
        this.data = c.getString(c.getColumnIndex(Fields.DATA.toString()));
        this.raw_data = c.getString(c.getColumnIndex(Fields.RAW_DATA.toString()));
        this.status = c.getInt(c.getColumnIndex(Fields.STATUS.toString()));
        this.key_id = c.getString(c.getColumnIndex(Fields.KEY_ID.toString()));
        this.longitude = c.getDouble(c.getColumnIndex(Fields.LONGITUDE.toString()));
        this.latitude = c.getDouble(c.getColumnIndex(Fields.LATITUDE.toString()));
        this.needs_push = c.getInt(c.getColumnIndex(Fields.NEEDS_PUSH.toString()));
        this.recipient_count = c.getInt(c.getColumnIndex(Fields.RECIPIENT_COUNT.toString()));
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

    public long getId() {
        return _id;
    }

    public boolean isReceived() {
        return key_from_me == 0;
    }

    public Date getTimestamp() {
        return new Date(timestamp);
    }

    public String getText() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
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

    @Override
    public String toString() {
        return "Message{" +
                "number='" + getNumber() + '\'' +
                ", text='" + getText() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }

    @Override
    public int compareTo(WhatsAppMessage another) {
        return TimestampComparator.INSTANCE.compare(this, another);
    }

    public enum Fields {
        KEY_REMOTE_JID,
        KEY_FROM_ME,
        TIMESTAMP,
        DATA,
        RAW_DATA,
        STATUS,
        KEY_ID,
        LONGITUDE,
        LATITUDE,
        NEEDS_PUSH,
        RECIPIENT_COUNT;

        @Override public String toString() {
            return this.name().toLowerCase(Locale.ENGLISH);
        }
    }

    public static class TimestampComparator implements Comparator<WhatsAppMessage> {
        public static final TimestampComparator INSTANCE = new TimestampComparator();

        @Override
        public int compare(WhatsAppMessage lhs, WhatsAppMessage rhs) {
            if (lhs == rhs) {
                return 0;
            } else if (lhs == null) {
                return 1;
            } else if (rhs == null) {
                return -1;
            } else {
                return rhs.getTimestamp().compareTo(lhs.getTimestamp());
            }
        }
    }
}
