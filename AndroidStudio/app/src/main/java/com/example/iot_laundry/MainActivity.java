package com.example.iot_laundry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.buttonStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DryingActivity.class);

                startActivity(intent);

                String activityState = "start";
                String serverAdress = "";

                DryingActivity.HttpRequestTask requestTask = new DryingActivity.HttpRequestTask(serverAdress);
                requestTask.execute(activityState);
            }
        });

    }
}