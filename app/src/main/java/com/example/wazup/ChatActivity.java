package com.example.wazup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {



    private String messageReceiverId,messageReceiverName,messageReceiverImage,messagesenderID;

    private TextView userName;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mauth;
    private ImageButton sendMessageButton;
    private EditText MessageInputText;

    private DatabaseReference rootref;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mauth=FirebaseAuth.getInstance();
        messagesenderID=mauth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();

        messageReceiverId=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage=getIntent().getExtras().get("visit_image").toString();


        IntializeControllers();



        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile).into(userImage);


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
    }

    private void IntializeControllers() {

        ChatToolBar=(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView)findViewById(R.id.custom_profile_image);
        userName=(TextView)findViewById(R.id.custom_profile_name);

        sendMessageButton=(ImageButton)findViewById(R.id.send_message_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter=new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootref.child("Messages").child(messagesenderID).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages=dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage(){
        String messageText=MessageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Write your message", Toast.LENGTH_SHORT).show();
        }
        else {
            String messageSenderRef="Messages/"+messagesenderID+"/"+messageReceiverId;
            String messageRecieverRef="Messages/"+messageReceiverId+"/"+messagesenderID;

            DatabaseReference userMessageKeyRef=rootref.child("Messages")
                    .child(messagesenderID).child(messageReceiverId).push();

            String messagePushID=userMessageKeyRef.getKey();

            Map messageTextBody= new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messagesenderID);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);

            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

            rootref.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                 if(task.isSuccessful()){
                     Toast.makeText(ChatActivity.this, "Message Sent Succesfully", Toast.LENGTH_SHORT).show();
                 }
                 else{
                     Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                 }
                 MessageInputText.setText("");
                }
            });

        }

    }
}
