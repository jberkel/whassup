package com.github.jberkel.whassup.model;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static com.github.jberkel.whassup.model.WhatsAppMessage.Fields.*;

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

    public static final String GROUP  = "g.us";
    public static final String DIRECT = "s.whatsapp.net";

    public WhatsAppMessage() {
        this.media = new Media();
        this.receipt = new Receipt();
    }

    public WhatsAppMessage(Cursor c) {
        this._id             = _ID.getLong(c);
        this.key_remote_jid  = KEY_REMOTE_JID.getString(c);
        this.key_from_me     = KEY_FROM_ME.getInt(c);
        this.timestamp       = TIMESTAMP.getLong(c);
        this.data            = DATA.getString(c);
        this.status          = STATUS.getInt(c);
        this.key_id          = KEY_ID.getString(c);
        this.longitude       = LONGITUDE.getDouble(c);
        this.latitude        = LATITUDE.getDouble(c);
        this.needs_push      = NEEDS_PUSH.getInt(c);
        this.recipient_count = RECIPIENT_COUNT.getInt(c);
        this.origin          = ORIGIN.getInt(c);
        this.media   = new Media(c);
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
        _ID,
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

        public int colIndex(Cursor c) {
            return c.getColumnIndex(toString());
        }

        public int getInt(Cursor c) {
            int idx = colIndex(c);
            return idx >= 0 ? c.getInt(idx) : 0;
        }

        public long getLong(Cursor c) {
            int idx = colIndex(c);
            return idx >= 0 ? c.getLong(idx) : 0;
        }

        public String getString(Cursor c) {
            int idx = colIndex(c);
            return idx >= 0 ? c.getString(idx) : null;
        }

        public double getDouble(Cursor c) {
            int idx = colIndex(c);
            return idx >= 0 ? c.getDouble(idx) : 0;
        }

        public byte[] getBlob(Cursor c) {
            int idx = colIndex(c);
            return idx >= 0 ? c.getBlob(idx) : null;
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
