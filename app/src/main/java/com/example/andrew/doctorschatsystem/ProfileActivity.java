package com.example.andrew.doctorschatsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userStatus , userStatus2;
    private CircleImageView profileImage;
    private Button sendRequestMessage;
    private RatingBar doctor_rate ;

    private DatabaseReference userRef, contactsRef;
    private FirebaseAuth mAuth;

    private String receivedUserID, senderUserID , img;
    private boolean isDoctor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receivedUserID = getIntent().getExtras().getString("visit_user_id");
        initializeFields();

        userStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDoctor)
                {
                    Intent intent = new Intent(ProfileActivity.this , PreviewMHActivity.class);
                    intent.putExtra("userID_MH" , receivedUserID);
                    startActivity(intent);
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(img))
                    img= "default" ;

                Intent intent = new Intent(ProfileActivity.this, ImageFullScreenActivity.class);
                intent.putExtra("ImageUri" , img);
                startActivity(intent);
            }
        });
    }

    private void initializeFields() {
        userRef = FirebaseDatabase.getInstance().getReference();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mAuth = FirebaseAuth.getInstance();

        senderUserID = mAuth.getCurrentUser().getUid();
        isDoctor = false ;

        userName = (TextView) findViewById(R.id.tv_profile_userName);
        userStatus = (TextView) findViewById(R.id.tv_profile_status);
        userStatus2 = (TextView) findViewById(R.id.tv_profile_status2);
        profileImage = (CircleImageView) findViewById(R.id.profile_ProfileImage);
        sendRequestMessage = (Button) findViewById(R.id.btn_profile_sendChatRequest);
        doctor_rate = (RatingBar)findViewById(R.id.DoctorProfile_ratingBar);

        retrieveDoctorInfo();
    }

    private void retrieveDoctorInfo() {

        userRef.child("Doctors").child(receivedUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    isDoctor = true ;
                    userStatus.setVisibility(View.INVISIBLE);
                    userStatus.setEnabled(false);

                    String rate ;
                    doctor_rate.setVisibility(View.VISIBLE);

                    if (dataSnapshot.hasChild("image"))
                    {
                        img = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(img).placeholder(R.drawable.profile_image).into(profileImage);
                    }

                    String name = dataSnapshot.child("uName").getValue().toString();
                    String status = dataSnapshot.child("uStatus").getValue().toString();

                    userName.setText(name);
                    userStatus2.setText(status);
                    if(dataSnapshot.hasChild("doctorRate"))
                        rate = dataSnapshot.child("doctorRate").getValue().toString();

                    else
                    {
                        rate = "0" ;
                        doctor_rate.setVisibility(View.GONE);
                   }

                    float DoctorRate = Float.parseFloat(rate) ;
                    DoctorRate = Math.round(DoctorRate * 10) / 10 ;
                    doctor_rate.setRating(DoctorRate);

                    manageChatRequest();
                }else {

                    userRef.child("Users").child(receivedUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                isDoctor = false ;
                                if (dataSnapshot.hasChild("image")) {
                                     img = dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(img).placeholder(R.drawable.profile_image).into(profileImage);
                                }
                                String name = dataSnapshot.child("uName").getValue().toString();
                                String status = dataSnapshot.child("uStatus").getValue().toString();

                                userName.setText(name);
                                userStatus2.setText(status);

                                manageChatRequest();
                            } else
                                Toast.makeText(ProfileActivity.this, "Does not exist", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {

        contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(receivedUserID))
                {
                    sendRequestMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmRemoveContact();
                        }
                    });
                }
                else
                {
                    sendRequestMessage.setVisibility(View.INVISIBLE);
                    sendRequestMessage.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void confirmRemoveContact() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext() , R.style.AlertDialog);
        builder.setTitle("   Remove "+userName.getText().toString()+" from your contacts ?   ");

        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeContact();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void removeContact() {
        contactsRef.child(senderUserID).child(receivedUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receivedUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ProfileActivity.this, "Contact has been removed", Toast.LENGTH_SHORT).show();

                                            } else
                                                Toast.makeText(ProfileActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else
                            Toast.makeText(ProfileActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                });
    }
}
