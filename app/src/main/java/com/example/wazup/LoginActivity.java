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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser;
    private Button lgnbtn;
    private EditText email,password;
    private TextView newaccount,forgetpassword;
    private FirebaseAuth mauth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mauth=FirebaseAuth.getInstance();
        firebaseUser=mauth.getCurrentUser();
        InitializeFields();

        newaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToRegisterActivity();
            }
        });
        
        lgnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

    }

    private void AllowUserToLogin() {
        String inputemail=email.getText().toString();
        String inputpassword=password.getText().toString();

        if(TextUtils.isEmpty(inputemail)){
            Toast.makeText(this,"Please Enter email...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(inputpassword)){
            Toast.makeText(this,"Please Enter password...",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mauth.signInWithEmailAndPassword(inputemail,inputpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                ToMainActivity();
                                Toast.makeText(LoginActivity.this,"Logged in Succesfully",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else {
                                String message=task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error:"+message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        lgnbtn=(Button)findViewById(R.id.login_button);
        email=(EditText)findViewById(R.id.login_email);
        password=(EditText)findViewById(R.id.login_password);
        newaccount=(TextView)findViewById(R.id.new_account);
        forgetpassword=(TextView)findViewById(R.id.forget_password);
        loadingBar=new ProgressDialog(this);
    }

// onstart Silindi

    private void ToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void ToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }
}
