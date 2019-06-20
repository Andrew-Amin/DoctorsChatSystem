package com.example.andrew.doctorschatsystem;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View root;
    private ListView groups_lv;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> groupsList = new ArrayList<>();
    private FirebaseAuth mAuth ;

    private DatabaseReference groupsRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_groups, container, false);

        initializeFields();
        retrieveAndDisplayGroups();

        groups_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String groupName = parent.getItemAtPosition(position).toString();

                Intent intent = new Intent(getContext() , GroupChatActivity.class);
                intent.putExtra("group_name" , groupName);
                startActivity(intent);
            }
        });


        groups_lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                alertRemoveGroupChat(parent.getItemAtPosition(position).toString());
                return true;
            }
        });
        return root;
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        String currentUser = mAuth.getCurrentUser().getUid() ;
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        groups_lv = (ListView) root.findViewById(R.id.groups_lv);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, groupsList);
        groups_lv.setAdapter(adapter);
    }

    private void retrieveAndDisplayGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext())
                    set.add(((DataSnapshot) iterator.next()).getKey());
                groupsList.clear();
                groupsList.addAll(set);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void alertRemoveGroupChat(final String groupName ) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setTitle("delete "+ groupName + " group ?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeGroupChat(groupName);
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

    private void removeGroupChat(final String groupName) {
        groupsRef.child(groupName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(getContext(), groupName + " group is successfully removed", Toast.LENGTH_SHORT).show();

                else
                    Toast.makeText(getContext(), task.getException().toString() , Toast.LENGTH_SHORT).show();
            }
        });
    }

}