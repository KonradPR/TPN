package com.example.tpn;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

public class CustomSwipeAdapter extends PagerAdapter {
    //static public int[] image_resource = {R.drawable.main,R.drawable.main,R.drawable.main,R.drawable.main,R.drawable.main};
    static public String[] labels ={"1","2","3","4","5"};
    static public Drawable[] drawables = new Drawable[5];
    private Context ctx;
    private LayoutInflater layoutInflater;

    public CustomSwipeAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return drawables.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        ImageView imageview = (ImageView) item_view.findViewById(R.id.image_view);
        TextView textView = (TextView)  item_view.findViewById(R.id.image_count);
        //imageview.setImageResource(image_resource[position]);
        imageview.setImageDrawable(drawables[position]);
        textView.setText(labels[position]);
        container.addView(item_view);
        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }

}