package com.group.amplifate;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class IntroViewPagerAdapter extends PagerAdapter {

    Context context;
    List<ScreenItem> mListScreen;

    public IntroViewPagerAdapter(Context context, List<ScreenItem> mListScreen) {
        this.context = context;
        this.mListScreen = mListScreen;
    }

    @Override
    public int getCount() {
        return mListScreen.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layoutScreen = inflater.inflate(R.layout.intro_layout, null);

        final ImageView imgSlide = layoutScreen.findViewById(R.id.intro_img);
        TextView title = layoutScreen.findViewById(R.id.intro_title);
        TextView description = layoutScreen.findViewById(R.id.intro_description);

        title.setText(mListScreen.get(position).getTitle());
        description.setText(mListScreen.get(position).getDescription());
        imgSlide.setImageResource(mListScreen.get(position).getScreenimg());

        container.addView(layoutScreen);

        imgSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable d = imgSlide.getDrawable();

                        if(d instanceof Animatable){
                            ((Animatable) d).start();
                        }
            }
        });
        return layoutScreen;

    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((View)object);
    }
}
