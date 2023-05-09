package com.example.multiroomlocalization;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.multiroomlocalization.socket.ClientSocket;

public class RegistrationActivity  extends AppCompatActivity {
    Button registration;
    boolean userEmpty;
    boolean passwordEmpty;
    EditText username;
    EditText password;
    ClientSocket client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);
        registration = findViewById(R.id.buttonRegistration);
        registration.setEnabled(false);
        userEmpty = true;
        passwordEmpty = true;
        username = findViewById(R.id.editMailRegistration);
        password = findViewById(R.id.editPasswordRegistration);

        client = new ClientSocket();
        client.setContext(getApplicationContext());
        client.start();

        username.addTextChangedListener(new TextWatcher() {
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

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User(username.getText().toString(),password.getText().toString());

                client.createMessageRegistration(user).executeAsync((response) -> {
                    if (response == "REGISTRATIONDONE"){
                        // REGISTRAZIONE ANDATA A BUON FINE
                    }
                    else {
                        // ESEGUIRE CASI ERRORE REGISTRAZIONE
                        // 1) UTENTE GIà REGISTRATO
                        // 2) NOME GIà IN USO
                        // 3) ALTRO
                    }

                } );

            }
        });

    }
}
