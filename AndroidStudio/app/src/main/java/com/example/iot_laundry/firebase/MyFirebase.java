package com.example.iot_laundry.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebase {
    private static final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    public static final DatabaseReference startRef = root.child("start");
    public static final DatabaseReference startMoistRef = root.child("startMoist");

    public static final DatabaseReference moistRef = root.child("moist");

    public static final DatabaseReference acRef = root.child("airconditioner");
    public static final DatabaseReference winRef = root.child("window");
    public static final DatabaseReference curtRef = root.child("curtain");
//    public static DatabaseReference myRef = root.getReference("iot-laundry02-default-rtdb");

}
