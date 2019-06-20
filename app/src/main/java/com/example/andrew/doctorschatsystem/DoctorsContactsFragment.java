package com.example.andrew.doctorschatsystem;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


/**
 * A simple {@link Fragment} subclass.
 */

public class DoctorsContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView contactsList;

    private DatabaseReference contactsRef , usersRef;
    private FirebaseAuth mAuth ;

    private String currentUserID , receiverID , receiverName , receiverImg   ;

    public DoctorsContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView = inflater.inflate(R.layout.fragment_doctors_contacts, container, false);

        initializeFields();

        return contactsView;
    }

    private void initializeFields() {
        contactsList = (RecyclerView) contactsView.findViewById(R.id.Contacts_RecyclerView);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid() ;
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions recyclerOptions = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef , Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts , ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                final String usersIDs = getRef(position).getKey();

                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            if(dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                if(state.equals("online"))
                                    holder.onlineIcon.setVisibility(View.VISIBLE);

                                else if(state.equals("offline"))
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }
                            else
                                holder.onlineIcon.setVisibility(View.INVISIBLE);

                            String uPicture = "default_image" ;
                            if(dataSnapshot.hasChild("image"))
                            {
                                 uPicture = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(uPicture).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                                final String uName = dataSnapshot.child("uName").getValue().toString();
                                String uStatus = dataSnapshot.child("uStatus").getValue().toString();

                                holder.userName.setText(uName);
                                holder.userStatus.setText(uStatus);

                            final String finalUPicture = uPicture;

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext() , PrivateChatActivity.class);

                                    intent.putExtra("chatWith_image" , finalUPicture);
                                    intent.putExtra("chatWith_ID" , usersIDs);
                                    intent.putExtra("chatWith_name" , uName);
                                    startActivity(intent);
                                }
                            });

                            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    receiverName = uName ;
                                    receiverID = usersIDs ;
                                    receiverImg = finalUPicture ;
                                    return false;
                                }
                            });
                            registerForContextMenu(holder.itemView);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout , viewGroup , false);
                return new ContactsViewHolder(view);
            }
        };
        contactsList.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(R.menu.contacts_context_menu , menu);

        //super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //return super.onContextItemSelected(item);
        switch (item.getItemId())
        {
            case R.id.contactsItem_Chat:
                Intent intent = new Intent(getContext(), PrivateChatActivity.class);

                intent.putExtra("chatWith_image", receiverImg);
                intent.putExtra("chatWith_ID", receiverID);
                intent.putExtra("chatWith_name", receiverName);
                startActivity(intent);
                return true;

            case R.id.contactsItem_Visit:
                Intent intent1 = new Intent(getContext() , ProfileActivity.class);
                intent1.putExtra("visit_user_id", receiverID);
                startActivity(intent1);
                return true;

            case R.id.contactsItem_remove:
                confirmRemoveContact(receiverName , receiverID);
                return true;

            default:
                return false ;
        }
    }

    private void confirmRemoveContact(String contactName , final String receivedUserID) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext() , R.style.AlertDialog);
        builder.setTitle("   Remove "+contactName+" from your contacts ?   ");

        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeContact(currentUserID , receivedUserID);
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

    private void removeContact(final String senderUserID , final String receivedUserID ) {
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
                                                Toast.makeText(getContext(), "Contact has been removed", Toast.LENGTH_SHORT).show();
                                            } else
                                                Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else
                            Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                });
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName , userStatus ;
        CircleImageView profileImage ;
        ImageView onlineIcon ;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.tv_displayUsers_userName);
            userStatus = itemView.findViewById(R.id.tv_displayUsers_userStatus);
            profileImage = itemView.findViewById(R.id.displayUsers_profileImage);
            onlineIcon = itemView.findViewById(R.id.iv_user_online_status);
        }
    }

}
