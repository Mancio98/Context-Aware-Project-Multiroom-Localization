package com.example.multiroomlocalization;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Map implements Parcelable {
    String id;
    boolean isReady;

    Map(String id, boolean isReady){
        this.id = id;
        this.isReady = isReady;
    }


    protected Map(Parcel in) {
        id = in.readString();
        isReady = in.readByte() != 0;
    }

    public static final Creator<Map> CREATOR = new Creator<Map>() {
        @Override
        public Map createFromParcel(Parcel in) {
            return new Map(in);
        }

        @Override
        public Map[] newArray(int size) {
            return new Map[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeByte((byte) (isReady ? 1 : 0));
    }
}
