package com.example.parkmapproject.main_ui.me;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.parkmapproject.R;

import java.util.Objects;

public class FeedbackActivity extends AppCompatActivity {
    EditText uSubject, uFeedback;
    private static final int EMAIL_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        uSubject = findViewById(R.id.subject);
        uFeedback = findViewById(R.id.feedback);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                    startActivityForResult(Intent.createChooser(sendEmail, getString(R.string.email_via)), EMAIL_CODE);
                }
                catch (ActivityNotFoundException e) {
                    displayToast(getString(R.string.no_email_clients));
                    finish();
                    overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EMAIL_CODE && resultCode == RESULT_OK) {
            finish();
            overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!uSubject.getText().toString().trim().equals("") && !uFeedback.getText().toString().trim().equals("")) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Lose information");
                alertDialog.setMessage("You haven't sent your feedback yet. Going back will lose your information. Are you sure?");
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Go back anyway", (dialog, which) -> {
                    finish();
                    overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
                });
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Stay here", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
            else {
                finish();
                overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!uSubject.getText().toString().trim().equals("") && !uFeedback.getText().toString().trim().equals("")) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.feedback_alert_title));
            alertDialog.setMessage(getString(R.string.feedback_alert_msg));
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.feedback_alert_negative_button), (dialog, which) -> {
                finish();
                overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
            });
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.feedback_alert_positive_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
        else {
            finish();
            overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
        }
    }

    private void displayToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
