// Generated by view binder compiler. Do not edit!
package com.example.multiroomlocalization.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.multiroomlocalization.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class PopupRoomBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button buttonCancel;

  @NonNull
  public final Button buttonSave;

  @NonNull
  public final EditText labelRoom;

  @NonNull
  public final TextView textView;

  private PopupRoomBinding(@NonNull LinearLayout rootView, @NonNull Button buttonCancel,
      @NonNull Button buttonSave, @NonNull EditText labelRoom, @NonNull TextView textView) {
    this.rootView = rootView;
    this.buttonCancel = buttonCancel;
    this.buttonSave = buttonSave;
    this.labelRoom = labelRoom;
    this.textView = textView;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static PopupRoomBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static PopupRoomBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.popup_room, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static PopupRoomBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.buttonCancel;
      Button buttonCancel = ViewBindings.findChildViewById(rootView, id);
      if (buttonCancel == null) {
        break missingId;
      }

      id = R.id.buttonSave;
      Button buttonSave = ViewBindings.findChildViewById(rootView, id);
      if (buttonSave == null) {
        break missingId;
      }

      id = R.id.labelRoom;
      EditText labelRoom = ViewBindings.findChildViewById(rootView, id);
      if (labelRoom == null) {
        break missingId;
      }

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      return new PopupRoomBinding((LinearLayout) rootView, buttonCancel, buttonSave, labelRoom,
          textView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
