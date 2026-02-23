package com.example.rainweather.view.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.rainweather.R;
import com.example.rainweather.databinding.ActivityMainBinding;
import com.example.rainweather.viewmodel.MainViewModel;
import com.google.android.material.button.MaterialButton;

public class AboutActivity extends AppCompatActivity {

    private ImageButton btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        iniView();

    }

    private void iniView(){
        btnReturn =findViewById(R.id.btn_return_about);
        btnReturn.setOnClickListener(v -> finish());

    }
}
