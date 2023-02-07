package com.example.multiroomlocalization;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity  extends AppCompatActivity {
    Button registration;
    boolean userEmpty;
    boolean passwordEmpty;
    EditText user;
    EditText password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);
        registration = findViewById(R.id.buttonRegistration);
        registration.setEnabled(false);
        userEmpty = true;
        passwordEmpty = true;
        user = findViewById(R.id.editMailRegistration);
        password = findViewById(R.id.editPasswordRegistration);

        user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    userEmpty=true;
                    registration.setEnabled(false);
                } else {
                    userEmpty=false;
                    if(!passwordEmpty){
                        registration.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    passwordEmpty=true;
                    registration.setEnabled(false);
                } else {
                    passwordEmpty=false;
                    if(!userEmpty){
                        registration.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
}
