package com.example.eric.processtracker.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model to represent one data point of vsize on a process
 */
public class VSizeEntry implements Parcelable {

    public final int vsize;
    public final long timestamp;

    public VSizeEntry(Parcel parcel) {
        vsize = parcel.readInt();
        timestamp = parcel.readLong();
    }

    public VSizeEntry(int vsize, long timestamp) {
        this.vsize = vsize;
        this.timestamp = timestamp;
    }

    public static final Creator<VSizeEntry> CREATOR = new Creator<VSizeEntry>() {
        @Override
        public VSizeEntry createFromParcel(Parcel in) {
            return new VSizeEntry(in);
        }

        @Override
        public VSizeEntry[] newArray(int size) {
            return new VSizeEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(vsize);
        dest.writeLong(timestamp);
    }
}
