package com.hatcheryhub.countsteps.helpers;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.orm.SugarApp;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.CookieHandler;
import java.net.CookieManager;

/**
 * Created by aditya on 30/9/15.
 */
public class Phantom extends SugarApp {

    public static CookieManager cookieManager;
    private static String MY_PREFS = "CONTYXT_MY_PREFS";
    private static Phantom instance;

    private static RequestQueue volleyRequestQueue;

    private static String baseURL = "http://khannarah.comxa.com";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        Log.i("[#]App", "Created");
        volleyRequestQueue = Volley.newRequestQueue(Phantom.getInstance());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static Phantom getInstance() {
        return instance;
    }

    public static String getMyPrefs() {
        return MY_PREFS;
    }

    public static CookieManager getCookieManager() {
        return cookieManager;
    }

    public static void setMyPrefs(String myPrefs) {
        MY_PREFS = myPrefs;
    }

    public static RequestQueue getVolleyRequestQueue() {
        return volleyRequestQueue;
    }

    public static String getBaseURL() {
        return baseURL;
    }

    /*public static void setTaskDescription(Activity activity, String label) {
        Bitmap icon = BitmapFactory.decodeResource(instance.getResources(),
                R.drawable.logo_icon_colored);

        TypedValue typedValue = new TypedValue();
        int color = typedValue.data;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(label, icon, color);
            ((Activity) activity).setTaskDescription(taskDescription);
        }
    }
*/

    public static JSONArray removeJsonObjectAtJsonArrayIndex(JSONArray source, int index) throws JSONException {
        if (index < 0 || index > source.length() - 1) {
            throw new IndexOutOfBoundsException();
        }

        final JSONArray copy = new JSONArray();
        for (int i = 0, count = source.length(); i < count; i++) {
            if (i != index) copy.put(source.get(i));
        }
        return copy;
    }


}
