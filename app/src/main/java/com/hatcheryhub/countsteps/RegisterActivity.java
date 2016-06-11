package com.hatcheryhub.countsteps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.hatcheryhub.countsteps.helpers.Phantom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class RegisterActivity extends AppCompatActivity implements OnConnectionFailedListener, ConnectionCallbacks, ResultCallback<People.LoadPeopleResult> {

    private CallbackManager callbackManager;
    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        private ProfileTracker mProfileTracker;

        @Override
        public void onSuccess(LoginResult loginResult) {
            if(Profile.getCurrentProfile() == null) {
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                        // profile2 is the new profile
                        Toast.makeText(RegisterActivity.this, "Welcome " + profile.getFirstName() + " " + profile.getLastName(), Toast.LENGTH_LONG).show();
                        Log.v("facebook - profile", profile2.getFirstName());
                        mProfileTracker.stopTracking();
                    }
                };
                mProfileTracker.startTracking();
                // no need to call startTracking() on mProfileTracker
                // because it is called by its constructor, internally.
            }
            else {
                Profile profile = Profile.getCurrentProfile();
                Log.v("facebook - profile", profile.getFirstName());
            }
        }
        @Override
        public void onCancel() {
            Toast.makeText(RegisterActivity.this, "Failed", Toast.LENGTH_LONG).show();

            Log.d("facebook", "failed");
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(RegisterActivity.this, "Failed", Toast.LENGTH_LONG).show();

            Log.d("facebook", "failed");
        }
    };

    private ProfileTracker mProfileTracker;
    private AccessTokenTracker accessTokenTracker;

    private String email, name, age, password;

    private EditText et_name, et_age, et_email, et_password, et_confirm;

    GoogleApiClient mGoogleApiClient;

    private ProgressDialog mProgressDialog;
    private ConnectionResult mConnectionResult;
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private SignInButton btnSignInGoogle;
    private TextView registerBtn;
    Intent google_data = null;
    Boolean flag = false;
    final private static int RC_SIGN_IN = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);

