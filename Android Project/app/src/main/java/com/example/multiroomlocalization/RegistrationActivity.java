package com.example.multiroomlocalization;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.multiroomlocalization.socket.ClientSocket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RegistrationActivity  extends AppCompatActivity {
    Button registration;
    boolean userEmpty;
    boolean passwordEmpty;
    EditText username;
    EditText password;
    ClientSocket client;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

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
        client.setContext(RegistrationActivity.this);
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
                registration.setEnabled(false);
                client.createMessageRegistration(user).executeAsync((response) -> {
                    System.out.println("fatto");
                    System.out.println(response);
                    Gson gson = new Gson();
                    String messageType = gson.fromJson(response, JsonObject.class).get("type").getAsString();
                    if(messageType.equals("REGISTRATION SUCCESSFUL")){

                        dialogBuilder = new AlertDialog.Builder(RegistrationActivity.this);
                        final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
                        dialogBuilder.setView(popup);
                        dialog = dialogBuilder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();

                        Button button = (Button) popup.findViewById(R.id.buttonPopup);
                        TextView text = (TextView) popup.findViewById(R.id.textPopup);

                        button.setText("CONFERMA");
                        text.setText(R.string.registrationDone);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });

                    }
                    else {
                        String messageDescription = gson.fromJson(response, JsonObject.class).get("description").getAsString();
                        Toast.makeText(RegistrationActivity.this, messageDescription, Toast.LENGTH_LONG).show();
                        registration.setEnabled(true);
                    }

                } );

            }
        });

    }
}
