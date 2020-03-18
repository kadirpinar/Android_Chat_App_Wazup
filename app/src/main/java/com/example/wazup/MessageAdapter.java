package com.example.wazup;

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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersref;

    public MessageAdapter(List<Messages>userMessagesList){
        this.userMessagesList=userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText,recieverMessageText;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText=(TextView)itemView.findViewById(R.id.sender_message_text);
            recieverMessageText=(TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout,viewGroup,false);

       mAuth=FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messagesenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(i);

        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();

        usersref= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image")){
                    String reciverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(reciverImage).placeholder(R.drawable.profile).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if(fromMessageType.equals("text")){
            messageViewHolder.recieverMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);


            if(fromUserId.equals(messagesenderId)){
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.recieverMessageText.setVisibility(View.VISIBLE);


                messageViewHolder.recieverMessageText.setBackgroundResource(R.drawable.reciver_messages);
                messageViewHolder.recieverMessageText.setText(messages.getMessage());
            }

        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}
