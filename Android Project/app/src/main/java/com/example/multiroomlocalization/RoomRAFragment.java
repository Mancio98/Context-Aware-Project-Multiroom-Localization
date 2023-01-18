package com.example.multiroomlocalization;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class RoomRAFragment extends Fragment {


    private BluetoothControlFragment bluetoothControl;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_assignra, container, false);

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothControl = new BluetoothControlFragment(this);

    }

    @Override
    public void onStart() {
        super.onStart();

        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        Button finishFragment = (Button) activity.findViewById(R.id.close_fragment);
        finishFragment.setOnClickListener(this::doneAssignments);

        bluetoothControl.setupBluetoothAndScan();

    }


    private void doneAssignments(View view) {

        bluetoothControl.getRoomsChoices();
        getParentFragmentManager().popBackStack();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetoothControl.closeControl();
    }
}