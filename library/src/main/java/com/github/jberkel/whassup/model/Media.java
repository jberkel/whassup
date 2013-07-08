package com.github.jberkel.whassup.model;

import android.database.Cursor;
import com.whatsapp.MediaData;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.github.jberkel.whassup.model.WhatsAppMessage.Fields.*;

public class Media {
    // https://mms831.whatsapp.net/d/mLiu1j3jlniKtF8IzYoEaubS43c/AlIMgZabrwNZRdGQlq2sxEEvO2b_Cej4y5dlcFTjnHOY.jpg
    String media_url;

    // image/jpeg
    String media_mime_type;

    // 1 ?
    String media_wa_type;

    // size of media_url content
    int media_size;

    int media_duration;

    String media_name;

    // EXo/UFC/8oxyHLRSRRRh7ZjoRfkyhNxqnQqBsB+2L/8=
    String media_hash;

    /**
     * Thumbnail representation of image.
     */
    byte[] raw_data;

    /**
     * Java serialized representation of {@link MediaData}
     */
    byte[] thumb_image;

    private MediaData mediaData;

    public Media() {
    }

    public Media(Cursor c) {
        this.raw_data        = RAW_DATA.getBlob(c);
        this.thumb_image     = THUMB_IMAGE.getBlob(c);
        this.media_hash      = MEDIA_HASH.getString(c);
        this.media_size      = MEDIA_SIZE.getInt(c);
        this.media_name      = MEDIA_NAME.getString(c);
        this.media_duration  = MEDIA_DURATION.getInt(c);
        this.media_mime_type = MEDIA_MIME_TYPE.getString(c);
        this.media_url       = MEDIA_URL.getString(c);
        this.media_wa_type   = MEDIA_WA_TYPE.getString(c);
    }

    public byte[] getRawData() {
        return raw_data;
    }

    public String getMimeType() {
        return media_mime_type;
    }

    public String getUrl() {
        return media_url;
    }

    public int getSize() {
        return media_size;
    }

    public File getFile() {
        MediaData md = getMediaData();
        return md == null ? null : md.getFile();
    }

    public long getFileSize() {
        MediaData md = getMediaData();
        return md == null ? -1 : md.getFileSize();
    }

    private MediaData getMediaData() {
        if (mediaData == null) {
            if (thumb_image != null) {
                mediaData = parseData(thumb_image);
            }
        }
        return mediaData;
    }

    private static MediaData parseData(byte[] data) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            return (MediaData) ois.readObject();
        } catch (IOException ignored) {
        } catch (ClassNotFoundException ignored) {
        } catch (ClassCastException ignored) {
        }
        return null;
    }

    @Override
    public String toString() {
        return "Media{" +
                "media_url='" + media_url + '\'' +
                ", media_mime_type='" + media_mime_type + '\'' +
                ", mediaData=" + getMediaData() +
                ", media_size=" + media_size +
                '}';
    }
}
