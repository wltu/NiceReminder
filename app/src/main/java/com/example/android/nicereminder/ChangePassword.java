package com.example.android.nicereminder;

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

public class ChangePassword extends AppCompatActivity {

    private EditText newPass;
    private EditText confirmPass;
    private String password;
    private String confirm;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPass = (EditText) findViewById(R.id.new_password);
        confirmPass = (EditText) findViewById(R.id.confirm_new_password);
        confirmButton = (Button) findViewById(R.id.password_confirm);

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

                    // Check for a valid email address.
                    if (TextUtils.isEmpty(confirm)) {
                        confirmPass.setError(getString(R.string.error_field_required));
                    } else if (!isPasswordValid(confirm)) {
                        confirmPass.setError(getString(R.string.error_invalid_password));
                    }else if(confirm.compareTo(password) != 0) {
                        confirmPass.setError("Password not equal!");
                    }else{
                        MainScreen.changePassword(confirm);
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

                if(!password.isEmpty()){
                    if(confirm.compareTo(password) != 0){
                        confirmPass.setError("Password not equal!");
                    }else {
                        MainScreen.changePassword(confirm);
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
}
