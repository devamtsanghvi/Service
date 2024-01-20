package com.example.service;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.os.PowerManager;
import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    Button but;
    EditText edit;

    String name_m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text = (TextView) findViewById(R.id.textView);

        SharedPreferences preferences = getSharedPreferences("phone", MODE_PRIVATE);

        name_m = preferences.getString("name_mobile", "");

        EditText edit = (EditText) findViewById(R.id.editTextText);
        Button but = (Button) findViewById(R.id.button);

        DatabaseReference check = FirebaseDatabase.getInstance().getReference("users");

        check.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()){
                        Log.i("check", String.valueOf(task.getResult().getValue()));
                    }
                    else {
                        Log.i("check", "not exists");
                    }
                }
            }
        });
        if(name_m.length()==0){
                edit.setVisibility(View.VISIBLE);
                but.setVisibility(View.VISIBLE);

        }

        else {
                Log.i("memmory", name_m);
                edit.setVisibility(View.INVISIBLE);
                but.setVisibility(View.INVISIBLE);
            }

        if (but.getVisibility()==View.VISIBLE){



            but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = edit.getText().toString();

                    if (text.length()==0){

                    }

                    else {
                        SharedPreferences preferences = getSharedPreferences("phone", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("name_mobile", text);
                        editor.apply();

                        name_m = text;
                        but.setVisibility(View.INVISIBLE);
                        edit.setVisibility(View.INVISIBLE);
                    }

                }
            });
        }





            ActivityCompat.requestPermissions( MainActivity.this , new String[]{Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);
        requestbatteryoptimization();
        Intent serviceIntent = new Intent(this, MyForegroundService.class);

        startForegroundService(serviceIntent);

        foregroundServiceRunning();
    }

    private void requestbatteryoptimization(){
        try {
            Intent intent = new Intent();
            String packageName = getPackageName();
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isbatteryoptimization(){
        boolean isIgnoring = false;
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    return isIgnoring;
    }



    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(MyForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}