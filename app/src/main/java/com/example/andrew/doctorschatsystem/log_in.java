package com.example.andrew.doctorschatsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class log_in extends AppCompatActivity {

    private EditText userName, password;
    private CheckBox remember;
    private ProgressDialog progressDialog;

    private String srt_username, str_password;
    private Boolean saveLogin , emailAddressVerified;

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Doctors");

        userName = (EditText) findViewById(R.id.Login_UserName_id);
        password = (EditText) findViewById(R.id.login_Password_id);
        remember = (CheckBox) findViewById(R.id.cb_rememberMe_id);

        RelativeLayout signIN = (RelativeLayout) findViewById(R.id.SIGNIN_ID_relative_layout);
        TextView signUP = (TextView) findViewById(R.id.tv_signUp_id);
        TextView forgetPass = (TextView) findViewById(R.id.tv_forgetPassword);


        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            userName.setText(loginPreferences.getString("username", ""));
            password.setText(loginPreferences.getString("password", ""));
            remember.setChecked(true);
        }

        // authentication
        signIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //remember me
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(userName.getWindowToken(), 0);

                srt_username = userName.getText().toString();
                str_password = password.getText().toString();

                if (remember.isChecked()) {
                    loginPrefsEditor.putBoolean("saveLogin", true);
                    loginPrefsEditor.putString("username", srt_username);
                    loginPrefsEditor.putString("password", str_password);
                    loginPrefsEditor.commit();
                } else {
                    loginPrefsEditor.clear();
                    loginPrefsEditor.commit();
                }
                online();
            }

        });


        signUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(log_in.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(log_in.this, resetPassword.class);
                startActivity(intent);
            }
        });

    }

    private void online() {
        String email = userName.getText().toString().trim();
        String pass = password.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "unfortunately we need your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "unfortunately we need a password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Logging , please wait . . .");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(log_in.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            final String currentUserID = firebaseAuth.getCurrentUser().getUid();
                            Query searchQuery = userRef.orderByChild("uID").startAt(currentUserID).endAt(currentUserID + "\uf8ff");
                            searchQuery.addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        userRef.child(currentUserID).child("device_token")
                                                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    // VerifyEmailAddress();
                                                    Toast.makeText(log_in.this, "Welcome " + userName.getText().toString(), Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                    SendUserToMainActivity();
                                                }
                                            }
                                        });
                                    }

                                    else
                                    {
                                        Toast.makeText(log_in.this, "This account are not allowed here !!", Toast.LENGTH_LONG).show();
                                        firebaseAuth.signOut();
                                        progressDialog.dismiss();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else
                        {
                            String msg = task.getException().toString();
                            Toast.makeText(log_in.this, msg, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(log_in.this, ChatMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void VerifyEmailAddress() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        emailAddressVerified = user.isEmailVerified();

        if(emailAddressVerified)
        {
            Toast.makeText(log_in.this, "Welcome " + userName.getText().toString(), Toast.LENGTH_LONG).show();
            SendUserToMainActivity();
        }
        else
        {
            Toast.makeText(this, "Verify your email account first ...", Toast.LENGTH_LONG).show();
            firebaseAuth.signOut();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        moveTaskToBack(true) ;
    }

}