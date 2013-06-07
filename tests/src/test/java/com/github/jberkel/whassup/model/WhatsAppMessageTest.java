package com.github.jberkel.whassup.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.nio.charset.Charset;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class WhatsAppMessageTest {

    @Test
    public void shouldParseTimestamp() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        m.timestamp = 1358086780000L;
        assertThat(m.getTimestamp().getTime()).isEqualTo(1358086780000L);
    }

    @Test
    public void shouldParseNumber() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        m.key_remote_jid = "4915773981234@s.whatsapp.net";
        assertThat(m.getNumber()).isEqualTo("4915773981234");
    }

    @Test
    public void shouldParseNumberFromGroupMessage() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        m.key_remote_jid = "4915773981234-12345@g.us";
        assertThat(m.getNumber()).isEqualTo("4915773981234");
    }

    @Test
    public void shouldDetectGroupMessage() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        m.key_remote_jid = "4915773981234-12345@g.us";
        assertThat(m.isGroupMessage()).isTrue();
        assertThat(m.isDirectMessage()).isFalse();
    }

    @Test
    public void shouldDetectDirectMessage() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        m.key_remote_jid = "4915773981234@s.whatsapp.net";
        assertThat(m.isDirectMessage()).isTrue();
        assertThat(m.isGroupMessage()).isFalse();
    }

    @Test
    public void shouldParseNumberWithInvalidSpec() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        assertThat(m.getNumber()).isNull();
        m.key_remote_jid = "foobaz";
        assertThat(m.getNumber()).isNull();
    }

    @Test
    public void shouldImplementComparableBasedOnTimestamp() throws Exception {
        WhatsAppMessage m1 = new WhatsAppMessage();
        WhatsAppMessage m2 = new WhatsAppMessage();

        m1.timestamp = 1;
        m2.timestamp = 2;

        assertThat(m1.compareTo(m2)).isLessThan(0);
        assertThat(m2.compareTo(m1)).isGreaterThan(0);

        assertThat(m2.compareTo(m2)).isEqualTo(0);
        assertThat(m1.compareTo(m1)).isEqualTo(0);
    }

    @Test
    public void shouldHaveTextIfNonEmptyString() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        assertThat(m.hasText()).isFalse();

        m.data = "";
        assertThat(m.hasText()).isFalse();

        m.data = "some text";
        assertThat(m.hasText()).isTrue();
    }

    @Test
    public void shouldCheckIfReceived() throws Exception {
        WhatsAppMessage m = new WhatsAppMessage();
        assertThat(m.isReceived()).isTrue();
        m.key_from_me = 1;
        assertThat(m.isReceived()).isFalse();
    }

    @Test
    public void shouldFilterPrivateUnicodeCharacters() throws Exception {
        byte[] b = new byte[] {
            (byte) 0xee, (byte) 0x90, (byte) 0x87, // private
            (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0xa4, //FACE WITH LOOK OF TRIUMPH, U+1F624
            (byte) 0xee, (byte) 0x84, (byte) 0x87 // private
        };
        String s = new String(b, Charset.forName("UTF-8"));
        assertThat(s.length()).isEqualTo(4);  // 4 x UTF16
        String filtered = WhatsAppMessage.filterPrivateBlock(s);

        assertThat(filtered).isEqualTo("\uD83D\uDE24");
        assertThat(filtered.length()).isEqualTo(2);
    }

    @Test
    public void shouldFilterPrivateUnicodeCharactersNull() throws Exception {
        assertThat(WhatsAppMessage.filterPrivateBlock(null)).isNull();
    }
}
