package com.example.android.nicereminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.EditText;
import android.widget.LinearLayout;

public class Settings extends PreferenceFragment {
    private Preference changeName;
    private Preference signout;
    private Preference changePassword;
    private Preference  deleteAccount;
    private Preference  changeProfilePicture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.activity_setting);

        changeName = findPreference("name");
        signout = findPreference("signout");
        changePassword = findPreference("password");
        deleteAccount = findPreference("delete");
        changeProfilePicture = findPreference("profile_picture");


        changeName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle("Change name:");

                final EditText input = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                input.setText(MainScreen.getName());

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

                return false;
            }
        });


        signout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Sign Out?");

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MainScreen.SignOut();
                    }
                });

                alert.show();

                return false;
            }
        });

        deleteAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
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

                return false;
            }
        });

        changePassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ChangePassword.class));

                return false;
            }
        });
    }

    //
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.activity_setting, container, false);

//        changeName = (TextView) view.findViewById(R.id.setting_change_name);
//        changeName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder alert = new AlertDialog.Builder(container.getContext());
//
//                alert.setTitle("Enter new name:");
//
//                final EditText input = new EditText(container.getContext());
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT);
//                input.setLayoutParams(lp);
//
//                alert.setView(input);
//
//                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    }
//                });
//
//                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        String newName = input.getText().toString();
//
//                        MainScreen.updateName(newName);
//                    }
//                });
//
//                alert.show();
//            }
//        });
//
//        signout = (TextView) view.findViewById(R.id.setting_signout);
//        signout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MainScreen.SignOut();
//            }
//        });
//
//        changePassword = (TextView) view.findViewById(R.id.setting_change_password);
//        changePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(container.getContext(), ChangePassword.class));
//            }
//        });
//
//        deleteAccount = (TextView) view.findViewById(R.id.setting_delete_account);
//        deleteAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                AlertDialog.Builder alert = new AlertDialog.Builder(container.getContext());
//
//                alert.setTitle("Delete Account?");
//
//                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    }
//                });
//
//                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        MainScreen.deleteAccount();
//                    }
//                });
//
//                alert.show();
//
//            }
//        });
//
//        changeProfilePicture = (TextView) view.findViewById(R.id.setting_change_profile_picture);
//        return view;
//    }
}
