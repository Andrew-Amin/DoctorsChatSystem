package com.example.andrew.doctorschatsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button btn_updateProfile;
    private ImageButton editProfilePicture ;
    private EditText userName, userStatus;
    private CircleImageView profilePicture;
    private ProgressDialog loadingBar;

    private FirebaseAuth mauth;
    private DatabaseReference rootRef;
    private StorageReference userProfilePicturesRef;

    private Uri resultUri ;
    private String currentUser , imgPath;
    private boolean updateImage , success , hasBasics , firstTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initializeFields();

        retrieveUserInfo();

        btn_updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateProfileSettings();

                if(updateImage)
                    UpdateProfileImage();

                if (success)
                    SendUserToMainActivity();
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ImageFullScreenActivity.class);
                intent.putExtra("ImageUri" , imgPath);
                startActivity(intent);
            }
        });

        editProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1).start(SettingActivity.this);
            }
        });

    }

   /* @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(TextUtils.isEmpty(userName.getText().toString() ))
            Toast.makeText(this, "Insert your name first", Toast.LENGTH_SHORT).show();

        else
            startActivity(new Intent(this , ChatMainActivity.class));
    }*/


    @Override
    public void onBackPressed() {
        if(!hasBasics)
        {
            if(firstTime)
            {
                Toast.makeText(this, "this account will be deleted ", Toast.LENGTH_SHORT).show();
                firstTime=false ;
            }
            else
            {
                rootRef.child("Doctors").child(currentUser).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            assert user != null;
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(!task.isSuccessful())
                                        Toast.makeText(SettingActivity.this, "we need your name to avoid delete this account", Toast.LENGTH_SHORT).show();
                                }
                            });
                            SendUserToLogInActivity();
                        }
                    }
                });
            }
        }
        else
            SendUserToMainActivity();
    }



    private void initializeFields() {
        mauth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = mauth.getCurrentUser().getUid();
        userProfilePicturesRef = FirebaseStorage.getInstance().getReference().child("ProfilePictures");

        btn_updateProfile = (Button) findViewById(R.id.set_btn_save);
        userName = (EditText) findViewById(R.id.set_et_userName);
        userStatus = (EditText) findViewById(R.id.set_et_status);
        profilePicture = (CircleImageView) findViewById(R.id.set_profile_image);
        editProfilePicture = (ImageButton) findViewById(R.id.setting_btn_changeImage);
        loadingBar = new ProgressDialog(this);

        updateImage = success  = false ;
        hasBasics = firstTime =true ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profilePicture.setImageURI(resultUri);
                updateImage = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void retrieveUserInfo() {
        rootRef.child("Doctors").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("uName"))
                {
                    if(dataSnapshot.hasChild("image"))
                    {
                         imgPath = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(imgPath).placeholder(R.drawable.profile_image)
                                .error(R.drawable.no_image).into(profilePicture);
                    }

                    userName.setText(dataSnapshot.child("uName").getValue().toString());
                    userStatus.setText(dataSnapshot.child("uStatus").getValue().toString());
                }

                else
                {
                    Toast.makeText(SettingActivity.this, "please , set & update your information ", Toast.LENGTH_SHORT).show();
                    hasBasics = false ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void UpdateProfileSettings() {
        String user_name = userName.getText().toString();
        String user_status = userStatus.getText().toString();

        if (TextUtils.isEmpty(user_status))
            Toast.makeText(this, "Speciality is mandatory", Toast.LENGTH_SHORT).show();

        else if (TextUtils.isEmpty(user_name))
            Toast.makeText(this, "please, enter your user name", Toast.LENGTH_SHORT).show();

        else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uID", currentUser);
            profileMap.put("uName", user_name);
            profileMap.put("uStatus", user_status);

            rootRef.child("Doctors").child(currentUser).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SettingActivity.this, "profile has updated successfully", Toast.LENGTH_SHORT).show();
                        hasBasics = true ;

                    } else {
                        Toast.makeText(SettingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void UpdateProfileImage () {
        final StorageReference filePath = userProfilePicturesRef.child(currentUser + ".jpg");

        loadingBar.setTitle("Updating profile");
        loadingBar.setMessage(" wait until update your profile ...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            rootRef.child("Doctors").child(currentUser).child("image").setValue(uri.toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (!task.isSuccessful())
                                                Toast.makeText(SettingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                }
                else
                    Toast.makeText(SettingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
                success = true ;
            }
        });
    }

    private int getRate(String DoctorID) {
        final int[] rate = {-1};
        rootRef.child("Doctors").child(DoctorID).child("DoctorRate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                     rate[0] = Integer.valueOf(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return rate[0] ;
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(SettingActivity.this, ChatMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void SendUserToLogInActivity() {
        Intent intent = new Intent(SettingActivity.this, log_in.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
