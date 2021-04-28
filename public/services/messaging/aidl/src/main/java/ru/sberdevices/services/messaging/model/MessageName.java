package ru.sberdevices.services.messaging.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class MessageName implements Parcelable {

    public final MessageNameType type;

    public MessageName(final MessageNameType type) {
        this.type = type;
    }

    protected MessageName(Parcel in) {
        this(MessageNameType.valueOf(in.readString()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type.name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MessageName> CREATOR =
            new Creator<MessageName>() {
                @Override
                public MessageName createFromParcel(Parcel in) {
                    return new MessageName(in);
                }

                @Override
                public MessageName[] newArray(int size) {
                    return new MessageName[size];
                }
            };

    public enum MessageNameType {
        SERVER_ACTION,
        RUN_APP,
        CLOSE_APP,
        HEARTBEAT,
        UPDATE_IP
    }
}
