package com.github.jberkel.whassup.model;

import android.database.Cursor;

import static com.github.jberkel.whassup.model.WhatsAppMessage.Fields.*;

public class Receipt {
    long received_timestamp;
    long send_timestamp;
    long receipt_server_timestamp;
    long receipt_device_timestamp;

    public Receipt() {
    }

    public Receipt(Cursor cursor) {
        this.receipt_device_timestamp = RECEIPT_DEVICE_TIMESTAMP.getLong(cursor);
        this.send_timestamp           = SEND_TIMESTAMP.getLong(cursor);
        this.receipt_server_timestamp = RECEIPT_SERVER_TIMESTAMP.getLong(cursor);
        this.received_timestamp       = RECEIVED_TIMESTAMP.getLong(cursor);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "received_timestamp=" + received_timestamp +
                ", send_timestamp=" + send_timestamp +
                ", receipt_server_timestamp=" + receipt_server_timestamp +
                ", receipt_device_timestamp=" + receipt_device_timestamp +
                '}';
    }
}
