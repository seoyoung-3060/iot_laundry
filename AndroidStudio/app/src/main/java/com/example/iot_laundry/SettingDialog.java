package com.example.iot_laundry;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.iot_laundry.firebase.MyFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

public class SettingDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private static String TAG = SettingDialog.class.getSimpleName();

    RadioGroup radioGroup_ac, radioGroup_window, radioGroup_curtain;
    RadioButton radioButton_ac_on, radioButton_ac_off, radioButton_window_on, radioButton_window_off, radioButton_curtain_on, radioButton_curtain_off;
    Button btn_save;

    private BottomSheetListener bottomSheetListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout_setting, container, false);

        /* binding views and initializing */
        radioGroup_ac = view.findViewById(R.id.radioGroup_ac);
        radioGroup_curtain = view.findViewById(R.id.radioGroup_curtain);
        radioGroup_window = view.findViewById(R.id.radioGroup_window);
        btn_save = view.findViewById(R.id.btn_save);

        radioGroup_ac.check(R.id.radioButton_ac_on);
        radioGroup_curtain.check(R.id.radioButton_curtain_on);
        radioGroup_window.check(R.id.radioButton_window_on);

        btn_save.setOnClickListener(this);


//        radioGroup_ac.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                }
//
//            }
//        });
//        radioGroup_curtain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//            }
//        });
//        radioGroup_window.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//            }
//        });
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
            }
        });

        MyFirebase.acRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase ac ", String.valueOf(task.getResult().getValue())); //값은 잘 불러와지는데
                    boolean acVal = (boolean) task.getResult().getValue();
                    if (acVal == true) radioGroup_ac.check(R.id.radioButton_ac_on);
                    else radioGroup_ac.check(R.id.radioButton_ac_off);
                }
            }
        });
        MyFirebase.winRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase win", String.valueOf(task.getResult().getValue()));
                    boolean winVal = (boolean) task.getResult().getValue();
                    if (winVal == true) radioGroup_window.check(R.id.radioButton_window_on);
                    else radioGroup_window.check(R.id.radioButton_window_off);

                }
            }
        });
        MyFirebase.curtRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase curt", String.valueOf(task.getResult().getValue()));
                    boolean curVal = (boolean) task.getResult().getValue();
                    if (curVal == true) radioGroup_curtain.check(R.id.radioButton_curtain_on);
                    else radioGroup_curtain.check(R.id.radioButton_curtain_off);
                }
            }
        });


        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                int radioButtonId = radioGroup_ac.getCheckedRadioButtonId();
                if (radioButtonId == R.id.radioButton_ac_on) {
                    MyFirebase.acRef.setValue(true);
                } else if (radioButtonId == R.id.radioButton_ac_off) {
                    MyFirebase.acRef.setValue(false);
                }

                if (radioGroup_window.getCheckedRadioButtonId() == R.id.radioButton_window_on) {
                    MyFirebase.winRef.setValue(true);
                } else {
                    MyFirebase.winRef.setValue(false);
                }

                if (radioGroup_curtain.getCheckedRadioButtonId() == R.id.radioButton_curtain_on) {
                    MyFirebase.curtRef.setValue(true);
                } else {
                    MyFirebase.curtRef.setValue(false);
                }

                bottomSheetListener.onButtonApply();
                dismiss();
                break;
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            bottomSheetListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }


    public interface BottomSheetListener {
        void onButtonApply();
    }
}