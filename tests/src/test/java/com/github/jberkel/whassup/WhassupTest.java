package com.github.jberkel.whassup;

import android.database.Cursor;
import com.github.jberkel.whassup.crypto.DBCrypto;
import com.github.jberkel.whassup.model.Fixtures;
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
        assertThat(messages).hasSize(76);
    }

    @Test
    public void shouldQueryMessages() throws Exception {
        Cursor cursor = whassup.queryMessages();
        assertThat(cursor).isNotNull();
        assertThat(cursor.getCount()).isEqualTo(76);
        cursor.close();
    }

    @Test
    public void shouldGetMessagesSinceASpecificTimestamp() throws Exception {
        List<WhatsAppMessage> messages = whassup.getMessages(1367349391104L, -1);
        assertThat(messages).isNotEmpty();
        assertThat(messages).hasSize(9);
    }

    @Test
    public void shouldQueryMessagesSinceASpecificTimestamp() throws Exception {
        Cursor cursor = whassup.queryMessages(1367349391104L, -1);
        assertThat(cursor).isNotNull();
        assertThat(cursor.getCount()).isEqualTo(9);
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
    public void shouldReturnMessagesInDescendingTimestampOrder() throws Exception {
        List<WhatsAppMessage> messages = whassup.getMessages();
        assertThat(messages).isSortedAccordingTo(WhatsAppMessage.TimestampComparator.INSTANCE);
    }

    @Test
    public void shouldCheckIfDbIsAvailable() throws Exception {
        when(dbProvider.getCurrent()).thenReturn(null);
        assertThat(whassup.hasBackupDB()).isFalse();
        when(dbProvider.getCurrent()).thenReturn(Fixtures.TEST_DB_1);
        assertThat(whassup.hasBackupDB()).isTrue();
    }
}
