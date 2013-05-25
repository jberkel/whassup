package com.github.jberkel.whassup;

import com.github.jberkel.whassup.crypto.DBCrypto;
import com.github.jberkel.whassup.model.Fixtures;
import com.github.jberkel.whassup.model.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
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
        whassup = new Whassup(Robolectric.application, new DBCrypto(), dbProvider);
    }

    @Test
    public void shouldGetAllMessages() throws Exception {
        when(dbProvider.getCurrent()).thenReturn(Fixtures.TEST_DB_1);
        List<Message> messages = whassup.getAllMessages();

        assertThat(messages).isNotEmpty();
        assertThat(messages).hasSize(76);
    }

    @Test
    public void shouldGetAllMessagesSince() throws Exception {
        when(dbProvider.getCurrent()).thenReturn(Fixtures.TEST_DB_1);
        List<Message> messages = whassup.getMessagesSince(1367349391104L);

        assertThat(messages).isNotEmpty();
        assertThat(messages).hasSize(9);
    }
}
