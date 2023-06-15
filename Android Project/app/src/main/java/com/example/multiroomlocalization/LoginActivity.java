package com.example.multiroomlocalization;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;
import com.example.multiroomlocalization.messages.connection.MessageLogin;
import com.example.multiroomlocalization.messages.connection.MessageSuccessfulLogin;
import com.example.multiroomlocalization.socket.ClientSocket;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Base64;

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
    public static BluetoothUtility btUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ScanService scanService = new ScanService(getApplicationContext());

        setContentView(R.layout.activity_login);
        TextView link = findViewById(R.id.textViewLink);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!scanService.getWifiManager().isScanThrottleEnabled()) {
                link.setVisibility(View.INVISIBLE);
            }
        }

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

        client = new ClientSocket(LoginActivity.this);
        client.setContext(LoginActivity.this);

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

                String encoded = Base64.getEncoder().encodeToString(password.getBytes());
                System.out.println(encoded);

                User user= new User(email,encoded);


                //loginSuccessfull(new ArrayList<Map>());
                ClientSocket.Callback<String> callbackSuccessful = new ClientSocket.Callback<String>() {
                    @Override
                    public void onComplete(String result) {
                        currentUser = user;
                        System.out.println(result);
                        Gson gson = new Gson();
                        ArrayList<Map> accountMap =  gson.fromJson(result, MessageSuccessfulLogin.class).getMapList();
                        loginSuccessful(accountMap);
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
                }

            }
        });

        TextView dnd = findViewById(R.id.textDnd);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager.isNotificationPolicyAccessGranted()) {
            dnd.setVisibility(View.INVISIBLE);
        }

        dnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!client.isMessageHandlerRunning())
            client.start();
    }

    private void loginSuccessful(ArrayList<Map> accountMap){
        Intent changeActivity;
        changeActivity = new Intent(this,ListMapActivity.class);
        changeActivity.putExtra("Map",accountMap);
        startActivity(changeActivity);
    }

    @Override
    protected void onDestroy() {

        if(client != null)
            client.closeConnection();
        btUtility = null;
        super.onDestroy();

    }
}