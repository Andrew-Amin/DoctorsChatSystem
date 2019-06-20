package com.example.andrew.doctorschatsystem;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessagesViewHolder> {
    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText,
        senderMessageTextDate, receiverMessageTextDate ;

        public CircleImageView receiverImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.customMsg_senderText);
            receiverMessageText = itemView.findViewById(R.id.customMsg_receiverText);
            receiverImage = itemView.findViewById(R.id.customMsg_receiverImage);

        }
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mAuth = FirebaseAuth.getInstance();

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages, viewGroup, false);
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder messagesViewHolder, int i) {
        String msgSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        final String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").child(fromUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    String imageURL = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(imageURL).placeholder(R.drawable.profile_image).into(messagesViewHolder.receiverImage);
                }
                else
                {
                    rootRef.child("Doctors").child(fromUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                                String imageURL = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(imageURL).placeholder(R.drawable.profile_image).into(messagesViewHolder.receiverImage);
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

        if(fromMessageType.equals("text"))
        {
            messagesViewHolder.receiverImage.setVisibility(View.INVISIBLE);
            messagesViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
            messagesViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

            if(fromUserID.equals(msgSenderID))
            {
                messagesViewHolder.senderMessageText.setVisibility(View.VISIBLE);
               messagesViewHolder.senderMessageText.setText(messages.getMessage());

            }

            else
            {
                messagesViewHolder.receiverImage.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessageText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public void clearAdapter () { userMessagesList.clear(); }

}
