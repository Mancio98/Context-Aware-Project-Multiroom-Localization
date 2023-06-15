package com.example.multiroomlocalization;

import static com.example.multiroomlocalization.Bluetooth.BluetoothUtility.BT_CONNECT_AND_SCAN;
import static com.example.multiroomlocalization.LoginActivity.btUtility;

import com.example.multiroomlocalization.Bluetooth.BluetoothUtility;

import com.example.multiroomlocalization.Bluetooth.ScanBluetoothService;
import com.example.multiroomlocalization.messages.connection.MessageLogin;
import com.example.multiroomlocalization.messages.connection.MessageSuccessfulLogin;
import com.example.multiroomlocalization.messages.localization.MessageEndMappingPhase;
import com.example.multiroomlocalization.messages.localization.MessageEndScanReferencePoint;
import com.example.multiroomlocalization.messages.localization.MessageFingerprint;
import com.example.multiroomlocalization.messages.localization.MessageNewReferencePoint;
import com.example.multiroomlocalization.messages.localization.MessageStartMappingPhase;
import com.example.multiroomlocalization.messages.localization.MessageStartScanReferencePoint;
import com.example.multiroomlocalization.speaker.Speaker;
import com.example.multiroomlocalization.localization.ReferencePoint;

import android.Manifest;
import android.app.Activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Handler;


import android.os.IBinder;
import android.util.Base64;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.io.ByteArrayOutputStream;

import java.io.Serializable;
import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.multiroomlocalization.socket.ClientSocket;


import com.google.android.material.floatingactionbutton.FloatingActionButton;


import com.google.gson.Gson;

import android.view.ViewTreeObserver;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements ServiceConnection {


    public static final int BT_INTERRUPT_DISCOVERY = 101;
    public static final int BT_SCAN = 102;
    public static final int BT_LIST_BOND = 103;
    public static final int BT_PAIR_DEVICE = 104;
    private Activity activity;

    private ImageView playPause;

    private Handler mHandler = new Handler();
    private ScanService scanService;
    private Canvas canvas;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText labelRoom;
    private Button cancel, save, upload;
    private ImageView imageView;
    private Bitmap mutableBitmap;
    private int imageViewHeight;
    private int imageViewWidth;
    private boolean first = true;
    private boolean newImage = true;
    private boolean passwordEmpty = true;
    private boolean nameEmpty = true;
    private String imageMap;
    private int len;
    private byte[] bb;

    private int intervalScan = 30000;
    private int timerScanTraining = 10000; //* 5 //60000 = 1 min
    private FloatingActionButton fab;

    protected ClientSocket clientSocket;
    private CropView cv;

    private ArrayList<ReferencePoint> referencePoints = new ArrayList<ReferencePoint>();

    private ArrayList<com.example.multiroomlocalization.ScanResult> scanResultArrayList = new ArrayList<com.example.multiroomlocalization.ScanResult>();

    private int seekPosition;
    private ImageButton nextTrack;
    private ImageButton previousTrack;
    private ArrayList<ListRoomsElement> deviceForRoom;
    private boolean onTop = false;
    private float startPlaylistX;
    private float startPlaylistY;
    private ArrayList<Speaker> listSpeaker;
    private TextView timeTextView;
    private ListView audioPlaylistView;


    private final Gson gson = new Gson();
    private ControlAudioService setupAudioService;
    private ReferencePointListAdapter adapterReferencePointList;
    //private ScanBluetooth scanBluetoothManager;
    private ScanBluetoothService scanBluetoothService;
    private boolean isBound=false;


    public interface BluetoothPermCallback {
        void onGranted();
        default void notGranted(){

        };
    }

    public static BluetoothPermCallback btPermissionCallback;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        setContentView(R.layout.activity_main);
        clientSocket = LoginActivity.client;

        fab = findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 1);
            }
        });

        activity = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String address = extras.getString("add");
            Integer port = extras.getInt("port");
            //The key argument here must match that used in the other activity
            //clientSocket.setAddress(address, port);
        }

        imageView = (ImageView) findViewById(R.id.map);
        // TODO DA PROVARE
        //imageView.getLayoutParams().height = 2000;
        //imageView.getLayoutParams().width = 1400;
        //imageView.requestLayout();

        if (imageView.getDrawable() == null) {
            dialogBuilder = new AlertDialog.Builder(this);
            final View popup = getLayoutInflater().inflate(R.layout.popup_upload_map, null);
            upload = (Button) popup.findViewById(R.id.upload);

            dialogBuilder.setView(popup);
            dialog = dialogBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // create an instance of the
                    // intent of the type image
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    someActivityResultLauncher.launch(i);

                }
            });

        } else {

            imageView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

            imageView.setOnTouchListener(touchListener);

        }

        /*
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.setFragmentResultListener("requestDevice", this, (requestKey, result) -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                deviceForRoom = result.getSerializable("deviceForRoom", ArrayList.class);

            } else
                deviceForRoom = (ArrayList<ListRoomsElement>) result.getSerializable("deviceForRoom");

            if (deviceForRoom != null) {
                listSpeaker = new ArraySet<>();
                deviceForRoom.forEach(room -> {
                    BluetoothDevice device = room.getDevice();
                    listSpeaker.add(new Speaker(device.getName(), device.getAddress(), room.getName()));
                });


                //clientSocket.sendMessageListSpeaker(listSpeaker);


                /* fare metodo per inizializzre il thread
                connectBluetoothThread = new ConnectBluetoothThread(activity);

                IntentFilter filter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
                filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_UUID);
                registerReceiver(btUtility.getConnectA2dpReceiver(), filter);


            }

        });*/


        //scanBluetoothManager = new ScanBluetooth(getApplicationContext(), activity);
    }


