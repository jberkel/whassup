package com.github.jberkel.whassup.model;

public class ChatList {
    public static final String TABLE = "chat_list";
    public static final String FIELD_KEY_REMOTE_JID = "key_remote_jid";

    long _id;

    // 491636325852@s.whatsapp.net
    String key_remote_jid;

    // index of the last message
    long message_table_id;
}
