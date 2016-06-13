package com.hatcheryhub.countsteps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hatcheryhub.countsteps.helpers.Phantom;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HeightWeightActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_weight);

        final EditText et_feet = (EditText) findViewById(R.id.height_feet);
        final EditText et_inches = (EditText) findViewById(R.id.height_inches);
        final EditText et_weight = (EditText) findViewById(R.id.weight_kg);

        Button proceed = (Button) findViewById(R.id.proceed_btn);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int feet = Integer.parseInt(et_feet.getText().toString());
                int inches = Integer.parseInt(et_inches.getText().toString());
                int weight = Integer.parseInt(et_weight.getText().toString());

                inches += feet*12;
                int cms = (int) (inches*2.54);

                double bmi = (weight*10000)/(cms*cms);
                int lbs = (int) (weight*2.20462);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                String curr_date = df.format(c.getTime());
                SharedPreferences.Editor editor =
                        getSharedPreferences(Phantom.getMyPrefs(), MODE_PRIVATE).edit();
                editor.putInt("height", cms);
                editor.putInt("weight", lbs);
                editor.putString(Phantom.getInstance().getString(R.string.curr_date), curr_date);
                editor.putInt("credits", 0);
                editor.putInt("target", 10000);
                editor.commit();

                Intent i = new Intent(HeightWeightActivity.this, CountActivity.class);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("isfirst", "true");
                startActivity(i);
                finish();
            }
        });
    }

}