/*
        imageView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageViewHeight = imageView.getHeight();
                imageViewWidth = imageView.getWidth();
                int xGlobal = imageView.getLeft();
                int yGlobal = imageView.getTop();

                System.out.println("Global");
                System.out.println("X: " + xGlobal);
                System.out.println("Y: " + yGlobal);
                System.out.println("Height: " + imageViewHeight + " Width: " + imageViewWidth);
                // don't forget to remove the listener to prevent being called again
                // by future layout events:
                if(first || newImage) {
                    first=false;
                    newImage = false;
                    Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap(), imageViewWidth, imageViewHeight, true);
                    mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    canvas = new Canvas(mutableBitmap);

                }
            });

        }else{

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

        imageView.setOnTouchListener(touchListener);

                createDialog(tempx,tempy);
                return false;
            }
        });*/


    @Override
    protected void onStart() {
        super.onStart();
        btUtility = new BluetoothUtility(this, activity);

        Intent intent = new Intent(this, ScanBluetoothService.class);

        bindService(intent, this, Context.BIND_AUTO_CREATE);

    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x1 = (int) event.getX();
            int y1 = (int) event.getY();

            System.out.println("X: " + x1 + " Y: " + y1);

            System.out.println("imageViewWidth: " + imageViewWidth + " imageViewHeight: " + imageViewHeight);


            float tempx = ((float) x1 / (float) imageViewWidth) * 100;
            float tempy = ((float) y1 / (float) imageViewHeight) * 100;

            System.out.println("tempX: " + tempx + " tempY: " + tempy);

            createDialog((int) tempx, (int) tempy);
            return false;
        }
    };

    ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            imageViewHeight = imageView.getHeight();
            imageViewWidth = imageView.getWidth();
            int xGlobal = imageView.getLeft();
            int yGlobal = imageView.getTop();

            System.out.println("Global");
            System.out.println("X: " + xGlobal);
            System.out.println("Y: " + yGlobal);
            System.out.println("Height: " + imageViewHeight + " Width: " + imageViewWidth);
            // don't forget to remove the listener to prevent being called again
            // by future layout events:
            if (first || newImage) {

                first = false;
                newImage = false;
                //Bitmap bitmap = Bitmap.createBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap(), imageViewWidth, imageViewHeight, true);
                mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                canvas = new Canvas(mutableBitmap);
                System.out.println("Canvas");
                System.out.println(canvas.getWidth());
                System.out.println(canvas.getHeight());

            }
        }
    };

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            scanService.startScan();
            mHandler.postDelayed(scanRunnable, intervalScan);

        }
    };

    private void stopScan() {
        mHandler.removeCallbacks(scanRunnable);
    }


   /* @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            dialogBuilder = new AlertDialog.Builder(this);
            final View popup = getLayoutInflater().inflate(R.layout.popup_upload_map, null);
            upload = (Button) popup.findViewById(R.id.upload);

            dialogBuilder.setView(popup);
            dialog = dialogBuilder.create();

            dialog.show();

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // create an instance of the
                    // intent of the type image
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    someActivityResultLauncher.launch(i);
                }
            });


            System.out.println("Presss");
            return true;
        } else if (id == R.id.action_client) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Uri image = data.getData();
                        //Uri destImage = null;

                        if (null != image) {

                            dialog.cancel();
                            cv = new CropView(MainActivity.this, image);
                            cv.setCanceledOnTouchOutside(false);
                            cv.show();
                            cv.setTouchButton(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        //t.run();
                                        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 3);
                                    }
                                    return false;
                                }
                            });
                        }



                    }
                }
            });




    private void startPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
        Button next = (Button) popup.findViewById(R.id.buttonPopup);
        TextView text = (TextView) popup.findViewById(R.id.textPopup);

        final boolean[] firstTime = {true};

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstTime[0]) {
                    text.setText(getString(R.string.addReferencePointpopupSecond));
                    next.setText("Start");
                    firstTime[0] = false;
                } else {
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
                    dialog.cancel();
                }
            }
        });
        text.setText(getString(R.string.addReferencePointpopup));

        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        System.out.println("show start popup!");
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        } else {
            switch (requestCode) {
                case 1:
                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 2);
                    break;
                case 2:
                    System.out.println("CASO 2");
                    ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
                        @Override
                        public void onComplete(String result) {
                            System.out.println("response");
                            System.out.println(result);

                            ClientSocket.Callback<String> callback1 = new ClientSocket.Callback<String>() {
                                @Override
                                public void onComplete(String result) {
                                    fab.setEnabled(true);
                                    createPopupStartTraining();
                                }
                            };

                            clientSocket.sendImage(bb, callback1);
                        }
                    };
                    Gson gson = new Gson();
                    MessageStartMappingPhase message = new MessageStartMappingPhase(len);
                    String json = gson.toJson(message);

                    clientSocket.sendMessageStartMappingPhase(callback, json);
                    fab.setEnabled(false);

                    break;
                case 3:

                    System.out.println("CASO 3");
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), cv.getCropImageView().getCroppedImage(), "IMG_" + Calendar.getInstance().getTime(), null);
                    System.out.println("PATH: " + path);
                    final Bitmap[] bmap = new Bitmap[1];
                    CountDownLatch latch = new CountDownLatch(1);

                    imageView.setImageURI(Uri.parse(path));
                    imageView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
                    imageView.setOnTouchListener(touchListener);

                    newImage = true;

                    cv.cancel();
                    t.start();
                    /*
                    imageView.buildDrawingCache();
                    bmap[0] = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    latch.countDown();


                    System.out.println("BMAP SIZE");
                    System.out.println(bmap[0].getWidth());
                    System.out.println(bmap[0].getHeight());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bmap[0].compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bb = bos.toByteArray();
                    imageMap = Base64.encodeToString(bb, 0);

                    len = bb.length;*/

                    startPopup();
                    break;
            }

        }
    }


    private void createDialog(int x, int y) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popup_room, null);
        labelRoom = (EditText) popup.findViewById(R.id.labelRoom);
        cancel = (Button) popup.findViewById(R.id.buttonCancel);
        save = (Button) popup.findViewById(R.id.buttonSave);

        save.setEnabled(false);

        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        labelRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().trim().length() == 0) {
                    save.setEnabled(false);
                } else {
                    save.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tempX = (x * imageViewWidth) / 100;
                int tempY = (y * imageViewHeight) / 100;

                Paint mPaint = new Paint();
                mPaint.setColor(Color.RED);
                canvas.drawCircle(tempX, tempY, 20, mPaint);
                imageView.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
                System.out.println("Canvas");
                System.out.println(canvas.getWidth());
                System.out.println(canvas.getHeight());
                System.out.println("IMAGEVIEW");
                System.out.println(imageView.getWidth());
                System.out.println(imageView.getHeight());
                System.out.println("imageViewHEIGHT&WIDTH");
                System.out.println(imageViewWidth);
                System.out.println(imageViewHeight);
                ReferencePoint ref = new ReferencePoint(x, y, labelRoom.getText().toString());
                referencePoints.add(ref);
                Toast.makeText(getApplicationContext(), "Stanza aggiunta correttamente", Toast.LENGTH_LONG).show();
                fab.setEnabled(true);
                dialog.cancel();
            }
        });

    }


    //listener to get if user granted permission for bluetooth connect and scan (only for sdk > 30)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case BT_CONNECT_AND_SCAN:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(grantResults.length > 1){
                        if(grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            btPermissionCallback.onGranted();


                            Toast.makeText(this, "BT Permission Granted", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btPermissionCallback.onGranted();


                        Toast.makeText(this, "BT Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "BT Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 2);
                    System.out.println("CASE1");
                } else {
                    Toast.makeText(MainActivity.this, "PROBLEM", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("CASE2");
                    ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
                        @Override
                        public void onComplete(String result) {
                            System.out.println("response");
                            System.out.println(result);

                            ClientSocket.Callback<String> callback1 = new ClientSocket.Callback<String>() {
                                @Override
                                public void onComplete(String result) {
                                    fab.setEnabled(true);
                                    createPopupStartTraining();
                                }
                            };

                            clientSocket.sendImage(bb, callback1);
                        }
                    };
                    Gson gson = new Gson();
                    MessageStartMappingPhase message = new MessageStartMappingPhase(len);
                    String json = gson.toJson(message);

                    clientSocket.sendMessageStartMappingPhase(callback, json);
                    fab.setEnabled(false);
                } else {
                    Toast.makeText(MainActivity.this, "PROBLEM", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:

                    System.out.println("CASO 3 ALTRO");
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), cv.getCropImageView().getCroppedImage(), "IMG_" + Calendar.getInstance().getTime(), null);
                    System.out.println("PATH: " + path);

                    imageView.setImageURI(Uri.parse(path));
                    imageView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
                    imageView.setOnTouchListener(touchListener);

                    newImage = true;

                    cv.cancel();

                    t.start();


                    startPopup();



                break;
        }
    }

    Thread t = new Thread(){
        public void run(){
            super.run();
            imageView.buildDrawingCache();

            Bitmap bmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            System.out.println("BMAP SIZE");
            System.out.println(bmap.getWidth());
            System.out.println(bmap.getHeight());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bb = bos.toByteArray();
            imageMap = Base64.encodeToString(bb, 0);

            len = bb.length;
        }
    };
    private void createPopupRoomTraining(ReferencePoint point, int index) {

        System.out.println("scan: "+point.getId());
        dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final View popup = getLayoutInflater().inflate(R.layout.layout_scan_training, null);
        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        Button buttonNext = popup.findViewById(R.id.buttonTraining);

        TextView textRoom = (TextView) popup.findViewById(R.id.textRoom);
        textRoom.setText(point.getId());

        TextView timer = (TextView) popup.findViewById(R.id.timer);
        timer.setText("seconds remaining: 05:00");

        MessageNewReferencePoint message = new MessageNewReferencePoint(point.getX(), point.getY(), point);
        Gson gson = new Gson();
        String json = gson.toJson(message);
        clientSocket.sendMessageNewReferencePoint(json);


        //TODO SISTEMARE TIMER CHE FACCIANO 5 MINUTI ADESSO IMPOSTATO A 10s
        CountDownTimer countDownTimer = new CountDownTimer(timerScanTraining, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText("seconds remaining: " + new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));
            }

            public void onFinish() {

                buttonNext.setEnabled(true);
                buttonNext.setText("STOP");
                buttonNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        timer.setText("Stanza completata");
                        System.out.println("REFERENCE");
                        System.out.println(referencePoints.size());
                        System.out.println(index);

                        mHandler.removeCallbacks(scanRunnable);

                        if (index + 1 < referencePoints.size()) {
                            buttonNext.setText("Next");
                            buttonNext.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.cancel();
                                    ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
                                        @Override
                                        public void onComplete(String result) {
                                            createPopupRoomTraining(referencePoints.get(index + 1), index + 1);
                                        }
                                    };
                                    Gson gson = new Gson();
                                    MessageEndScanReferencePoint message = new MessageEndScanReferencePoint();
                                    String json = gson.toJson(message);
                                    clientSocket.sendMessageEndScanReferencePoint(json, callback);
                                }
                            });
                        } else {
                            buttonNext.setText("Finish");
                            buttonNext.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
                                        @Override
                                        public void onComplete(String result) {

                                            dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                            final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
                                            dialogBuilder.setView(popup);

                                            Button next = (Button) popup.findViewById(R.id.buttonPopup);
                                            TextView text = (TextView) popup.findViewById(R.id.textPopup);

                                            text.setText(R.string.settings);
                                            next.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    dialog.cancel();
                                                    dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                                    final View popup = getLayoutInflater().inflate(R.layout.referencepointlist_view, null);
                                                    dialogBuilder.setView(popup);

                                                    RecyclerView recyclerView = (RecyclerView) popup.findViewById(R.id.recyclerViewReferencePoint);

                                                    adapterReferencePointList = new ReferencePointListAdapter(referencePoints, getApplicationContext(), scanBluetoothService);

                                                    recyclerView.setAdapter(adapterReferencePointList);
                                                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                                                    System.out.println(referencePoints.size());
                                                    Button buttonConferma = (Button) popup.findViewById(R.id.buttonConfermaSettings);

                                                    buttonConferma.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            adapterReferencePointList.closeBluetoothScan();
                                                            ArrayList<Settings> arrListSettings = new ArrayList<>();

                                                            for (int i = 0; i < referencePoints.size(); i++) {
                                                                System.out.println("DND");
                                                                System.out.println(referencePoints.get(i).getDnd());
                                                                System.out.println("SPEAKER");
                                                                System.out.println(referencePoints.get(i).getSpeaker().getName());
                                                                arrListSettings.add(new Settings(referencePoints.get(i).getId(), referencePoints.get(i).getSpeaker(), referencePoints.get(i).getDnd()));
                                                            }

                                                            dialog.cancel();
                                                            dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                                            final View popup = getLayoutInflater().inflate(R.layout.layout_password_map, null);
                                                            dialogBuilder.setView(popup);
                                                            dialog = dialogBuilder.create();
                                                            dialog.setCanceledOnTouchOutside(false);
                                                            dialog.setCancelable(false);

                                                            Button button = popup.findViewById(R.id.buttonSendPassword);
                                                            button.setEnabled(false);
                                                            EditText name = popup.findViewById(R.id.editTextInputMapName);
                                                            EditText password = popup.findViewById(R.id.passwordInputMap);

                                                            password.addTextChangedListener(new TextWatcher() {
                                                                @Override
                                                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                                }

                                                                @Override
                                                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                                    if (charSequence.toString().trim().length() == 0) {
                                                                        passwordEmpty = true;
                                                                        button.setEnabled(false);
                                                                    } else {
                                                                        passwordEmpty = false;
                                                                        if (!nameEmpty) {
                                                                            button.setEnabled(true);
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void afterTextChanged(Editable editable) {

                                                                }
                                                            });

                                                            name.addTextChangedListener(new TextWatcher() {
                                                                @Override
                                                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                                }

                                                                @Override
                                                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                                    if (charSequence.toString().trim().length() == 0) {
                                                                        nameEmpty = true;
                                                                        button.setEnabled(false);
                                                                    } else {
                                                                        nameEmpty = false;
                                                                        if (!passwordEmpty) {
                                                                            button.setEnabled(true);
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void afterTextChanged(Editable editable) {

                                                                }
                                                            });

                                                            button.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    button.setEnabled(false);

                                                                    ClientSocket.Callback<String> callback = new ClientSocket.Callback<String>() {
                                                                        @Override
                                                                        public void onComplete(String result) {
                                                                            System.out.println(password.getText().toString());
                                                                            System.out.println(result);

                                                                            ClientSocket.Callback<String> callbackSuccessful = new ClientSocket.Callback<String>() {
                                                                                @Override
                                                                                public void onComplete(String result) {
                                                                                    System.out.println(result);
                                                                                    Gson gson = new Gson();
                                                                                    ArrayList<Map> accountMap = gson.fromJson(result, MessageSuccessfulLogin.class).getMapList();
                                                                                    Intent changeActivity;
                                                                                    changeActivity = new Intent(MainActivity.this, ListMapActivity.class);
                                                                                    changeActivity.putExtra("Map", accountMap);
                                                                                    finish();
                                                                                    dialog.cancel();
                                                                                    startActivity(changeActivity);
                                                                                }
                                                                            };

                                                                            Gson gson = new Gson();
                                                                            MessageLogin message = new MessageLogin(LoginActivity.currentUser);
                                                                            String json = gson.toJson(message);
                                                                            clientSocket.sendMessageLogin(callbackSuccessful, null, json);

                                                                           /* ClientSocket.Callback<String> callback1 = new ClientSocket.Callback<String>() {
                                                                                @Override
                                                                                public void onComplete(String result) {
                                                                                    Toast.makeText(getApplicationContext(), gson.fromJson(result, MessageChangeReferencePoint.class).getReferencePoint().getId(),Toast.LENGTH_LONG).show();
                                                                                }
                                                                            };
                                                                            clientSocket.addCallbackChangeReferencePoint(callback1);
                                                                            //AGGIUNTO PER PROVARE CON MATTI POI SARà DA ELIMINARE
                                                                            //mHandler.postDelayed(scanRunnable, 5000);

                                                                            */
                                                                        }
                                                                    };

                                                                    Gson gson = new Gson();
                                                                    MessageEndMappingPhase message = new MessageEndMappingPhase(password.getText().toString(), arrListSettings, name.getText().toString());
                                                                    String json = gson.toJson(message);
                                                                    clientSocket.sendMessageEndMappingPhase(callback, json);

                                                                }
                                                            });
                                                            dialog.show();
                                                        }
                                                    });

                                                    dialog = dialogBuilder.create();
                                                    dialog.setCanceledOnTouchOutside(false);
                                                    dialog.setCancelable(false);
                                                    dialog.show();

                                                }
                                            });

                                            dialog = dialogBuilder.create();
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.setCancelable(false);
                                            dialog.show();


                                        }
                                    };

                                    Gson gson = new Gson();
                                    MessageEndScanReferencePoint message = new MessageEndScanReferencePoint();
                                    String json = gson.toJson(message);
                                    clientSocket.sendMessageEndScanReferencePoint(json, callback);
                                    dialog.cancel();
                                }

                            });
                        }
                    }
                });
            }
        };

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanService = new ScanService(getApplicationContext());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!scanService.getWifiManager().isScanThrottleEnabled()) {//Settings.Global.getInt(getApplicationContext().getContentResolver(), "wifi_scan_throttle_enabled") == 0){
                        intervalScan = 5000;
                        System.out.println("IntervalScan: " + intervalScan);
                    } else {
                        intervalScan = 30000;
                        System.out.println("IntervalScan: " + intervalScan);
                    }
                }

                mHandler.postDelayed(scanRunnable, 0);
                scanService.registerReceiver(broadcastReceiverScan);

                scanResultArrayList.clear();
                countDownTimer.start();

                Gson gson = new Gson();
                MessageStartScanReferencePoint message = new MessageStartScanReferencePoint();
                String json = gson.toJson(message);
                clientSocket.sendMessageStartScanReferencePoint(json);
                buttonNext.setEnabled(false);
            }
        });

        dialog.show();
    }

    private void createPopupStartTraining() {
        dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final View popup = getLayoutInflater().inflate(R.layout.popup_text, null);
        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();


        Button next = (Button) popup.findViewById(R.id.buttonPopup);
        TextView text = (TextView) popup.findViewById(R.id.textPopup);

        text.setText(getString(R.string.trainingText));
        next.setText("Next");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                int i = 0;
                createPopupRoomTraining(referencePoints.get(i), i);
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }


    private BroadcastReceiver broadcastReceiverScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }

        private void scanSuccess() {
            List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
            List<com.example.multiroomlocalization.ScanResult> listScan = new ArrayList<>();
            for (ScanResult res : results) {
                com.example.multiroomlocalization.ScanResult scan = new com.example.multiroomlocalization.ScanResult(res.BSSID, res.SSID, res.level);
                System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID + " level: " + res.level);
                listScan.add(scan);
            }

            Gson gson = new Gson();
            MessageFingerprint message = new MessageFingerprint(listScan);
            String json = gson.toJson(message);
            clientSocket.sendMessageFingerprint(json);
        }

        private void scanFailure() {

            List<android.net.wifi.ScanResult> results = scanService.getWifiManager().getScanResults();
            List<com.example.multiroomlocalization.ScanResult> listScan = new ArrayList<>();
            for ( ScanResult res : results ) {
                com.example.multiroomlocalization.ScanResult scan = new com.example.multiroomlocalization.ScanResult(res.BSSID,res.SSID,res.level);
                System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
                listScan.add(scan);
            }

            Gson gson = new Gson();
            MessageFingerprint message = new MessageFingerprint(listScan);
            String json = gson.toJson(message);
            clientSocket.sendMessageFingerprint(json);
       }


   };



    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(this);
            isBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {


        ScanBluetoothService.LocalBinder binder = (ScanBluetoothService.LocalBinder) service;
        scanBluetoothService = binder.getService();
        scanBluetoothService.setContext(getApplicationContext());

        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity = null;
    }
}
