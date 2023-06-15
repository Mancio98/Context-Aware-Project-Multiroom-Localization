package com.example.multiroomlocalization;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.example.multiroomlocalization.messages.connection.MessageRegistration;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.google.gson.Gson;

import java.util.Base64;

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


        client = LoginActivity.client;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CLOSE&#95;ALL");
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                RegistrationActivity.this.finish();
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);


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
                String encoded = Base64.getEncoder().encodeToString(password.getText().toString().getBytes());

                User user = new User(username.getText().toString(),encoded);
                registration.setEnabled(false);
                ClientSocket.Callback<String> callbackSuccessful = new ClientSocket.Callback<String>() {
                    @Override
                    public void onComplete(String result) {
                        dialogBuilder = new AlertDialog.Builder(RegistrationActivity.this);
                        final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
                        dialogBuilder.setView(popup);

                        Button button = (Button) popup.findViewById(R.id.buttonPopup);
                        TextView text = (TextView) popup.findViewById(R.id.textPopup);

                        button.setText("CONFERMA");
                        text.setText(R.string.registrationDone);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                                finish();
                            }
                        });

                        dialog = dialogBuilder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                };
                ClientSocket.Callback<String> callbackUnsuccessful = new ClientSocket.Callback<String>() {

                    @Override
                    public void onComplete(String result) {
                        Gson gson = new Gson();
                        Toast.makeText(RegistrationActivity.this, "USERNAME GIÁ PRESENTE", Toast.LENGTH_LONG).show();
                        username.setText("");
                        password.setText("");
                        registration.setEnabled(true);
                    }
                };
                MessageRegistration message = new MessageRegistration(user);
                Gson gson = new Gson();
                String json = gson.toJson(message);
                client.sendMessageRegistration(callbackSuccessful,callbackUnsuccessful,json);

            }
        });

    }
}
