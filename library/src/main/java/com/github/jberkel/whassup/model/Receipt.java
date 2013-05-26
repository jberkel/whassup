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
        this.receipt_device_timestamp = cursor.getLong(cursor.getColumnIndex(RECEIPT_DEVICE_TIMESTAMP.toString()));
        this.send_timestamp = cursor.getLong(cursor.getColumnIndex(SEND_TIMESTAMP.toString()));
        this.receipt_server_timestamp = cursor.getLong(cursor.getColumnIndex(RECEIPT_SERVER_TIMESTAMP.toString()));
        this.received_timestamp = cursor.getLong(cursor.getColumnIndex(RECEIVED_TIMESTAMP.toString()));
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
