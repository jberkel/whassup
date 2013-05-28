package com.github.jberkel.whassup.model;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a whatsapp message
 *
 * CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT,
 *      key_remote_jid TEXT NOT NULL,
 *      key_from_me INTEGER,
 *      key_id TEXT NOT NULL,
 *      status INTEGER,
 *      needs_push INTEGER,
 *      data TEXT,
 *      timestamp INTEGER,
 *      media_url TEXT,
 *      media_mime_type TEXT,
 *      media_wa_type TEXT,
 *      media_size INTEGER,
 *      media_name TEXT,
 *      media_hash TEXT,
 *      latitude REAL,
 *      longitude REAL,
 *      thumb_image TEXT,
 *      remote_resource TEXT,
 *      received_timestamp INTEGER,
 *      send_timestamp INTEGER,
 *      receipt_server_timestamp INTEGER,
 *      receipt_device_timestamp INTEGER,
 *      raw_data BLOB,
 *      recipient_count INTEGER,
 *      media_duration INTEGER,
 *      origin INTEGER
 *  );
 */
public class WhatsAppMessage implements Comparable<WhatsAppMessage> {
    public static final String TABLE = "messages";

    private static final String GROUP  = "g.us";
    private static final String DIRECT = "s.whatsapp.net";

    public WhatsAppMessage() {
        this.media = new Media();
        this.receipt = new Receipt();
    }

    public WhatsAppMessage(Cursor c) {
        this._id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        this.key_remote_jid = c.getString(c.getColumnIndex(Fields.KEY_REMOTE_JID.toString()));
        this.key_from_me = c.getInt(c.getColumnIndex(Fields.KEY_FROM_ME.toString()));
        this.timestamp = c.getLong(c.getColumnIndex(Fields.TIMESTAMP.toString()));
        this.data = c.getString(c.getColumnIndex(Fields.DATA.toString()));
        this.status = c.getInt(c.getColumnIndex(Fields.STATUS.toString()));
        this.key_id = c.getString(c.getColumnIndex(Fields.KEY_ID.toString()));
        this.longitude = c.getDouble(c.getColumnIndex(Fields.LONGITUDE.toString()));
        this.latitude = c.getDouble(c.getColumnIndex(Fields.LATITUDE.toString()));
        this.needs_push = c.getInt(c.getColumnIndex(Fields.NEEDS_PUSH.toString()));
        this.recipient_count = c.getInt(c.getColumnIndex(Fields.RECIPIENT_COUNT.toString()));
        this.origin = c.getInt(c.getColumnIndex(Fields.ORIGIN.toString()));
        this.media = new Media(c);
        this.receipt = new Receipt(c);
    }

    long _id;

    /**
     * 49157712345@s.whatsapp.net  (single recipient)
     * 49157712345-1369779058@g.us (group message)
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
     * 0 = ?
     * 5 = recipient received message
     * 4 = group related?
     * 6 = group related?
     */
    int status;

    int needs_push;

    /**
     * textual content of the message
     */
    String data;

    /**
     * epoch in seconds
     */
    long timestamp;

    Media media;
    Receipt receipt;

    double longitude;
    double latitude;

    String remote_resource;

    // > 0 for group messages
    int recipient_count;

    int origin;

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

    public String getFilteredText() {
        return filterPrivateBlock(data);
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

    public int getRecipientCount() {
        return recipient_count;
    }

    public String getNumber() {
        if (TextUtils.isEmpty(key_remote_jid)) return  null;
        String[] components = key_remote_jid.split("@", 2);
        if (components.length > 1) {
            if (!isGroupMessage()) {
                return components[0];
            } else {
                return components[0].split("-")[0];
            }
        } else {
            return null;
        }
    }

    public Media getMedia() {
        return media;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public boolean hasMediaAttached() {
        return media.getFile() != null &&
                media.getFile().exists() &&
                media.getFile().canRead();
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(data);
    }

    @Override
    public String toString() {
        return "Message{" +
                "number='" + getNumber() + '\'' +
                ", text='" + getText() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", media=" + getMedia() +
                '}';
    }

    @Override
    public int compareTo(WhatsAppMessage another) {
        return TimestampComparator.INSTANCE.compare(this, another);
    }

    public boolean isDirectMessage() {
        return key_remote_jid != null && key_remote_jid.endsWith(DIRECT);
    }

    public boolean isGroupMessage() {
        return key_remote_jid != null && key_remote_jid.endsWith(GROUP);
    }

    public enum Fields {
        KEY_REMOTE_JID,
        KEY_FROM_ME,
        TIMESTAMP,
        DATA,
        RAW_DATA,
        MEDIA_HASH,
        MEDIA_SIZE,
        MEDIA_NAME,
        MEDIA_DURATION,
        MEDIA_MIME_TYPE,
        MEDIA_WA_TYPE,
        MEDIA_URL,
        STATUS,
        KEY_ID,
        LONGITUDE,
        LATITUDE,
        NEEDS_PUSH,
        RECIPIENT_COUNT,
        THUMB_IMAGE,
        ORIGIN,
        RECEIVED_TIMESTAMP,
        SEND_TIMESTAMP,
        RECEIPT_SERVER_TIMESTAMP,
        RECEIPT_DEVICE_TIMESTAMP
        ;

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
                return lhs.getTimestamp().compareTo(rhs.getTimestamp());
            }
        }
    }

    static String filterPrivateBlock(String s) {
        if (s == null) return null;

        final StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); ) {
            int codepoint = s.codePointAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(codepoint);
            if (block != Character.UnicodeBlock.PRIVATE_USE_AREA) {
                sb.append(Character.toChars(codepoint));
            }
            i += Character.charCount(codepoint);
        }
        return sb.toString();
    }
}
