package com.example.andrew.doctorschatsystem;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.v4.content.FileProvider.getUriForFile;

public class PrivateChatActivity extends AppCompatActivity {

    private String chatWith_ID , senderID , pictureFilePath , fileName;
    private final List<Messages>messagesList = new ArrayList<>();
    private MessageAdapter messageAdapter ;
    private static final int GALLERY_REQUEST = 555 , CAMERA_REQUEST = 666 ,
            DOC_REQUEST = 444 , REQUEST_STARAGE = 333 , PDF_REQUEST = 222;

    private TextView bar_userName , bar_userLastSeen ;
    private CircleImageView bar_userImage;
    private EditText privateChat_input ;
    private ImageButton btn_send_privateChat , btn_sendImage , btn_sendFile;
    private RecyclerView userMessagesList ;

    private FirebaseAuth mAuth ;
    private DatabaseReference rootRef ;
    private StorageReference ChatPictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        chatWith_ID = getIntent().getExtras().getString("chatWith_ID");
        String chatWith_name = getIntent().getExtras().getString("chatWith_name");
        String chatWith_image = getIntent().getExtras().getString("chatWith_image");

        initializeFields();

        bar_userName.setText(chatWith_name);
        Picasso.get().load(chatWith_image).placeholder(R.drawable.profile_image).into(bar_userImage);

