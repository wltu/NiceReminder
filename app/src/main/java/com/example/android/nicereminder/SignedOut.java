package com.example.android.nicereminder;


import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;



public class SignedOut extends PreferenceFragment {


    private Preference signin;
    private Preference signup;
    private Intent intent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.activity_signedout);

        signin = findPreference("signin");
        signup = findPreference("signup");

        signin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                intent = new Intent(getActivity(), LoginActivity.class);
                intent.putExtra("Signup", false);
                startActivity(intent);

                return false;
            }
        });

        signup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                intent = new Intent(getActivity(), LoginActivity.class);
                intent.putExtra("Signup", true);
                startActivity(intent);
                return false;
            }
        });
    }
}
