package com.example.iot_laundry;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private static String TAG = SettingDialog.class.getSimpleName();

    RadioGroup radioGroup_ac, radioGroup_window, radioGroup_curtain;
    RadioButton radioButton_ac_on, radioButton_ac_off, radioButton_window_on, radioButton_window_off, radioButton_curtain_on, radioButton_curtain_off;
    Button btn_save;


    //DB - firestore
    Long time;

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
                FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
//                int height = displayMetrics.heightPixels;
//                int maxHeight = (int) (height*0.93);
//                BottomSheetBehavior.from(bottomSheet).setPeekHeight(maxHeight);
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


                dismiss();
                break;
        }
    }
}