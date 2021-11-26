package com.example.iot_laundry;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.iot_laundry.Utils.MyServer;
import com.example.iot_laundry.firebase.MyFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;

public class ServerSettingDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private static String TAG = ServerSettingDialog.class.getSimpleName();

    EditText et1, et2, et3;
    Button btn_save;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout_server_setting, container, false);

        /* binding views and initializing */
        et1 = view.findViewById(R.id.et1);
        et2 = view.findViewById(R.id.et2);
        et3 = view.findViewById(R.id.et3);
        btn_save = view.findViewById(R.id.btn_save);

        btn_save.setOnClickListener(this);

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

        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                String server1 = String.valueOf(et1.getText());
                String server2 = String.valueOf(et2.getText());
                String server3 = String.valueOf(et3.getText());

                String serverAddress = server1 + "." + server2 + "." + server3 + ".";
                MyServer.setServerAddress(serverAddress);

                dismiss();
                break;
        }
    }


}