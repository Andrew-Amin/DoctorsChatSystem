package com.example.andrew.doctorschatsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class resetPassword extends AppCompatActivity {

    private EditText email;
    private Button reset;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        email = (EditText) findViewById(R.id.et_resetPass);
        reset = (Button) findViewById(R.id.btn_resetPass);

        firebaseAuth = FirebaseAuth.getInstance();

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString().trim();
                if (TextUtils.isEmpty(userEmail))
                    Toast.makeText(resetPassword.this, "unfortunately we need a Email", Toast.LENGTH_SHORT).show();
                else {
                    firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(resetPassword.this, "Check your email account . . .", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(resetPassword.this, log_in.class));
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(resetPassword.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
