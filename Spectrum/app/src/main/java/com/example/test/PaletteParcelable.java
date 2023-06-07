package com.example.test;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PaletteParcelable implements Parcelable {
    private ArrayList<Integer> colours;
    private String title;
    private String id;
    private String userId;

    // CONSTRUCTOR Palette
    public PaletteParcelable(){}

    public PaletteParcelable(String title, ArrayList<Integer> colours, String userId) {
        this.title = title;
        this.colours = colours;
        this.userId = userId;
    }

    // SETTERS

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) { this.id = id; }

    public void addColour(int colour) {
        colours.add(colour);
    }

    public void removeColour(int index) {
        colours.remove(index);
    }

    // GETTERS

    public ArrayList<Integer> getColours() {
        return colours;
    }

    public String getTitle() { return title; }

    public String getId() { return id; }

    public String getUserId() {return userId; }

    // PARCELABLE

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.title);
        out.writeList(this.colours);
        out.writeString(this.id);
        out.writeString(this.userId);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<PaletteParcelable>() {
        public PaletteParcelable createFromParcel(Parcel in) {
            return new PaletteParcelable(in);
        }

        public PaletteParcelable[] newArray(int size) {
            return new PaletteParcelable[size];
        }
    };

    private PaletteParcelable(Parcel in) {
        title = in.readString();
        colours = in.readArrayList(null);
        id = in.readString();
        userId = in.readString();
    }
}
