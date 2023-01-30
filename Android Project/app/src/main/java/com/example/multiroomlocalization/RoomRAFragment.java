package com.example.multiroomlocalization;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RoomRAFragment extends Fragment {


    private BluetoothControlFragment bluetoothControl;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);


        return inflater.inflate(R.layout.fragment_assignra, container, false);
    }



    @Override
    public void onStart() {
        super.onStart();

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        Button finishFragment = (Button) activity.findViewById(R.id.close_fragment);
        finishFragment.setOnClickListener(this::doneAssignments);


        bluetoothControl = new BluetoothControlFragment(this);

        bluetoothControl.setupBluetoothAndScan();
    }


    private void doneAssignments(View view) {

        ArrayList<ListRoomsElement> deviceForRoom = bluetoothControl.getRoomsChoices();
        if(deviceForRoom != null) {
            bluetoothControl.closeControl();
            getParentFragmentManager().popBackStack();

            Bundle bundle = new Bundle();
            bundle.putSerializable("deviceForRoom", deviceForRoom);
            getParentFragmentManager().setFragmentResult("requestDevice",bundle);
        }
        else {
            FragmentActivity activity = getActivity();


            if (activity == null)
                return;

            AlertDialog alertDialog = new AlertDialog.Builder(activity)
                    .setMessage("Cannot assign same speaker to different rooms. Reassign them")
                    .setCancelable(true)
                    .create();
            alertDialog.show();
            //Toast.makeText(activity, "Cannot assign same speaker to different rooms. Reassign them", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetoothControl.closeControl();
    }
}