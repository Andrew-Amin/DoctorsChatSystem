package com.example.andrew.doctorschatsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private TextView  password_matching;
    private RelativeLayout register;
    private EditText Email, Password, Password2, medicalID;
    private ProgressDialog progressDialog;

    private boolean IsDoctor;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password_matching.setVisibility(View.INVISIBLE);
                SignUp();
            }
        });

        Password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password_matching.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkPassword2(s.toString());
            }
        });

        Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkPassword(s.toString());
            }
        });
    }

    private void initializeFields() {
        Email = (EditText) findViewById(R.id.register_UserName_id);
        Password = (EditText) findViewById(R.id.register_Password_id);
        Password2 = (EditText) findViewById(R.id.register_Password_id2);
        password_matching = (TextView) findViewById(R.id.matched_password);
        medicalID = (EditText) findViewById(R.id.DoctorRegister_Medical_id);
        register = (RelativeLayout) findViewById(R.id.register_SIGNIN_ID_relative_layout);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        IsDoctor = false;
    }

    private void SignUp() {

        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String passwordMatched = password_matching.getText().toString().trim();
        String mID = medicalID.getText().toString().trim();

        if (passwordMatched.equals("Matched"))
        {
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)|| TextUtils.isEmpty(mID))
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();

            else if (verifyDoctorCode(mID))
                DoctorSignUp(email , password);
        }
        else
            Toast.makeText(this, "your information is not complete", Toast.LENGTH_SHORT).show();
    }

    private void DoctorSignUp(String email, String password) {
        progressDialog.setMessage("Registering your account . . .");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String currentUserID = firebaseAuth.getCurrentUser().getUid();
                            rootRef.child("Doctors").child(currentUserID).setValue("");

                            rootRef.child("Doctors").child(currentUserID).child("device_token")
                                    .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //sendVerificationEmail();
                                        SendUserToMainActivity();
                                    }
                                    else
                                        Toast.makeText(RegisterActivity.this, "oops! , error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                            Toast.makeText(RegisterActivity.this, "oops! , error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            if (!(currentUser.isEmailVerified())) {
                currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "You Have registered Successfully", Toast.LENGTH_SHORT).show();
                            Toast.makeText(RegisterActivity.this, "you have to verify you email address ...", Toast.LENGTH_LONG).show();
                            firebaseAuth.signOut();
                            SendUserToLogInActivity();

                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            firebaseAuth.signOut();
                        }

                    }
                });
            }
        }
    }

    private boolean verifyDoctorCode(final String medicalCode) {
        rootRef.child("Doctors_Keys").child("New").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (medicalCode.equals(dataSnapshot.getValue().toString()))
                        IsDoctor = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return IsDoctor ;
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, ChatMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void SendUserToLogInActivity() {
        Intent intent = new Intent(RegisterActivity.this, log_in.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void checkPassword2(String newPass) {

        String pass1 = Password.getText().toString();

        if (!TextUtils.isEmpty(pass1) && pass1.equals(newPass)) {
            password_matching.setText("Matched");
            password_matching.setTextColor(Color.GREEN);
        } else {
            password_matching.setText("Not Matched");
            password_matching.setTextColor(Color.RED);
        }
    }

    private void checkPassword(String newPass) {

        String pass = Password2.getText().toString();

        if (!TextUtils.isEmpty(pass) && pass.equals(newPass)) {
            password_matching.setText("Matched");
            password_matching.setTextColor(Color.GREEN);
        } else {
            password_matching.setText("Not Matched");
            password_matching.setTextColor(Color.RED);
        }
    }
}
