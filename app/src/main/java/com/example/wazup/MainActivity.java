package com.example.wazup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar main_Toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UserRef;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FindFriendsRecyclerList=(RecyclerView)findViewById(R.id.find_friends_rcyler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mauth=FirebaseAuth.getInstance();
        firebaseUser=mauth.getCurrentUser();
        rootref= FirebaseDatabase.getInstance().getReference();

        main_Toolbar=(Toolbar)findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(main_Toolbar);
        getSupportActionBar().setTitle("Wazup");

        //viewPager=(ViewPager)findViewById(R.id.main_tabs_page);

        //adapter=new Adapter(getSupportFragmentManager());
        //viewPager.setAdapter(adapter);

        //tabLayout=(TabLayout)findViewById(R.id.main_tabs);
        //tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseUser==null){
            TOLoginActivity();
        }
        else {
            VerifyUserExistance();

            FirebaseRecyclerOptions<Contacts> options=
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(UserRef,Contacts.class)
                            .build();


            FirebaseRecyclerAdapter<Contacts,MainActivity.FindFriendViewHolder> adapter=
                    new FirebaseRecyclerAdapter<Contacts, MainActivity.FindFriendViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull MainActivity.FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                            holder.username.setText(model.getName());
                            Picasso.get().load(model.getImage()).into(holder.profileimage);
                            final String[] retImage = {"default_image"};

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final String visit_user_id = getRef(position).getKey();

                                    UserRef.child(visit_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final String retName = dataSnapshot.child("name").getValue().toString();
                                            retImage[0] =dataSnapshot.child("image").getValue().toString();

                                            Intent chatIntent = new Intent(MainActivity.this,ChatActivity.class);
                                            chatIntent.putExtra("visit_user_name",retName);
                                            chatIntent.putExtra("visit_user_id",visit_user_id);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });



                                }
                            });
                        }

                        @NonNull
                        @Override
                        public MainActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display,viewGroup,false);
                            MainActivity.FindFriendViewHolder viewHolder = new MainActivity.FindFriendViewHolder(view);
                            return viewHolder;
                        }
                    };
            FindFriendsRecyclerList.setAdapter(adapter);
            adapter.startListening();
        }
    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        TextView username;
        CircleImageView profileimage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            profileimage=itemView.findViewById(R.id.users_profile_image);
        }
    }

    private void VerifyUserExistance() {
        String currentUserID=mauth.getCurrentUser().getUid();
        rootref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                   // Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();

                }
                else {
                    ToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void TOLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.logout){
            mauth.signOut();
            TOLoginActivity();
        }
        if (item.getItemId()==R.id.setting_options){
            ToSettingActivity();
        }

        return true;
    }

    private void ToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this,Setting.class);
        startActivity(settingIntent);
    }




}
