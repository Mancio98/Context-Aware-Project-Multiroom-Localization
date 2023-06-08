package com.example.multiroomlocalization;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multiroomlocalization.messages.connection.MessageLogin;
import com.example.multiroomlocalization.messages.connection.MessageSuccessfulLogin;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends AppCompatActivity {

    EditText userInput;
    EditText passwordInput;
    Button buttonLogin;
    boolean userEmpty;
    boolean passwordEmpty;
    boolean bool;
    protected static ClientSocket client;
    protected static User currentUser;
    public static Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView link = findViewById(R.id.textViewLink);
        link.setMovementMethod(LinkMovementMethod.getInstance());

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
        handler = new Handler(Looper.getMainLooper());

        //TODO DA SCOMMENTARE
        //client = new ClientSocket(LoginActivity.this);
        //client.setContext(LoginActivity.this);
        //client.start();


        textRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changeActivity = new Intent(getApplicationContext(),RegistrationActivity.class);
                startActivity(changeActivity);
            }
        });

        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

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
            public void afterTextChanged(Editable editable) {}
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

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
            public void afterTextChanged(Editable editable) {}
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = userInput.getText().toString();
                String password = passwordInput.getText().toString();

                User user= new User(email,password);



                loginSuccessfull(new ArrayList<Map>());
                /*ClientSocket.Callback<String> callbackSuccessful = new ClientSocket.Callback<String>() {
                    @Override
                    public void onComplete(String result) {
                        currentUser = user;
                        System.out.println(result);
                        Gson gson = new Gson();
                        ArrayList<Map> accountMap =  gson.fromJson(result, MessageSuccessfulLogin.class).getMapList();
                        loginSuccessfull(accountMap);
                    }
                };
                ClientSocket.Callback<String> callbackUnsuccessful = new ClientSocket.Callback<String>() {
                    @Override
                    public void onComplete(String result) {
                        Toast.makeText(LoginActivity.this, "ERROR: CREDENZIALI NON CORRETTE", Toast.LENGTH_LONG).show();
                    }
                };

                Gson gson = new Gson();
                MessageLogin message = new MessageLogin(user);
                String json = gson.toJson(message);
                client.sendMessageLogin(callbackSuccessful,callbackUnsuccessful,json);
                */


                //TODO CODICE PER ATTIVARE DONOTDISTURB
                /*
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                System.out.println(mNotificationManager.isNotificationPolicyAccessGranted());
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


    private void loginSuccessfull(ArrayList<Map> accountMap){
        //Intent changeActivity;
        //changeActivity = new Intent(this,ListMapActivity.class);s
        //changeActivity.putExtra("Map",accountMap);

        Intent changeActivity = new Intent(this,ListMapActivity.class);

        String address = userInput.getText().toString();
        Integer port = Integer.parseInt(passwordInput.getText().toString());
        client = new ClientSocket(LoginActivity.this);
        client.setAddress(address,port);
        client.setContext(LoginActivity.this);
        client.start();

        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        User user = new User("luca","12345");
                        ClientSocket.Callback<String> callbackSuccessful = new ClientSocket.Callback<String>() {
                            @Override
                            public void onComplete(String result) {
                                currentUser = user;
                                System.out.println(result);
                                Gson gson = new Gson();
                                ArrayList<Map> accountMap =  gson.fromJson(result, MessageSuccessfulLogin.class).getMapList();
                                //loginSuccessfull(accountMap);
                                changeActivity.putExtra("Map",accountMap);
                                startActivity(changeActivity);

                            }
                        };
                        ClientSocket.Callback<String> callbackUnsuccessful = new ClientSocket.Callback<String>() {
                            @Override
                            public void onComplete(String result) {
                                Toast.makeText(LoginActivity.this, "ERROR: CREDENZIALI NON CORRETTE", Toast.LENGTH_LONG).show();
                            }
                        };

                        Gson gson = new Gson();
                        MessageLogin message = new MessageLogin(user);
                        String json = gson.toJson(message);
                        client.sendMessageLogin(callbackSuccessful,callbackUnsuccessful,json);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


        //startActivity(changeActivity);
    }

}