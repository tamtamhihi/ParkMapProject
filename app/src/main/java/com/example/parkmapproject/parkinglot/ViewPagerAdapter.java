package com.example.parkmapproject.parkinglot;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<Bitmap> imgBitmaps;
    private String[] imgUrl;

    public ViewPagerAdapter(Context context, ArrayList<Bitmap> imgBitmaps) {
        this.context = context;
        this.imgBitmaps = imgBitmaps;
    }

    @Override
    public int getCount() {
        return imgBitmaps.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView img = new ImageView(context);
        Glide.with(context).load(imgBitmaps.get(position)).centerCrop().into(img);
        container.addView(img);
        return img;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}