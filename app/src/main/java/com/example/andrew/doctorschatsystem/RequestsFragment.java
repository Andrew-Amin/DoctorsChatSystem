package com.example.andrew.doctorschatsystem;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class RequestsFragment extends Fragment {

    private View requestView;
    private RecyclerView requests_recyclerView;

    private DatabaseReference chatRequestsRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestView = inflater.inflate(R.layout.fragment_requests, container, false);

        initializeFields();

        return requestView;
    }

    private void initializeFields() {
        requests_recyclerView = (RecyclerView) requestView.findViewById(R.id.Requests_RecyclerView);
        requests_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference();
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat_requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestsRef.child(currentUserID), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter = new
                FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.btn_Decline.setVisibility(View.VISIBLE);
                        holder.btn_Accept.setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference request_type_ref = getRef(position).child("request_type").getRef();

                        request_type_ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String request_type = dataSnapshot.getValue().toString();

                                    if (request_type.equals("received")) {
                                        usersRef.child("Users").child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    if (dataSnapshot.hasChild("image")) {

                                                        final String request_userImage = dataSnapshot.child("image").getValue().toString();

                                                        Picasso.get().load(request_userImage).placeholder(R.drawable.profile_image)
                                                                .into(holder.profileImage);
                                                    }
                                                    final String request_userName = dataSnapshot.child("uName").getValue().toString();

                                                    holder.userName.setText(request_userName);
                                                    holder.userStatus.setText("wants to contact with you ...");

                                                    holder.btn_Accept.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            acceptChatRequest(list_user_id);
                                                        }
                                                    });

                                                    holder.btn_Decline.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            cancelChatRequest(list_user_id);
                                                        }
                                                    });
                                                } else {
                                                    usersRef.child("Doctors").child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild("image")) {

                                                                final String request_userImage = dataSnapshot.child("image").getValue().toString();

                                                                Picasso.get().load(request_userImage).placeholder(R.drawable.profile_image)
                                                                        .into(holder.profileImage);
                                                            }
                                                            final String request_userName = dataSnapshot.child("uName").getValue().toString();

                                                            holder.userName.setText(request_userName);
                                                            holder.userStatus.setText("wants to contact with you ...");

                                                            holder.btn_Accept.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    acceptChatRequest(list_user_id);
                                                                }
                                                            });

                                                            holder.btn_Decline.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    cancelChatRequest(list_user_id);
                                                                }
                                                            });
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
                                    } else if (request_type.equals("sent")) {
                                        holder.btn_Accept.setText("Cancel");
                                        holder.btn_Accept.setBackgroundResource(R.drawable.decline_btn_bg);

                                        holder.btn_Decline.setVisibility(View.INVISIBLE);
                                        holder.btn_Decline.setEnabled(false);


                                        usersRef.child("Users").child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists()) {
                                                    if (dataSnapshot.hasChild("image")) {

                                                        final String request_userImage = dataSnapshot.child("image").getValue().toString();

                                                        Picasso.get().load(request_userImage).placeholder(R.drawable.profile_image)
                                                                .into(holder.profileImage);
                                                    }
                                                    final String request_userName = dataSnapshot.child("uName").getValue().toString();

                                                    holder.userName.setText(request_userName);
                                                    holder.userStatus.setText("You have sent request to " + request_userName);

                                                    holder.btn_Accept.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            cancelChatRequest(list_user_id);
                                                        }
                                                    });
                                                } else {
                                                    usersRef.child("Doctors").child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild("image")) {

                                                                final String request_userImage = dataSnapshot.child("image").getValue().toString();

                                                                Picasso.get().load(request_userImage).placeholder(R.drawable.profile_image)
                                                                        .into(holder.profileImage);
                                                            }
                                                            final String request_userName = dataSnapshot.child("uName").getValue().toString();

                                                            holder.userName.setText(request_userName);
                                                            holder.userStatus.setText("You have sent request to " + request_userName);

                                                            holder.btn_Accept.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    cancelChatRequest(list_user_id);
                                                                }
                                                            });
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

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                        return viewHolder;
                    }
                };
        requests_recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button btn_Accept, btn_Decline;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.tv_displayUsers_userName);
            userStatus = itemView.findViewById(R.id.tv_displayUsers_userStatus);
            profileImage = itemView.findViewById(R.id.displayUsers_profileImage);
            btn_Accept = itemView.findViewById(R.id.btn_AcceptRequest);
            btn_Decline = itemView.findViewById(R.id.btn_DeclineRequest);
        }
    }


    private void cancelChatRequest(final String list_user_id) {
        chatRequestsRef.child(currentUserID).child(list_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestsRef.child(list_user_id).child(currentUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Request has been deleted", Toast.LENGTH_SHORT).show();
                                            } else
                                                Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else
                            Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void acceptChatRequest(final String list_user_id) {
        contactsRef.child(currentUserID).child(list_user_id)
                .child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsRef.child(list_user_id).child(currentUserID)
                            .child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chatRequestsRef.child(currentUserID).child(list_user_id)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chatRequestsRef.child(list_user_id).child(currentUserID)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        Toast.makeText(getContext(), "New contact saved", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}
