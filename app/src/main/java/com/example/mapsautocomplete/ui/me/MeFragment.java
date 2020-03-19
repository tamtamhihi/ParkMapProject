package com.example.mapsautocomplete.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mapsautocomplete.FeedbackActivity;
import com.example.mapsautocomplete.R;

public class MeFragment extends Fragment {

    private MeViewModel notificationsViewModel;
    private ImageView avatar;
    private RelativeLayout feedback;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = new ViewModelProvider(this).get(MeViewModel.class);
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
