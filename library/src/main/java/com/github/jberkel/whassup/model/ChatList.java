package com.github.jberkel.whassup.model;

/**
 * CREATE TABLE chat_list (
 *      _id INTEGER PRIMARY KEY AUTOINCREMENT,
 *      key_remote_jid TEXT UNIQUE,
 *      message_table_id INTEGER
 *  );
 */
public class ChatList {
    public static final String TABLE = "chat_list";
    public static final String FIELD_KEY_REMOTE_JID = "key_remote_jid";

    long _id;

    // 4916312345@s.whatsapp.net
    String key_remote_jid;

    // index of the last message
    long message_table_id;
}
