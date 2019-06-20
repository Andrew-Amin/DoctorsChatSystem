package com.example.andrew.doctorschatsystem;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FilesGallery extends AppCompatActivity {
    private ListView filesList ;
    private TextView noDoc ;

    private ArrayAdapter<String> listAdapter ;
    private ArrayList <String> links ;

    private DatabaseReference filesRef ;

    private String ChatWithID ,SenderID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_gallery);

        SenderID = Objects.requireNonNull(getIntent().getExtras()).getString("SenderID");
        ChatWithID = Objects.requireNonNull(getIntent().getExtras()).getString("ChatWithID");

        initialization();

        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new DownloadTask(FilesGallery.this , links.get(position) , (String)parent.getItemAtPosition(position));
            }
        });
    }

    private void initialization() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.fileGallery_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Received Files");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noDoc = (TextView)findViewById(R.id.fileGallery_tv_noDoc);
        listAdapter = new ArrayAdapter<String>(this , android.R.layout.simple_list_item_1);
        filesList = (ListView) findViewById(R.id.fileGallery_ListView);
        filesList.setAdapter(listAdapter);

        links = new ArrayList<>() ;

        filesRef = FirebaseDatabase.getInstance().getReference().child("FileMessages")
                .child(ChatWithID).child(SenderID);

        filesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(noDoc.getVisibility()==View.VISIBLE)
                    noDoc.setVisibility(View.GONE);

                RetrieveFiles(dataSnapshot);
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

    private void RetrieveFiles (DataSnapshot snapshot) {
        Iterator files = snapshot.getChildren().iterator() ;
        String fileName , fileDate , fileLink ;
        while (files.hasNext())
        {
           fileDate = ((DataSnapshot)files.next()).getValue().toString();
           fileLink= ((DataSnapshot)files.next()).getValue().toString();
           fileName= ((DataSnapshot)files.next()).getValue().toString();

            listAdapter.add(fileName+"_at_"+fileDate);
            links.add(fileLink);
        }
    }

}
