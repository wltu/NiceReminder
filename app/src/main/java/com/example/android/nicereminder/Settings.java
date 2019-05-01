package com.example.android.nicereminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class Settings extends Fragment {
    private TextView changeName;
    private TextView signout;
    private TextView changePassword;
    private TextView deleteAccount;
    private TextView changeProfilePicture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting, container, false);

        changeName = (TextView) view.findViewById(R.id.setting_change_name);
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(container.getContext());

                alert.setTitle("Enter new name:");

                final EditText input = new EditText(container.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                alert.setView(input);

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = input.getText().toString();

                        MainScreen.updateName(newName);
                    }
                });

                alert.show();
            }
        });

        signout = (TextView) view.findViewById(R.id.setting_signout);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainScreen.SignOut();
            }
        });

        changePassword = (TextView) view.findViewById(R.id.setting_change_password);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(container.getContext(), ChangePassword.class));
            }
        });

        deleteAccount = (TextView) view.findViewById(R.id.setting_delete_account);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(container.getContext());

                alert.setTitle("Delete Account?");

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MainScreen.deleteAccount();
                    }
                });

                alert.show();

            }
        });

        changeProfilePicture = (TextView) view.findViewById(R.id.setting_change_profile_picture);
        return view;
    }
}
