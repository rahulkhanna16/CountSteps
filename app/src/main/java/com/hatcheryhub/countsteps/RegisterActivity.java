package com.hatcheryhub.countsteps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hatcheryhub.countsteps.helpers.Phantom;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;


import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.hatcheryhub.countsteps.models.User;


public class RegisterActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private CallbackManager callbackManager;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        private ProfileTracker mProfileTracker;

        @Override
        public void onSuccess(LoginResult loginResult) {
            if(Profile.getCurrentProfile() == null) {
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile profile, final Profile profile2) {
                        Toast.makeText(RegisterActivity.this, "Welcome " + profile2.getFirstName() + " " + profile2.getLastName(), Toast.LENGTH_LONG).show();
                        mProfileTracker.stopTracking();
                        GraphRequest request = GraphRequest.newMeRequest(
                                AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject userMe, GraphResponse response) {
                                        if(userMe!=null){
                                            //...... do your things
                                            Log.i("other user details", String.valueOf(userMe) + " " + String.valueOf(response));
                                            try {
                                                String email = userMe.getString("email");
                                                String dob = userMe.getString("birthday");
                                                int age = calculateAge(userMe.getString("birthday"));                                                String name = profile2.getFirstName() + " " + profile2.getLastName();
                                                String profilepic = String.valueOf(profile2.getProfilePictureUri(60, 60));
                                                User.saveUser(new User(name, email, "facebook", dob, profilepic, age));
                                                changeLoginStatus();

                                                Intent i = new Intent(RegisterActivity.this, HeightWeightActivity.class);
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(i);
                                                finish();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });

                        Bundle parameters = new Bundle();

                        parameters.putString("fields","email,birthday");
                        request.setParameters(parameters);

                        GraphRequest.executeBatchAsync(request);
                    }
                };
                mProfileTracker.startTracking();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_register);


        SharedPreferences sharedPref = Phantom.getInstance().getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        int login = sharedPref.getInt("loginstatus", 0);
        if(login == 1) {
            Intent i = new Intent(RegisterActivity.this, CountActivity.class);
            i.putExtra("isfirst", "false");
            startActivity(i);
            finish();
        }

