package com.example.andrew.doctorschatsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar ;
    private ImageButton btnSendMsg ;
    private EditText userInputMsg ;
    private TextView displayTextMsg;
    private ScrollView mScrollView;
    private String currentGroupName , currentUserID , currentUserName ,
             currentDate , currentTime ;

    private FirebaseAuth mAuth ;
    private DatabaseReference usersRef , groupNameRef , groupMessageKeyRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initializeFields();
        getUserInfo();

        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToDatabase();
                userInputMsg.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                    retrieveConversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                    retrieveConversation(dataSnapshot);
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
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(GroupChatActivity.this , ChatMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void initializeFields() {
        currentGroupName = getIntent().getExtras().getString("group_name") ;

        mToolbar = (Toolbar)findViewById(R.id.groupChat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Doctors");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        btnSendMsg = (ImageButton) findViewById(R.id.send_msg_btn);
        userInputMsg = (EditText)findViewById(R.id.input_group_message);
        displayTextMsg = (TextView)findViewById(R.id.groupChat_text_display);
        mScrollView = (ScrollView)findViewById(R.id.scroll_view1);
    }

    private void retrieveConversation(DataSnapshot dataSnapshot) {
        Iterator iterable = dataSnapshot.getChildren().iterator();

        while (iterable.hasNext())
        {
            String date = ((DataSnapshot)iterable.next()).getValue().toString();
            String msg = ((DataSnapshot)iterable.next()).getValue().toString();
            String time = ((DataSnapshot)iterable.next()).getValue().toString();
            String uName = ((DataSnapshot)iterable.next()).getValue().toString();

            displayTextMsg.append(uName +" :"+"\n" +" --> "+msg+"\n\n"+"                                " +
                    time+"  "+date+"\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    currentUserName = dataSnapshot.child("uName").getValue().toString() ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessageToDatabase() {
        String msg = userInputMsg.getText().toString().trim();
        String msgKey = groupNameRef.push().getKey();
        if(!TextUtils.isEmpty(msg))
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyy");
            currentDate = dateFormat.format(calendar.getTime());

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = timeFormat.format(calendar.getTime());

            HashMap<String , Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(msgKey);
            HashMap<String , Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("uName" , currentUserName);
                messageInfoMap.put("message" , msg);
                messageInfoMap.put("date" , currentDate);
                messageInfoMap.put("time" , currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }
}
