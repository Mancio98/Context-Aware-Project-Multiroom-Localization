package com.example.multiroomlocalization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multiroomlocalization.socket.ClientSocket;

public class LoginActivity extends AppCompatActivity {

    EditText userInput;
    EditText passwordInput;
    Button buttonLogin;
    boolean userEmpty;
    boolean passwordEmpty;
    boolean bool;
    ClientSocket client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userInput = findViewById(R.id.editMailRegistration);
        passwordInput = findViewById(R.id.editPasswordRegistration);
        buttonLogin = findViewById(R.id.buttonRegistration);
        TextView textRegistration = findViewById(R.id.textRegistration);
        userEmpty = true;
        passwordEmpty = true;
        buttonLogin.setEnabled(false);
        bool = true;
        SpannableString content = new SpannableString( "Create an account" ) ;
        content.setSpan( new UnderlineSpan() , 0 , content.length() , 0 ) ;
        textRegistration.setText(content) ;

        client = new ClientSocket();
        client.setContext(getApplicationContext());
        client.start();


        textRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changeActivity = new Intent(getApplicationContext(),RegistrationActivity.class);
                startActivity(changeActivity);
            }
        });

        //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    userEmpty =true;
                    buttonLogin.setEnabled(false);
                } else {
                    userEmpty =false;
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
                    if(!userEmpty){
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
                String email = userInput.getText().toString();
                String password = passwordInput.getText().toString();

                User user= new User(email,password);
                client.createMessageLogin(user).execute();
                /*if(email.equals("admin") && password.equals("admin")){
                    loginSuccesfull();
                }
                else {
                    Toast.makeText(LoginActivity.this, "ERROR LOGIN", Toast.LENGTH_LONG).show();
                }
                */


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