package com.example.parkmapproject.main_ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.example.parkmapproject.R;

public class MeFragment extends Fragment {

    private ImageView avatar;
    private CoordinatorLayout howTo, feedback, aboutUs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_me, container, false);

        howTo = root.findViewById(R.id.how_to_option);
        feedback = root.findViewById(R.id.feedback_option);
        aboutUs = root.findViewById(R.id.about_us_option);

        howTo.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HowToActivity.class);
            startActivity(intent);
        });
        feedback.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FeedbackActivity.class);
            startActivity(intent);
        });
        aboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AboutActivity.class);
            startActivity(intent);
        });

        return root;
    }
}
