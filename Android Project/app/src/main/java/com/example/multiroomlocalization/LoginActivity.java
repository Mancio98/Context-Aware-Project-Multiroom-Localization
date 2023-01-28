package com.example.multiroomlocalization;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput;
    EditText passwordInput;
    Button buttonLogin;
    boolean emailEmpty;
    boolean passwordEmpty;
    boolean bool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.editMail);
        passwordInput = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        emailEmpty = true;
        passwordEmpty = true;
        buttonLogin.setEnabled(false);
        bool = true;
        //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    emailEmpty=true;
                    buttonLogin.setEnabled(false);
                } else {
                    emailEmpty=false;
                    if(!passwordEmpty){
                        buttonLogin.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    passwordEmpty=true;
                    buttonLogin.setEnabled(false);
                } else {
                    passwordEmpty=false;
                    if(!emailEmpty){
                        buttonLogin.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                if(email.equals("admin") && password.equals("admin")){
                    loginSuccesfull();
                }
                else {
                    Toast.makeText(LoginActivity.this, "ERROR LOGIN", Toast.LENGTH_LONG).show();
                }

                /*System.out.println(mNotificationManager.isNotificationPolicyAccessGranted());
                // Check if the notification policy access has been granted for the app.
                if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                }
                else {
                    if(bool) {
                        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                        bool = false;
                    }else {
                        bool = true;
                        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                    }
                }*/

            }
        });


    }


    private void loginSuccesfull(){
        Intent changeActivity = new Intent(this,MainActivity.class);
        startActivity(changeActivity);
    }

}