        btn_send_privateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        bar_userName.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                messageAdapter.clearAdapter();
                Intent intent = new Intent(PrivateChatActivity.this, ProfileActivity.class);
                intent.putExtra("visit_user_id", chatWith_ID);
                startActivity(intent);
            }
        });

        bar_userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageAdapter.clearAdapter();
                Intent intent = new Intent(PrivateChatActivity.this , ProfileActivity.class);
                intent.putExtra("visit_user_id" , chatWith_ID) ;
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        btn_sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PrivateChatActivity.this , R.style.AlertDialog);
                builder.setTitle("Selection options");
                builder.setMessage("Which way you need to get picture ?");
                builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(checkPermission())
                            dispatchTakePictureIntent() ;
                        else
                            requestPermission();
                    }
                }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent galleryIntent = new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, GALLERY_REQUEST);
                    }
                }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
            }
        });

        btn_sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PrivateChatActivity.this , R.style.AlertDialog);
                builder.setTitle("Selection options");
                builder.setMessage("Which way you need to get your file ?");
                builder.setPositiveButton("PDF", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getPDF();
                    }
                }).setNegativeButton("Other", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent docIntent = new Intent();
                        docIntent.setAction(Intent.ACTION_GET_CONTENT);
                        docIntent.setType("text/*");
                        startActivityForResult(docIntent , DOC_REQUEST);
                    }
                }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null)
            uploadImage(data.getData());

        else if (requestCode==CAMERA_REQUEST && resultCode == RESULT_OK )
        {
            File file = new File(pictureFilePath);

            if(file.exists())
                uploadImage(Uri.fromFile(file));
        }

        else if (requestCode == DOC_REQUEST && resultCode == RESULT_OK && data != null)
            uploadFile(data.getData() );

        else if (requestCode == PDF_REQUEST && resultCode == RESULT_OK && data != null)
            RequestFileName(data.getData());

    }

    @Override
    public void onBackPressed() {
        messageAdapter.clearAdapter();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        messageAdapter.clearAdapter();
        super.onStop();
    }

    private void initializeFields() {
        mAuth=FirebaseAuth.getInstance();
        senderID=mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        ChatPictures = FirebaseStorage.getInstance().getReference().child("ChatPictures");


        Toolbar chatToolbar = (Toolbar) findViewById(R.id.privateChat_toolBar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar , null);
        actionBar.setCustomView(actionBarView);

        bar_userImage =(CircleImageView)findViewById(R.id.chatBar_image);
        bar_userName = (TextView)findViewById(R.id.chatBar_name);
        bar_userLastSeen = (TextView)findViewById(R.id.chatBar_lastSeen);
        btn_send_privateChat = (ImageButton) findViewById(R.id.privateChat_send_msg_btn);
        btn_sendImage = (ImageButton) findViewById(R.id.privateChat_input_image);
        btn_sendFile = (ImageButton) findViewById(R.id.privateChat_input_file);
        privateChat_input = (EditText)findViewById(R.id.privateChat_input_message);
        userMessagesList = (RecyclerView)findViewById(R.id.privateChat_RecyclerView);
        userMessagesList.setLayoutManager(new LinearLayoutManager(this));

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList.setAdapter(messageAdapter);

        displayLastSeen();
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(senderID).child(chatWith_ID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.private_chat_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.privateChat_menu_visitProfile:
                messageAdapter.clearAdapter();
                Intent intent = new Intent(PrivateChatActivity.this, ProfileActivity.class);
                intent.putExtra("visit_user_id", chatWith_ID);
                startActivity(intent);

                return true;

            case R.id.privateChat_menu_recommendation:
                Intent intent1 = new Intent(PrivateChatActivity.this , FindDoctorActivity.class);
                intent1.putExtra("fd_senderID" , senderID);
                intent1.putExtra("fd_chatWithID" , chatWith_ID);
                startActivity(intent1);

                return true;

            case R.id.privateChat_menu_chatGallery:
                Intent intent2 = new Intent(PrivateChatActivity.this , GalleryMainActivity.class);
                intent2.putExtra("SenderID" , senderID);
                intent2.putExtra("ChatWithID" , chatWith_ID);
                startActivity(intent2);

                return true;

            case R.id.privateChat_menu_files:
                Intent intent3 = new Intent(PrivateChatActivity.this , FilesGallery.class);
                intent3.putExtra("SenderID" , senderID);
                intent3.putExtra("ChatWithID" , chatWith_ID);
                startActivity(intent3);
                return true ;

            default:
                return false;
        }

    }

    private void sendMessage() {
        String messageText = privateChat_input.getText().toString().trim();
        if(!(TextUtils.isEmpty(messageText)))
        {
            String messageSenderRef = "Messages/"+senderID+"/"+chatWith_ID;
            String messageReceiverRef = "Messages/"+chatWith_ID+"/"+senderID;

            //that's will create a key for each message
            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(senderID)
                    .child(chatWith_ID).push();

            String messagePush_id = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message" , messageText);
            messageTextBody.put("type" , "text");
            messageTextBody.put("from" , senderID);


            Map  messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePush_id , messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePush_id , messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(!task.isSuccessful())
                        Toast.makeText(PrivateChatActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });
            privateChat_input.setText("");
        }
    }

    private void uploadImage(Uri resultUri){

        //that's will create a key for each message
        DatabaseReference userMessageKeyRef = rootRef.child("ImageMessages").child(senderID)
                .child(chatWith_ID).push();

        final String messagePush_id = userMessageKeyRef.getKey();

        final StorageReference filePath = ChatPictures.child(senderID).child(chatWith_ID).child(messagePush_id + ".jpg");

        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            sendImage(uri.toString() , messagePush_id , true);
                        }
                    });
                }
                else
                    Toast.makeText(PrivateChatActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void uploadFile(Uri resultUri) {

        final ProgressDialog dialog = new ProgressDialog(this) ;
        dialog.setTitle("Upload file");
        dialog.setMessage("This may take a while ...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        DatabaseReference userMessageKeyRef = rootRef.child("FilesMessages").child(senderID)
                .child(chatWith_ID).push();

        final String messagePush_id = userMessageKeyRef.getKey();

        final StorageReference filePath = ChatPictures.child(senderID).child(chatWith_ID).child(messagePush_id + ".txt");

        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            sendImage(uri.toString() , messagePush_id , false);
                        }
                    });
                }
                else
                    Toast.makeText(PrivateChatActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void sendImage(String imgPath , String messagePush_id , boolean isImage) {

        String messageSenderRef ;
        Map messageTextBody = new HashMap();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyy");
        String SendDate = dateFormat.format(calendar.getTime());

        if(isImage)
        {
            messageSenderRef = "ImageMessages/" + senderID + "/" + chatWith_ID;
            messageTextBody.put("imgLink", imgPath);
            messageTextBody.put("imgDate",SendDate);
            messageTextBody.put("from", senderID);
        }
        else
        {
            messageSenderRef = "FileMessages/" + senderID + "/" + chatWith_ID;
            messageTextBody.put("fileLink", imgPath);
            messageTextBody.put("fileDate",SendDate);
            messageTextBody.put("fileName", fileName);
        }

        //String messageReceiverRef = "ImageMessages/" + chatWith_ID + "/" + senderID;

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePush_id, messageTextBody);
        //messageBodyDetails.put(messageReceiverRef + "/" + messagePush_id, messageTextBody);

        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                    Toast.makeText(PrivateChatActivity.this, "Uploaded ", Toast.LENGTH_SHORT).show();

                else
                    Toast.makeText(PrivateChatActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

            }
        });
        // messageAdapter.clearAdapter();
        //  onStart();
    }

    private void displayLastSeen() {
        rootRef.child("Users").child(chatWith_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.child("userState").hasChild("state"))
                    {
                        String state = dataSnapshot.child("userState").child("state").getValue().toString();

                        if(state.equals("online"))
                            bar_userLastSeen.setText(" online ..");
                        else if(state.equals("offline"))
                        {
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();
                            bar_userLastSeen.setText("Last seen: "+date+" at "+time);
                            bar_userLastSeen.setTextSize(12);
                        }

                    }
                    else
                        bar_userLastSeen.setText("update the app");
                }
                else
                {
                    rootRef.child("Doctors").child(chatWith_ID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                if(dataSnapshot.child("userState").hasChild("state"))
                                {
                                    String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                    if(state.equals("online"))
                                        bar_userLastSeen.setText(" online ..");
                                    else if(state.equals("offline"))
                                    {
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                        bar_userLastSeen.setText("Last seen: "+date+" at "+time);
                                        bar_userLastSeen.setTextSize(12);
                                    }

                                }
                                else
                                    bar_userLastSeen.setText("update the app");
                            }
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

//----------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------

    private void getPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PDF_REQUEST);
    }

    private void RequestFileName(final Uri data) {

        AlertDialog.Builder builder = new AlertDialog.Builder(PrivateChatActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter File Name: ");

        final EditText fileNameField = new EditText(PrivateChatActivity.this);
        fileNameField.setHint("e.g Prescription 1");
        builder.setView(fileNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName= fileNameField.getText().toString();
                if (TextUtils.isEmpty(fileName))
                    Toast.makeText(PrivateChatActivity.this, "You must enter a name !", Toast.LENGTH_SHORT).show();
                else
                    uploadFile(data);
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

    private String createImageName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "IMG_"+timeStamp ;
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File pictureDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String pictureName = createImageName();

        File imageFile = null;
        try {
            imageFile = File.createTempFile(pictureName , ".jpg" , pictureDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pictureFilePath = imageFile.getAbsolutePath() ;
        Uri imageUri = getUriForFile(this , "com.example.andrew.doctorschatsystem",imageFile);
        takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryAddPic() ;
        startActivityForResult(takePictureIntent , CAMERA_REQUEST);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(pictureFilePath);
        mediaScanIntent.setData(Uri.fromFile(file));
        this.sendBroadcast(mediaScanIntent);
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(PrivateChatActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_STARAGE);
    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STARAGE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!cameraAccepted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                displayAlertMessage("You need to allow access", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_STARAGE);
                                    }
                                });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }
}
