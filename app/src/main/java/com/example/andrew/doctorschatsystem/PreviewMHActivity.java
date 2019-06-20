package com.example.andrew.doctorschatsystem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PreviewMHActivity extends AppCompatActivity {

    private TextView age , gender , weight , smoking , alcohol , diabetes , hyper , psych;

    private DatabaseReference medicalHistoryRef ;

    private String userID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_mh);

        userID = Objects.requireNonNull(getIntent().getExtras()).getString("userID_MH");

        initializeFields();
    }

    @Override
    protected void onStart() {
        super.onStart();

        medicalHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                   age.setHint(dataSnapshot.child("Age").getValue().toString());
                   gender.setHint(dataSnapshot.child("Gender").getValue().toString());
                   weight.setHint(dataSnapshot.child("weight").getValue().toString());
                   smoking.setHint(dataSnapshot.child("Smoking").getValue().toString());
                   alcohol.setHint(dataSnapshot.child("Alcohol").getValue().toString());
                   diabetes.setHint(dataSnapshot.child("diabetes").getValue().toString());
                   hyper.setHint(dataSnapshot.child("hyper").getValue().toString());
                   psych.setHint(dataSnapshot.child("Psychiatric Disorders").getValue().toString());
                }
                else
                    Toast.makeText(PreviewMHActivity.this, "NO medical history", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {

        Toolbar toolbar = (Toolbar)findViewById(R.id.preview_MH_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Medical History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        medicalHistoryRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("Medical History");

        age = (TextView)findViewById(R.id.preview_MH_age);
        gender = (TextView)findViewById(R.id.preview_MH_gender);
        weight = (TextView)findViewById(R.id.preview_MH_weight);
        smoking = (TextView)findViewById(R.id.preview_MH_smoking);
        alcohol = (TextView)findViewById(R.id.preview_MH_Alcohol);
        diabetes = (TextView)findViewById(R.id.preview_MH_diabetes);
        hyper = (TextView)findViewById(R.id.preview_MH_hyper);
        psych = (TextView)findViewById(R.id.preview_MH_psych);
    }



}
