package com.example.parkmapproject.me_ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.parkmapproject.R;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        final EditText uSubject = findViewById(R.id.subject);
        final EditText uFeedback = findViewById(R.id.feedback);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button submitButton = findViewById(R.id.submit_feedback);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uFeedback.getText().toString().trim().isEmpty()) {
                    displayToast(getString(R.string.feedback_ask));
                    return;
                }
                Intent sendEmail = new Intent(Intent.ACTION_SEND);
                sendEmail.setType("message/rfc822");
                sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"tam.ngocbang.nguyen@gmail.com"});
                sendEmail.putExtra(Intent.EXTRA_SUBJECT, uSubject.getText().toString());
                sendEmail.putExtra(Intent.EXTRA_TEXT, uFeedback.getText().toString());

                try {
                    startActivity(Intent.createChooser(sendEmail, getString(R.string.email_via)));
                }
                catch (ActivityNotFoundException e) {
                    displayToast(getString(R.string.no_email_clients));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.homeAsUp) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
