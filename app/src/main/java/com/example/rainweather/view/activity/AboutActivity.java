package com.example.rainweather.view.activity;


import android.os.Bundle;

import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rainweather.R;

public class AboutActivity extends AppCompatActivity {

    private ImageButton btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        iniView();

    }

    private void iniView() {
        btnReturn = findViewById(R.id.btn_return_about);
        btnReturn.setOnClickListener(v -> finish());

    }
}
