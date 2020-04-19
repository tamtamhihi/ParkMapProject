package com.example.parkmapproject.main_ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.parkmapproject.me_ui.FeedbackActivity;
import com.example.parkmapproject.R;

public class MeFragment extends Fragment {

    private ImageView avatar;
    private RelativeLayout feedback;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_me, container, false);

        avatar = root.findViewById(R.id.user_avatar);
        feedback = root.findViewById(R.id.feedback_option);

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FeedbackActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }
}
