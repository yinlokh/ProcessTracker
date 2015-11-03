package com.example.eric.processtracker.model;

import android.os.Parcel;
import android.os.Parcelable;

// Data model for a process entry from ps command
public class ProcessEntry implements Comparable, Parcelable{

    public final int pid;
    public final String name;
    public final int vsize;

    public ProcessEntry(Parcel parcel) {
        pid = parcel.readInt();
        name = parcel.readString();
        vsize = parcel.readInt();
    }

    public ProcessEntry(int pid, int vsize, String name) {
        this.pid = pid;
        this.vsize = vsize;
        this.name = name;

    }

    @Override
    public int compareTo(Object another) {
        ProcessEntry other = (ProcessEntry) another;
        return this.name.compareTo(other.name);
    }

    public static final Creator<ProcessEntry> CREATOR = new Creator<ProcessEntry>() {
        @Override
        public ProcessEntry createFromParcel(Parcel in) {
            return new ProcessEntry(in);
        }

        @Override
        public ProcessEntry[] newArray(int size) {
            return new ProcessEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pid);
        dest.writeString(name);
        dest.writeInt(vsize);
    }
}
