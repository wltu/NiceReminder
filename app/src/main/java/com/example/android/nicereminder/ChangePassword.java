package com.example.android.nicereminder;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private EditText newPass;
    private EditText oldPass;
    private EditText confirmPass;
    private String password;
    private String oldpassword;
    private String confirm;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPass = (EditText) findViewById(R.id.new_password);
        oldPass = (EditText) findViewById(R.id.old_password);
        confirmPass = (EditText) findViewById(R.id.confirm_new_password);
        confirmButton = (Button) findViewById(R.id.password_confirm);

        oldPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT || id == EditorInfo.IME_NULL) {

                    oldpassword = oldPass.getText().toString();

                    // Check for a valid email address.
                    if (TextUtils.isEmpty(oldpassword)) {
                        oldPass.setError(getString(R.string.error_field_required));
                    } else if (!isPasswordValid(oldpassword)) {
                        oldPass.setError(getString(R.string.error_invalid_password));
                    }else{
                        newPass.requestFocus();
                    }

                    return true;
                }
                return false;
            }
        });

        newPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT || id == EditorInfo.IME_NULL) {

                    password = newPass.getText().toString();

                    // Check for a valid email address.
                    if (TextUtils.isEmpty(password)) {
                        newPass.setError(getString(R.string.error_field_required));
                    } else if (!isPasswordValid(password)) {
                        newPass.setError(getString(R.string.error_invalid_password));
                    }else{
                        confirmPass.requestFocus();
                    }

                    return true;
                }
                return false;
            }
        });

        confirmPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    confirm = confirmPass.getText().toString();
                    password = newPass.getText().toString();

                    // Check for a valid email address.
                    if (TextUtils.isEmpty(confirm)) {
                        confirmPass.setError(getString(R.string.error_field_required));
                    } else if (!isPasswordValid(confirm)) {
                        confirmPass.setError(getString(R.string.error_invalid_password));
                    }else if(confirm.compareTo(password) != 0) {
                        confirmPass.setError("Password not equal!");
                    }else{
                        oldpassword = oldPass.getText().toString();
                        changePassword(confirm, oldpassword);
                        finish();
                    }

                    return true;
                }
                return false;
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm = confirmPass.getText().toString();
                password = newPass.getText().toString();
                oldpassword = oldPass.getText().toString();

                if(!password.isEmpty()){
                    if(confirm.compareTo(password) != 0){
                        confirmPass.setError("Password not equal!");
                    }else {
                        changePassword(confirm, oldpassword);
                        finish();
                    }
                }
            }
        });
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    // Update password through Firebase
    private void changePassword(final String password, String oldpass){
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user != null){
            user = FirebaseAuth.getInstance().getCurrentUser();
            final String email = user.getEmail();

            AuthCredential credential = EmailAuthProvider.getCredential(email, oldpass);

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(getApplication(), "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getApplication(), "Password Changed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else {
                        Toast.makeText(getApplication(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
