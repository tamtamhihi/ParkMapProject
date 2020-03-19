package com.example.mapsautocomplete;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private String[] imgUrl;

    public ViewPagerAdapter(Context context, String[] url) {
        this.context = context;
        this.imgUrl = url;
    }
    @Override
    public int getCount() {
        return imgUrl.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView img = new ImageView(context);
        Glide.with(context).load(imgUrl[position]).centerCrop().into(img);
        container.addView(img);
        return img;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