//        Intent i = new Intent(RegisterActivity.this, CountActivity.class);
//        startActivity(i);

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.registerCallback(callbackManager, facebookCallback);
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API,Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();


        btnSignInGoogle = (SignInButton) findViewById(R.id.google_plus_signin);
        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGplus();
            }
        });

        et_age = (EditText) findViewById(R.id.et_age);
        et_email = (EditText) findViewById(R.id.emailEditText);
        et_name = (EditText) findViewById(R.id.et_name);
        et_password = (EditText) findViewById(R.id.passwordEditText);
        et_confirm = (EditText) findViewById(R.id.et_confirm);

        registerBtn = (TextView) findViewById(R.id.register_tv);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = et_email.getText().toString().trim();
                password = et_password.getText().toString();
                name = et_name.getText().toString();
                age = et_age.getText().toString();


                Log.i("initiate", "register");
                register();
                if (email.isEmpty()) {
                    et_email.setError("Email is required!");
                    return;
                }

                if (name.isEmpty()) {
                    et_name.setError("Name is required!");
                    return;
                }

                if (age.isEmpty()) {
                    et_age.setError("Age is required!");
                }

                if (password.isEmpty()) {
                    et_password.setError("Password is required!");
                }
                else if(!et_confirm.getText().toString().isEmpty()) {
                    if (!et_confirm.getText().toString().equals(password)) {
                        et_password.setError("Confirm password!!");
                        et_confirm.setText("");
                    }
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        Profile profile = Profile.getCurrentProfile();
        if(profile!=null) {
            Log.i("welcome", profile.getFirstName() + "");
            Toast.makeText(this, "Welcome " + profile.getFirstName() + " " + profile.getLastName(), Toast.LENGTH_LONG).show();
        }
        if(Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                    // profile2 is the new profile
                    mProfileTracker.stopTracking();
                    Toast.makeText(RegisterActivity.this, "Welcome " + profile.getFirstName() + " " + profile.getLastName(), Toast.LENGTH_LONG).show();
                    Log.v("facebook - profile", profile2.getFirstName());

                }
            };
            mProfileTracker.startTracking();
            Log.v("yo", "yo");
            accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    accessTokenTracker.stopTracking();
                }
            };
            accessTokenTracker.startTracking();

        }

        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        if (requestCode == 0) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }
            mIntentInProgress = false;

            Toast.makeText(this, "onActivityResult: " + resultCode + " " + data.toString(), Toast.LENGTH_LONG).show();

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }


    private void register() {


//        progressDialogToggle(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.i("entered", "register");

                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                try {
                    jsonObject.put("username", email);
                    jsonObject.put("password", password);
                    jsonObject.put("name", name);
                    jsonObject.put("age", age);

                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                final JSONObject postJsonObject = jsonObject;


                String url = Phantom.getBaseURL() + "/Register.php";
                String uri = Uri.parse(url)
                        .buildUpon()
                        .build().toString();

                // Request a string response from the provided URL.

                StringRequest stringRequest = new StringRequest(Request.Method.POST, uri,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.i("[#]RESPONSE STATUS", "OK");
                                Log.i("[#]RESPONSE", response);

                                String id, username, register, profile_pic; //email is already entered by user
                                boolean error_status = true;

                                try {
                                    JSONObject res = new JSONObject(response);
                                    error_status = res.getBoolean("error");

                                    if (!error_status) {
                                        Log.i("Success", "registered");

                                    }
                                } catch (JSONException e) {
                                    error_status = true;
                                    e.printStackTrace();
                                }

                                if (!error_status) {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    if (sharedPreferences.getInt("loginstatus", 0) != 1) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("email", email);
                                        editor.putString("password", password);
                                        editor.putString("name", name);
                                        editor.putString("age", age);
                                        editor.putInt("loginstatus", 1);
                                        editor.commit();
                                    }
//                                    loginNavigation();
                                }

//                                progressDialogToggle(false);

                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("[#]ERROR RESPONSE", String.valueOf(error));
//                        progressDialogToggle(false);

                        int error_code;

                        try {
                            error_code = error.networkResponse.statusCode;
                        } catch (Exception e) {
                            error_code = -1;
                        }

                        if (error_code == 400) {
                            Snackbar.make(findViewById(android.R.id.content), "Invalid Email or Password.", Snackbar.LENGTH_LONG)
                                    .show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Please check your internet connection.", Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                }) {
                    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("email", email);
                        params.put("password", password);
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/x-www-form-urlencoded");
                        params.put("json", postJsonObject.toString());
                        return params;
                    }
                };
                // Add the request to the RequestQueue.
                Log.i("check it", stringRequest.toString());
                Phantom.getVolleyRequestQueue().add(stringRequest);

            }
        });

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Method to resolve any signin errors
     * */

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
                .setResultCallback(this);

    }

    @Override
    public void onConnectionSuspended(int arg0) {

        Toast.makeText(this, "connection suspended!", Toast.LENGTH_SHORT).show();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        Log.i("onResult", " " + peopleData.toString());
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                Toast.makeText(this, "Display name: " + personBuffer.get(0).getDisplayName(), Toast.LENGTH_SHORT).show();
                for (int i = 0; i < count; i++) {
                    Log.d("onResult", "Display name: " + personBuffer.get(i).getDisplayName());
                    Toast.makeText(this, "Display name: " + personBuffer.get(i).getDisplayName(), Toast.LENGTH_SHORT).show();
                }
            } finally {
                personBuffer.release();
            }
        } else {
            Log.e("onResult", "Error requesting visible circles: " + peopleData.getStatus());
        }
    }

}

//589591934022-9qgms3ork4tpvblu7j6i8v0aj0rj823o.apps.googleusercontent.com