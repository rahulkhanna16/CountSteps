package com.hatcheryhub.countsteps.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rahulkhanna on 13/06/16.
 */
public class Logs extends SugarRecord implements Parcelable {
    private String date;
    int credit;

    public Logs() {

    }


    public Logs(String date, int credit) {
        this.date = date;
        this.credit = credit;
    }


    public Logs(Parcel in) {
        this.date = in.readString();
        this.credit = in.readInt();
    }

    public static void saveLog(String date, int credit) {
        Logs logs = new Logs(date, credit);
        logs.save();
    }

    public static ArrayList<Logs> getAllLogs(){
        return (ArrayList<Logs>) Logs.listAll(Logs.class);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeInt(credit);
    }

    public static final Creator<Logs> CREATOR = new Creator<Logs>()
    {
        public Logs createFromParcel(Parcel in)
        {
            return new Logs(in);
        }
        public Logs[] newArray(int size)
        {
            return new Logs[size];
        }
    };
}
