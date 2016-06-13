package com.hatcheryhub.countsteps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hatcheryhub.countsteps.helpers.Phantom;
import com.hatcheryhub.countsteps.models.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private Toolbar toolbar;

    private RecyclerView navigationDrawerRecyclerView;                           // Declaring RecyclerView
    private RecyclerView.Adapter navigationDrawerAdapter;                        // Declaring Adapter For Recycler View
    private RecyclerView.LayoutManager navigationDrawerLayoutManager;            // Declaring Layout Manager as a linear layout manager
    private DrawerLayout navigationDrawer;                                  // Declaring DrawerLayout

    private Button open_gallery;
    private ActionBarDrawerToggle navigationDrawerToggle;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button registerButton;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        signInButton = (Button) findViewById(R.id.signInButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Login");

        signInButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.signInButton: {
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString();
//                authLogin(email, password);
                SharedPreferences sharedPreferences = Phantom.getInstance().getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
                User user = User.getUser();

                Log.i("email", email);
                Log.i("pass", password);
                Log.i("email", user.getEmail());
                Log.i("pass", sharedPreferences.getString(Phantom.getInstance().getString(R.string.user_password), null));

                if(email.equals(sharedPreferences.getString(Phantom.getInstance().getString(R.string.user_email), null))) {
                    if(password.equals(sharedPreferences.getString(Phantom.getInstance().getString(R.string.user_password), null))) {
                        loginNavigation();
                    }
                }
            }
            break;
            /*
            //TODO register activity
            case R.id.registerButton: {
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
            }
            break;*/
        }
    }
//
//    private void authLogin(final String email, final String password) {
//
//        if (email.isEmpty()) {
//            emailEditText.setError("Email is required!");
//            return;
//        }
//
//        if (password.isEmpty()) {
//            passwordEditText.setError("Password is required!");
//        }
//
//        progressDialogToggle(true);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//
//                JSONObject jsonObject = new JSONObject();
//                JSONArray jsonArray = new JSONArray();
//
//                try {
//                    jsonObject.put("email", email);
//                    jsonObject.put("password", password);
//
//                    jsonArray.put(jsonObject);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//                final JSONObject postJsonObject = jsonObject;
//
//
//                String url = Phantom.getBaseURL() + "/api_v5.php/login";
//                String uri = Uri.parse(url)
//                        .buildUpon()
//                        .build().toString();
//
//                // Request a string response from the provided URL.
//
//                StringRequest stringRequest = new StringRequest(Request.Method.POST, uri,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                // Display the first 500 characters of the response string.
//                                Log.i("[#]RESPONSE STATUS", "OK");
//                                Log.i("[#]RESPONSE", response);
//
//                                String id, username, register, profile_pic; //email is already entered by user
//                                boolean error_status = true;
//
//                                try {
//                                    JSONObject res = new JSONObject(response);
//                                    error_status = res.getBoolean("error");
//
//                                    if (!error_status) {
//                                        id = res.getString("id");
//                                        username = res.getString("username");
//                                        register = res.getString("register");
//                                        profile_pic = res.getString("profilepic");
//
//                                        User.saveUser(new User(id, email, username, register, profile_pic));  //saves user details in shared preferences
//                                        User.saveSessionId(User.getSessionCookie());  //Saves Cookie in shared preferences
//                                    }
//                                } catch (JSONException e) {
//                                    error_status = true;
//                                    e.printStackTrace();
//                                }
//
//                                if (!error_status) {
//                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                                    if(sharedPreferences.getInt("loginstatus",0)!=1) {
//                                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                                        editor.putString("email", email);
//                                        editor.putString("pass", password);
//                                        editor.putInt("loginstatus", 1);
//                                        editor.putString("contexts", "Arts,Food,Legal,Living,Tech,Work,Sports,Music,Movies,Social,Travel");
//                                        editor.commit();
//                                    }
//                                    loginNavigation();
//                                } else {
//                                    Snackbar.make(findViewById(android.R.id.content), "An error occurred. Please try again.", Snackbar.LENGTH_LONG)
//                                            .show();
//                                }
//
//                                progressDialogToggle(false);
//
//                            }
//                        }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i("[#]ERROR RESPONSE", String.valueOf(error));
//                        progressDialogToggle(false);
//
//                        int error_code;
//
//                        try {
//                            error_code = error.networkResponse.statusCode;
//                        } catch (Exception e) {
//                            error_code = -1;
//                        }
//
//                        if (error_code == 400) {
//                            Snackbar.make(findViewById(android.R.id.content), "Invalid Email or Password.", Snackbar.LENGTH_LONG)
//                                    .show();
//                        } else {
//                            Snackbar.make(findViewById(android.R.id.content), "Please check your internet connection.", Snackbar.LENGTH_LONG)
//                                    .show();
//                        }
//                    }
//                }) {
//                    protected Map<String, String> getParams() throws AuthFailureError {
//                        Map<String, String> params = new HashMap<String, String>();
//                        params.put("email", email);
//                        params.put("password", password);
//                        return params;
//                    }
//
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        Map<String, String> params = new HashMap<String, String>();
//                        params.put("Content-Type", "application/x-www-form-urlencoded");
//                        params.put("json", postJsonObject.toString());
//                        return params;
//                    }
//                };
//                // Add the request to the RequestQueue.
//                Log.i("check it", stringRequest.toString());
//                Phantom.getVolleyRequestQueue().add(stringRequest);
//
//            }
//        });
//
//    }


    private void progressDialogToggle(boolean state) {
        String email = emailEditText.getText().toString().trim();

        if (state) {
            progressDialog.setMessage("Logging in as " + email);
            progressDialog.show();

        } else {
            progressDialog.setMessage("Welcome " + email);
            progressDialog.hide();
        }
    }

    //TODO
    private void loginNavigation() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        Intent intent = new Intent(this, HeightWeightActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
