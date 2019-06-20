package com.example.andrew.doctorschatsystem;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatsList ;

    private DatabaseReference chatsRef , rootRef ;
    private FirebaseAuth mAuth ;

    private String currentUserID ;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        initializeFields();

        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions <Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef , Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts ,ChatsViewHolder > adapter = new
                FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String list_userID = getRef(position).getKey();
                final String[] uImage = {"default_image"};
                rootRef.child("Users").child(list_userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            if(dataSnapshot.hasChild("image"))
                            {
                                 uImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(uImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            final String uName = dataSnapshot.child("uName").getValue().toString();

                            holder.userName.setText(uName);


                            if(dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                if(state.equals("online"))
                                    holder.userStatus.setText(" online ..");
                                else if(state.equals("offline"))
                                {
                                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                    String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                    holder.userStatus.setText("Last seen:\n    "+date+"  at  "+time);
                                    holder.userStatus.setTextSize(14);
                                }

                            }
                            else
                                holder.userStatus.setText("update the app");


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext() , PrivateChatActivity.class);

                                    intent.putExtra("chatWith_image" , uImage[0]);
                                    intent.putExtra("chatWith_ID" , list_userID);
                                    intent.putExtra("chatWith_name" , uName);
                                    startActivity(intent);
                                }
                            });

                        }
                        else
                        {
                            rootRef.child("Doctors").child(list_userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.hasChild("image"))
                                        {
                                            uImage[0] = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(uImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }
                                        final String uName = dataSnapshot.child("uName").getValue().toString();

                                        holder.userName.setText(uName);


                                        if(dataSnapshot.child("userState").hasChild("state"))
                                        {
                                            String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                            if(state.equals("online"))
                                                holder.userStatus.setText(" online ..");
                                            else if(state.equals("offline"))
                                            {
                                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                                holder.userStatus.setText("Last seen:\n    "+date+"  at  "+time);
                                                holder.userStatus.setTextSize(14);
                                            }

                                        }
                                        else
                                            holder.userStatus.setText("update the app");


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(getContext() , PrivateChatActivity.class);

                                                intent.putExtra("chatWith_image" , uImage[0]);
                                                intent.putExtra("chatWith_ID" , list_userID);
                                                intent.putExtra("chatWith_name" , uName);
                                                startActivity(intent);
                                            }
                                        });
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

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout , viewGroup , false);
                return new ChatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void initializeFields() {
        chatsList = (RecyclerView)privateChatView.findViewById(R.id.chatFragment_RecyclerView);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName , userStatus ;
        CircleImageView profileImage ;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.tv_displayUsers_userName);
            userStatus = itemView.findViewById(R.id.tv_displayUsers_userStatus);
            profileImage = itemView.findViewById(R.id.displayUsers_profileImage);
        }
    }
}
