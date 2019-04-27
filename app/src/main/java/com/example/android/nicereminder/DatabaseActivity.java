package com.example.android.nicereminder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DatabaseActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView email;
    private TextView password;
    private Button signin;
    private Button signup;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        mAuth = FirebaseAuth.getInstance();

        email = (TextView)findViewById(R.id.email);
        password = (TextView)findViewById(R.id.password);

        signin = (Button) findViewById(R.id.signin);
        signup = (Button) findViewById(R.id.signup);


    }


}
