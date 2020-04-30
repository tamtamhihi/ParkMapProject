package com.example.parkmapproject.main_ui.me;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.parkmapproject.R;

public class AboutActivity extends AppCompatActivity {
    private Button feedbackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        feedbackButton = findViewById(R.id.feedback_button);
        feedbackButton.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
        }
        return true;
    }
}
