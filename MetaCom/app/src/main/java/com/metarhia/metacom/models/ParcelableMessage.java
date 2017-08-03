package com.metarhia.metacom.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by masha on 8/3/17.
 */

public class ParcelableMessage extends Message implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public ParcelableMessage createFromParcel(Parcel in) {
            return new ParcelableMessage(in);
        }

        @Override
        public ParcelableMessage[] newArray(int size) {
            return new ParcelableMessage[size];
        }
    };

    public ParcelableMessage(MessageType type, String content, boolean isIncoming) {
        super(type, content, isIncoming);
    }

    public ParcelableMessage(Message message) {
        super(message.getType(), message.getContent(), message.isIncoming());
        setWaiting(message.isWaiting());
    }

    private ParcelableMessage(Parcel in) {
        super(MessageType.valueOf(in.readString()), in.readString(), in.readByte() != 0x00);
        boolean mIsWaiting = in.readByte() != 0x00;
        setWaiting(mIsWaiting);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getType().name());
        dest.writeString(getContent());
        dest.writeByte((byte) (isIncoming() ? 0x01 : 0x00));
        dest.writeByte((byte) (isWaiting() ? 0x01 : 0x00));
    }
}