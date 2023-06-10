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

public final class PopupAddMapBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button buttonConfermaIdPassMap;

  @NonNull
  public final Button buttonCreateMap;

  @NonNull
  public final EditText editTextIdMapInput;

  @NonNull
  public final EditText editTextPasswordMapInput;

  @NonNull
  public final TextView textView4;

  @NonNull
  public final TextView textView5;

  @NonNull
  public final TextView textView6;

  @NonNull
  public final TextView textView7;

  @NonNull
  public final TextView textView9;

  private PopupAddMapBinding(@NonNull LinearLayout rootView,
      @NonNull Button buttonConfermaIdPassMap, @NonNull Button buttonCreateMap,
      @NonNull EditText editTextIdMapInput, @NonNull EditText editTextPasswordMapInput,
      @NonNull TextView textView4, @NonNull TextView textView5, @NonNull TextView textView6,
      @NonNull TextView textView7, @NonNull TextView textView9) {
    this.rootView = rootView;
    this.buttonConfermaIdPassMap = buttonConfermaIdPassMap;
    this.buttonCreateMap = buttonCreateMap;
    this.editTextIdMapInput = editTextIdMapInput;
    this.editTextPasswordMapInput = editTextPasswordMapInput;
    this.textView4 = textView4;
    this.textView5 = textView5;
    this.textView6 = textView6;
    this.textView7 = textView7;
    this.textView9 = textView9;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static PopupAddMapBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static PopupAddMapBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.popup_add_map, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static PopupAddMapBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.buttonConfermaIdPassMap;
      Button buttonConfermaIdPassMap = ViewBindings.findChildViewById(rootView, id);
      if (buttonConfermaIdPassMap == null) {
        break missingId;
      }

      id = R.id.buttonCreateMap;
      Button buttonCreateMap = ViewBindings.findChildViewById(rootView, id);
      if (buttonCreateMap == null) {
        break missingId;
      }

      id = R.id.editTextIdMapInput;
      EditText editTextIdMapInput = ViewBindings.findChildViewById(rootView, id);
      if (editTextIdMapInput == null) {
        break missingId;
      }

      id = R.id.editTextPasswordMapInput;
      EditText editTextPasswordMapInput = ViewBindings.findChildViewById(rootView, id);
      if (editTextPasswordMapInput == null) {
        break missingId;
      }

      id = R.id.textView4;
      TextView textView4 = ViewBindings.findChildViewById(rootView, id);
      if (textView4 == null) {
        break missingId;
      }

      id = R.id.textView5;
      TextView textView5 = ViewBindings.findChildViewById(rootView, id);
      if (textView5 == null) {
        break missingId;
      }

      id = R.id.textView6;
      TextView textView6 = ViewBindings.findChildViewById(rootView, id);
      if (textView6 == null) {
        break missingId;
      }

      id = R.id.textView7;
      TextView textView7 = ViewBindings.findChildViewById(rootView, id);
      if (textView7 == null) {
        break missingId;
      }

      id = R.id.textView9;
      TextView textView9 = ViewBindings.findChildViewById(rootView, id);
      if (textView9 == null) {
        break missingId;
      }

      return new PopupAddMapBinding((LinearLayout) rootView, buttonConfermaIdPassMap,
          buttonCreateMap, editTextIdMapInput, editTextPasswordMapInput, textView4, textView5,
          textView6, textView7, textView9);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
