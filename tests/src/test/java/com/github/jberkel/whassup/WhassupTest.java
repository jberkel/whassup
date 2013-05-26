package com.github.jberkel.whassup;

import android.database.Cursor;
import com.github.jberkel.whassup.crypto.DBCrypto;
import com.github.jberkel.whassup.model.Fixtures;
import com.github.jberkel.whassup.model.Media;
import com.github.jberkel.whassup.model.WhatsAppMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class WhassupTest {
    Whassup whassup;
    @Mock DBProvider dbProvider;

    @Before public void before() {
        initMocks(this);
        whassup = new Whassup(new DBCrypto(), dbProvider);
        when(dbProvider.getCurrent()).thenReturn(Fixtures.TEST_DB_1);
    }

    @Test
    public void shouldGetAllMessages() throws Exception {
        List<WhatsAppMessage> messages = whassup.getMessages();
        assertThat(messages).isNotEmpty();
        assertThat(messages).hasSize(82);
    }

    @Test
    public void shouldQueryMessages() throws Exception {
        Cursor cursor = whassup.queryMessages();
        assertThat(cursor).isNotNull();
        assertThat(cursor.getCount()).isEqualTo(82);
        cursor.close();
    }

    @Test
    public void shouldGetMessagesSinceASpecificTimestamp() throws Exception {
        List<WhatsAppMessage> messages = whassup.getMessages(1367349391104L, -1);
        assertThat(messages).isNotEmpty();
        assertThat(messages).hasSize(15);
    }

    @Test
    public void shouldQueryMessagesSinceASpecificTimestamp() throws Exception {
        Cursor cursor = whassup.queryMessages(1367349391104L, -1);
        assertThat(cursor).isNotNull();
        assertThat(cursor.getCount()).isEqualTo(15);
    }

    @Test
    public void shouldGetMessagesSinceASpecificTimestampAndLimit() throws Exception {
        List<WhatsAppMessage> messages = whassup.getMessages(1367349391104L, 3);
        assertThat(messages).isNotEmpty();
        assertThat(messages).hasSize(3);
    }

    @Test
    public void shouldQueryMessagesSinceASpecificTimestampAndLimit() throws Exception {
        Cursor cursor = whassup.queryMessages(1367349391104L, 3);
        assertThat(cursor).isNotNull();
        assertThat(cursor.getCount()).isEqualTo(3);
    }

    @Test
    public void shouldGetMedia() throws Exception {
        List<WhatsAppMessage> messages = whassup.getMessages();
        WhatsAppMessage msg = null;
        for (WhatsAppMessage message : messages) {
            if (message.getId() == 82) {
                msg = message;
                break;
            }
        }
        assertThat(msg).isNotNull();
        assertThat(msg.getMedia()).isNotNull();
        Media mediaData = msg.getMedia();
        assertThat(mediaData).isNotNull();
        assertThat(mediaData.getFileSize()).isEqualTo(67731L);
        assertThat(mediaData.getFile().getAbsolutePath()).isEqualTo("/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20130526-WA0000.jpg");
    }

    @Test
    public void shouldReturnMessagesInAscendingTimestampOrder() throws Exception {
        List<WhatsAppMessage> messages = whassup.getMessages();
        assertThat(messages).isSortedAccordingTo(WhatsAppMessage.TimestampComparator.INSTANCE);
        WhatsAppMessage first = messages.get(0);
        WhatsAppMessage last = messages.get(messages.size() - 1);
        assertThat(first.getTimestamp()).isBefore(last.getTimestamp());
    }

    @Test
    public void shouldCheckIfDbIsAvailable() throws Exception {
        when(dbProvider.getCurrent()).thenReturn(null);
        assertThat(whassup.hasBackupDB()).isFalse();
        when(dbProvider.getCurrent()).thenReturn(Fixtures.TEST_DB_1);
        assertThat(whassup.hasBackupDB()).isTrue();
    }
}
