package com.github.jberkel.whassup.helper;

import com.github.jberkel.whassup.model.ChatList;
import com.github.jberkel.whassup.model.WhatsAppMessage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.provider.BaseColumns._ID;
import static com.github.jberkel.whassup.model.WhatsAppMessage.Fields.DATA;
import static com.github.jberkel.whassup.model.WhatsAppMessage.Fields.KEY_REMOTE_JID;

/**
 * Anonymises an unencrypted database.
 */
public class Anonymizer {

    public static void main(String[] args) throws SQLException {
        if (args.length == 0) {
            error("<input>");
        }
        File input = new File(args[0]);
        if (!input.exists()) {
            error("file "+input+ " does not exist");
        }
        Anonymizer anonymizer = new Anonymizer();
        anonymizer.anonymize(input);
    }

    public void anonymize(File in) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot load driver class", e);
        }
        Connection conn = DriverManager.getConnection("jdbc:sqlite:"+in.getAbsolutePath());
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM messages");
        ResultSet set = stmt.executeQuery();


        final Map<String, String> idMappings = new HashMap<String, String>();
        final Random random = new Random(System.currentTimeMillis());
        while (set.next()) {
            long id = set.getLong(_ID);
            String remoteId = set.getString(KEY_REMOTE_JID.toString());
            String mappedId = idMappings.get(remoteId);
            if (mappedId == null) {
                mappedId = String.format("%s@s.whatsapp.net", String.valueOf(Math.abs(random.nextLong())));
                idMappings.put(remoteId, mappedId);
            }
            PreparedStatement update = conn.prepareStatement(
                    "UPDATE "+ WhatsAppMessage.TABLE+" SET "+ DATA + " = ? "  +
                    "WHERE " +_ID+" = ?"
            );
            update.setString(1, generateMessage(random));
            update.setLong(2, id);
            update.execute();
        }
        for (Map.Entry<String, String> e : idMappings.entrySet()) {
            replaceJid(WhatsAppMessage.TABLE, conn, e.getKey(), e.getValue());
            replaceJid(ChatList.TABLE, conn, e.getKey(), e.getValue());
        }
        set.close();
        conn.close();
    }

    private void replaceJid(String table, Connection conn, String old, String _new) throws SQLException {
        PreparedStatement update = conn.prepareStatement(
                "UPDATE "+ table+" SET "+ KEY_REMOTE_JID+" = ? " +
                        "WHERE "+ KEY_REMOTE_JID+" = ?"
        );
        update.setString(1, _new);
        update.setString(2, old);
        update.execute();
    }

    private static void error(String message) {
        System.err.println(Anonymizer.class.getSimpleName()+" "+message);
        System.exit(1);
    }

    private static String generateMessage(Random random) {
        return new LoremIpsum().getWords(random.nextInt(10));
    }
}