//        Intent i = new Intent(RegisterActivity.this, CountActivity.class);
//        startActivity(i);

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "user_birthday"));
        loginButton.registerCallback(callbackManager, facebookCallback);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

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
                showProgressDialog();
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString();
                String confirm_password = et_confirm.getText().toString();
                String name = et_name.getText().toString();
                int age = 0;
                boolean error = false;
                try {
                    age = Integer.parseInt(et_age.getText().toString());
                }catch(NumberFormatException e) {
                    error = true;
                    et_age.setError("Enter valid age!");
                }

                if (email.isEmpty()) {
                    et_email.setError("Email is required!");
                    error = true;
                    return;
                }

                if (name.isEmpty()) {
                    et_name.setError("Name is required!");
                    error = true;
                    return;
                }

                if (password.isEmpty()) {
                    et_password.setError("Password is required!");
                    error = true;
                }

                if (confirm_password.isEmpty()) {
                    et_password.setError("Password is required!");
                    error = true;
                }

                else if(!error) {
                    if (age == 0) {
                        et_age.setError("Age is required!");
                        error = true;
                    }
                }

                if(!error) {
                    if (!confirm_password.equals(password)) {
                        et_password.setError("Confirm password!!");
                        et_confirm.setText("");
                    }
                    else {
                        User.saveUser(new User(name, email, "server", "null", "", age));

                        SharedPreferences sharedPref = Phantom.getInstance().getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(Phantom.getInstance().getString(R.string.user_password), password);
                        editor.commit();

                        changeLoginStatus();

                        hideProgressDialog();

                        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                }
                hideProgressDialog();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile profile, final Profile profile2) {
                    // profile2 is the new profile
                    mProfileTracker.stopTracking();
                    Toast.makeText(RegisterActivity.this, "Welcome " + profile2.getFirstName() + " " + profile2.getLastName(), Toast.LENGTH_LONG).show();
                    Log.v("facebook - profile", profile2.getFirstName() + " " + String.valueOf(profile2.getProfilePictureUri(60, 60)));
//                    User.saveUser(profile2.getFirstName() + " " + profile2.getLastName(), profile2.getProfilePictureUri(60, 60).toString(), );
                    GraphRequest request = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject userMe, GraphResponse response) {
                                    if(userMe!=null){
                                        //...... do your things
                                        Log.i("other user details", String.valueOf(userMe) + " " + String.valueOf(response));
                                        try {
                                            String email = userMe.getString("email");
                                            String dob = userMe.getString("birthday");
                                            int age = calculateAge(userMe.getString("birthday"));
                                            String name = profile2.getFirstName() + " " + profile2.getLastName();
                                            String profilepic = String.valueOf(profile2.getProfilePictureUri(60, 60));
                                            User.saveUser(new User(name, email, "facebook", dob, profilepic, age));
                                            changeLoginStatus();
                                            Intent i = new Intent(RegisterActivity.this, HeightWeightActivity.class);
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(i);
                                            finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });

                    Bundle parameters = new Bundle();

                    parameters.putString("fields","email,birthday");
                    request.setParameters(parameters);

                    GraphRequest.executeBatchAsync(request);
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


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        if (requestCode == RC_SIGN_IN) {
            if(!mGoogleApiClient.isConnecting()||!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

//
//    private void register() {
//
//
////        progressDialogToggle(true);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                Log.i("entered", "register");
//
//                JSONObject jsonObject = new JSONObject();
//                JSONArray jsonArray = new JSONArray();
////
////                try {
////                    jsonObject.put("username", email);
////                    jsonObject.put("password", password);
////                    jsonObject.put("name", name);
////                    jsonObject.put("age", age);
////
////                    jsonArray.put(jsonObject);
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
//
//
//                final JSONObject postJsonObject = jsonObject;
//
//
//                String url = Phantom.getBaseURL() + "/Register.php";
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
//                                        Log.i("Success", "registered");
//
//                                    }
//                                } catch (JSONException e) {
//                                    error_status = true;
//                                    e.printStackTrace();
//                                }
//
//                                if (!error_status) {
//                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                                    if (sharedPreferences.getInt("loginstatus", 0) != 1) {
//                                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                                        editor.putString("email", email);
//                                        editor.putString("password", password);
//                                        editor.putString("name", name);
//                                        editor.putString("age", age);
//                                        editor.putInt("loginstatus", 1);
//                                        editor.commit();
//                                    }
////                                    loginNavigation();
//                                }
//
////                                progressDialogToggle(false);
//
//                            }
//                        }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i("[#]ERROR RESPONSE", String.valueOf(error));
////                        progressDialogToggle(false);
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
//                    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
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

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void signInWithGplus() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "onConnectionFailed:" + connectionResult, Toast.LENGTH_LONG).show();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
//            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
//            updateUI(true);
            Toast.makeText(this, "Welcome " + acct.getDisplayName(), Toast.LENGTH_SHORT).show();
            String name = acct.getDisplayName();
            String email = acct.getEmail();
            String profile_pic = acct.getPhotoUrl().toString();
            int age = 21;
            User.saveUser(new User(name, email, "google", "null",profile_pic, age));
            changeLoginStatus();

            Intent i = new Intent(RegisterActivity.this, HeightWeightActivity.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        } else {
            // Signed out, show unauthenticated UI.
//            updateUI(false);
            Toast.makeText(this, "Could not authenticate!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading..");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void changeLoginStatus() {

        SharedPreferences sharedPref = Phantom.getInstance().getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("loginstatus", 1);
        editor.commit();
    }

    private int calculateAge(String dob) {

        if(dob == null) {
            return 0;
        }
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String curr_date = df.format(c.getTime());

        Log.i("curr_date - birth_date", curr_date + " - " + dob);

        int curr_year = Integer.parseInt(curr_date.split("/")[2]);
        int birth_year = Integer.parseInt(dob.split("/")[2]);
        int age = curr_year - birth_year;

        if(Integer.parseInt(curr_date.split("/")[0])<Integer.parseInt(dob.split("/")[0]))
            age = age-1;
        else if(Integer.parseInt(curr_date.split("/")[1])<Integer.parseInt(dob.split("/")[1]))
            age = age-1;

        Log.i("age", age +"");
        return age;

    }
}

//589591934022-9qgms3ork4tpvblu7j6i8v0aj0rj823o.apps.googleusercontent.com