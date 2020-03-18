package com.example.wazup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Setting extends AppCompatActivity {

    private Button Updatesettings;
    private EditText username;
    private CircleImageView userProfileimage;
    private String currentUserId;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;
    private static final int GalleryPick=1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        mToolbar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");


        Updatesettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Updatesettings();
            }
        });

        RetriveUserId();


        userProfileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick&&resultCode==RESULT_OK&&data!=null){
            Uri ImageUri=data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Profile image uploading please wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                 final Uri resultUri = result.getUri();
                 StorageReference filePath=UserProfileImagesRef.child(currentUserId+"jpg");
                 filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Setting.this, "Profile Image Uploaded", Toast.LENGTH_SHORT).show();

                            final String downloaedUrl=task.getResult().getDownloadUrl().toString();

                            rootref.child("Users").child(currentUserId).child("image")
                                    .setValue(downloaedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(Setting.this, "Image saved", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else{
                                                String messeage=task.getException().toString();
                                                Toast.makeText(Setting.this, "Error:"+messeage, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                        else {
                            String message = task.getException().toString();
                            Toast.makeText(Setting.this, "Error:"+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void Updatesettings() {
        String setUsername=username.getText().toString();
        if(TextUtils.isEmpty(setUsername)){
            Toast.makeText(this,"Please write your user name",Toast.LENGTH_SHORT).show();

        }
        else{
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUsername);
            rootref.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                ToMainActivity();
                                Toast.makeText(Setting.this,"Profile updated Succesfully",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message=task.getException().toString();
                                Toast.makeText(Setting.this,"Error"+message,Toast.LENGTH_SHORT).show();                            }
                        }
                    });
        }
    }

    private void RetriveUserId() {
        rootref.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()&&(dataSnapshot.hasChild("name")&&dataSnapshot.hasChild("image"))){
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveprofileimage=dataSnapshot.child("image").getValue().toString();
                            username.setText(retrieveUserName);
                            Picasso.get().load(retrieveprofileimage).into(userProfileimage);

                        }
                        else if(dataSnapshot.exists()&&(dataSnapshot.hasChild("name"))){
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            username.setText(retrieveUserName);

                        }
                        else if(dataSnapshot.exists()&&(dataSnapshot.hasChild("image"))){
                            String retrieveprofileimage=dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(retrieveprofileimage).into(userProfileimage);

                        }                        else{
                            Toast.makeText(Setting.this, "Please set up your profile ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void InitializeFields() {
        Updatesettings=(Button)findViewById(R.id.update);
        username=(EditText)findViewById(R.id.get_username);
        userProfileimage=(CircleImageView)findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);
    }

    private void ToMainActivity() {
        Intent mainIntent = new Intent(Setting.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
