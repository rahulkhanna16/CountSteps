package com.hatcheryhub.countsteps.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hatcheryhub.countsteps.R;
import com.hatcheryhub.countsteps.helpers.Phantom;

/**
 * Created by rahulkhanna on 12/06/16.
 */
public class User {
    private String name;
    private String email;
    private String login_type;
    private String dob;
    private String profile_pic;
    int age;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLogin_type() {
        return login_type;
    }

    public String getDob() {
        return dob;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public int getAge() {
        return age;
    }

    public User(String name, String email, String login_type, String dob, String profile_pic, int age) {
        this.name = name;
        this.email = email;
        this.login_type = login_type;
        this.dob = dob;
        this.profile_pic = profile_pic;
        this.age = age;
    }

    public static void saveUser (User user) {
        SharedPreferences sharedPref = Phantom.getInstance().getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Phantom.getInstance().getString(R.string.user_name), user.getName());
        editor.putString(Phantom.getInstance().getString(R.string.user_email), user.getEmail());
        editor.putString(Phantom.getInstance().getString(R.string.user_profilepic), user.getProfile_pic());
        editor.putString(Phantom.getInstance().getString(R.string.user_dob), user.getDob());
        editor.putString(Phantom.getInstance().getString(R.string.user_logintype), user.getLogin_type());
        editor.putInt(Phantom.getInstance().getString(R.string.user_age), user.getAge());
        editor.commit();

        Log.i("#User saved", user.getName() + " " + user.getEmail() + " " + user.getAge() + " " + user.getLogin_type());
    }

    public static User getUser() {
        User user;
        String name, email, dob, profile_pic, login_type;
        int age;

        SharedPreferences sharedPref = Phantom.getInstance().getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        name = sharedPref.getString(Phantom.getInstance().getString(R.string.user_name), null);
        email = sharedPref.getString(Phantom.getInstance().getString(R.string.user_email), null);
        age = sharedPref.getInt(Phantom.getInstance().getString(R.string.user_age), 0);
        dob = sharedPref.getString(Phantom.getInstance().getString(R.string.user_dob), null);
        login_type = sharedPref.getString(Phantom.getInstance().getString(R.string.user_logintype), null);
        profile_pic = sharedPref.getString(Phantom.getInstance().getString(R.string.user_profilepic), null);

        user = new User(name, email, login_type, dob, profile_pic, age);
        return user;
    }
}
