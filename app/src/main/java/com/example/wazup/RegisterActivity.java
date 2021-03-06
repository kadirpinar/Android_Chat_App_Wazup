package com.example.wazup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private Button createbtn;
    private EditText email,password;
    private TextView ahaccount;
    private  FirebaseAuth mauth;
    private ProgressDialog loadingBar;
    private DatabaseReference rootref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InitializeFields();

        mauth= FirebaseAuth.getInstance();
        rootref= FirebaseDatabase.getInstance().getReference();
        ahaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToLoginActivity();
            }
        });

        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String inputemail=email.getText().toString();
        String inputpassword=password.getText().toString();

        if(TextUtils.isEmpty(inputemail)){
            Toast.makeText(this,"Please Enter email...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(inputpassword)){
            Toast.makeText(this,"Please Enter password...",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mauth.createUserWithEmailAndPassword(inputemail,inputpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currentUserID=mauth.getCurrentUser().getUid();
                                rootref.child("Users").child(currentUserID).setValue("");
                                ToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account Created Succesfully",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message=task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error:"+ message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        createbtn=(Button)findViewById(R.id.register_button);
        email=(EditText)findViewById(R.id.register_email);
        password=(EditText)findViewById(R.id.register_password);
        ahaccount=(TextView)findViewById(R.id.have_account);
        loadingBar=new ProgressDialog(this);
    }


    private void ToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void ToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
