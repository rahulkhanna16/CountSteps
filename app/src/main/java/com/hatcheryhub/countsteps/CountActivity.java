package com.hatcheryhub.countsteps;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hatcheryhub.countsteps.helpers.Phantom;
import com.hatcheryhub.countsteps.models.User;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CountActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    boolean activityRunning;
    private TextView count, tv_calories, tv_miles;
    private int stepsInSensor = 0;
    private String isFirst = "";
    private int stepsAtReset = 0;
    private Dialog profile_dialog;
    private int height, weight;
    boolean flag = false;
    private ImageButton play_pause;
    private TextView tv_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);

        isFirst = getIntent().getStringExtra("isfirst");

        initHW();

        SharedPreferences prefs = getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        stepsAtReset = prefs.getInt("stepsAtReset", 0);

        count = (TextView) findViewById(R.id.tv_count);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        initialiseDialog();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundResource(R.drawable.back_shade_blue);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_dialog.show();
            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryDark)));

        play_pause = (ImageButton) findViewById(R.id.play_pause);
        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation out = AnimationUtils.loadAnimation(CountActivity.this, android.R.anim.fade_out);
                Animation in = AnimationUtils.loadAnimation(CountActivity.this, android.R.anim.fade_in);

                play_pause.startAnimation(out);
                play_pause.setVisibility(View.INVISIBLE);

                if(tv_calories.getCurrentTextColor() != ContextCompat.getColor(CountActivity.this, R.color.grey)) {
                    play_pause.setImageResource(R.drawable.play_button);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
                    ll.setBackgroundResource(R.drawable.circle_background_unselected);
                    count.setTextColor(ContextCompat.getColor(CountActivity.this, R.color.grey));
                    tv_miles.setTextColor(ContextCompat.getColor(CountActivity.this, R.color.grey));
                    tv_calories.setTextColor(ContextCompat.getColor(CountActivity.this, R.color.grey));
                }
                else {
                    play_pause.setImageResource(R.drawable.pause_button);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
                    ll.setBackgroundResource(R.drawable.circle_background);
                    count.setTextColor(ContextCompat.getColor(CountActivity.this, R.color.red));
                    tv_miles.setTextColor(ContextCompat.getColor(CountActivity.this, R.color.green));
                    tv_calories.setTextColor(ContextCompat.getColor(CountActivity.this, R.color.blue));
                }

                play_pause.startAnimation(in);
                play_pause.setVisibility(View.VISIBLE);
            }
        });

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String curr_date = df.format(c.getTime());
        tv_date = (TextView)findViewById(R.id.date_tv);
        tv_date.setText(curr_date);
    }

    private void initialiseDialog() {

        profile_dialog = new Dialog(CountActivity.this);
        profile_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        profile_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        profile_dialog.setContentView(R.layout.profile_dialog);

        User profile = User.getUser();

        TextView age = (TextView) profile_dialog.findViewById(R.id.age_dialog);
        age.setText(profile.getAge() + "");

        TextView name = (TextView) profile_dialog.findViewById(R.id.user_name_tv);
        name.setText(profile.getName());

        ImageView profilepic = (ImageView) profile_dialog.findViewById(R.id.user_dp_iv);
        if(!profile.getLogin_type().equals("server")) {
            Picasso.with(this)
                    .load(profile.getProfile_pic())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.no_user)
                    .error(R.drawable.no_user)
                    .into(profilepic);
        }
        else{
            profilepic.setImageResource(R.drawable.no_user);
        }


        SharedPreferences prefs = getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);

        TextView height = (TextView) profile_dialog.findViewById(R.id.height_dialog);
        height.setText(prefs.getInt("height", 0) + "");

        TextView weight = (TextView) profile_dialog.findViewById(R.id.weight_dialog);
        weight.setText(prefs.getInt("weight", 0) + "");

        TextView credits = (TextView) profile_dialog.findViewById(R.id.credit_dialog);
        credits.setText(prefs.getInt("credits", 0) + "");

        if(prefs.getInt("weight", 0)!=0) {
            TextView bmi = (TextView) profile_dialog.findViewById(R.id.bmi_dialog);
            double cal_bmi = (prefs.getInt("weight", 0) * 10000) / (prefs.getInt("height", 0) * prefs.getInt("height", 0));
            bmi.setText(new DecimalFormat("###.##").format(cal_bmi));
        }

        final TextView target = (TextView) profile_dialog.findViewById(R.id.target_dialog);
        target.setText(prefs.getInt("target", 0) + "");

        final ImageButton edit_target = (ImageButton) profile_dialog.findViewById(R.id.editbtn_target);
        edit_target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) profile_dialog.findViewById(R.id.et_target);

                if(editText.getVisibility() == View.GONE) {
                    edit_target.setImageResource(R.drawable.pencil_green);
                    target.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                }
                else {
                    if(editText.getText().toString() != null) {
                        edit_target.setImageResource(R.drawable.pencil);
                        int steps = Integer.parseInt(editText.getText().toString());
                        SharedPreferences.Editor editor = getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE).edit();
                        editor.putInt("target", steps);
                        editor.commit();
                        target.setText(steps + "");
                        target.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        initHW();
        compareDate();
        SharedPreferences prefs = getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        stepsAtReset = prefs.getInt("stepsAtReset", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        compareDate();

        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if(countSensor!=null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(activityRunning) {
            float x = event.values[0];
            stepsInSensor = (int) x;
            if(isFirst.equals("true")) {
                stepsAtReset = stepsInSensor;
                count.setText(String.valueOf("0"));
                SharedPreferences.Editor editor =
                        getSharedPreferences(Phantom.getMyPrefs(), MODE_PRIVATE).edit();
                editor.putInt("stepsAtReset", stepsAtReset);
                editor.commit();
                isFirst = "false";
            }
            int stepsSinceReset = stepsInSensor - stepsAtReset;
            count.setText(String.valueOf(stepsSinceReset));
            if (flag)
                calculate(stepsSinceReset);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void compareDate() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String curr_date = df.format(c.getTime());
        tv_date.setText(curr_date);

        SharedPreferences prefs = getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        int credit = prefs.getInt("credits", 0);
        String past_date = prefs.getString(Phantom.getInstance().getString(R.string.curr_date), null);
        if(!curr_date.equals(past_date)) {
            SharedPreferences.Editor editor = prefs.edit();
            if(Integer.parseInt(count.getText().toString()) > 10000) {
                credit = credit + 1;
                editor.putInt("credits", credit);
            }
            editor.putString(Phantom.getInstance().getString(R.string.curr_date), curr_date);
            setStepsAtReset();
        }

    }

    private void setStepsAtReset() {
        stepsAtReset = stepsInSensor;

        SharedPreferences.Editor editor =
                getSharedPreferences(Phantom.getMyPrefs(), MODE_PRIVATE).edit();
        editor.putInt("stepsAtReset", stepsAtReset);
        editor.commit();

        count.setText("0");
    }

    private void calculate(int steps) {
        double mile = steps * stride * 6.21 / 1000000;
        int calories = (int) (calburnpermile * mile);

        if(tv_calories!=null && tv_miles!=null) {
            tv_calories.setText(calories + "");
            tv_miles.setText(new DecimalFormat("###.##").format(mile));
        }
    }

    private void initHW() {
        tv_date = (TextView)findViewById(R.id.date_tv);
        tv_calories = (TextView) findViewById(R.id.calories_tv);
        tv_miles = (TextView) findViewById(R.id.mile_tv);
        SharedPreferences prefs = getSharedPreferences(Phantom.getMyPrefs(), Context.MODE_PRIVATE);
        height = prefs.getInt("height", 0);
        weight = prefs.getInt("weight", 0);
        calburnpermile = weight * 0.57;
        stride = 0.414 * height;
        Log.i("height/weight", height+"/"+weight);
        flag = true;
    }
    double calburnpermile, stride;


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}

/*
input: steps, height, weight

stride = height in cms x 0.414

mile = step x stride (convert to mile)

calburn/mile = weight x 0.57

cal burned = cal burned/mile x mile

target steps, credit
*/