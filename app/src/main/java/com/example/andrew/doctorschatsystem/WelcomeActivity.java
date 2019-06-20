package com.example.andrew.doctorschatsystem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseUser currentUser ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
        int SPLASH_TIME_OUT = 1000;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED)
        {
            Toast toast = Toast.makeText(this, "NO internet connection ! some features will be not available",
                    Toast.LENGTH_LONG);

            toast.getView().setBackgroundColor(Color.rgb(100,100,100));
            TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
            view.setTextColor(Color.WHITE);

            toast.show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentUser != null)
                {
                    startActivity(new Intent(WelcomeActivity.this , ChatMainActivity.class));
                    overridePendingTransition(R.anim.fade_in , R.anim.fade_out);
                    finish();
                }
                else
                {
                    startActivity(new Intent(WelcomeActivity.this , log_in.class));
                    overridePendingTransition(R.anim.fade_in , R.anim.fade_out);
                    finish();
                }

            }
        }, SPLASH_TIME_OUT);

    }
}
