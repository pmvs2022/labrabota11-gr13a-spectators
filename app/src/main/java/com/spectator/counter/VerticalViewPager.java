package com.spectator.counter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class VerticalViewPager extends ViewPager {


    public VerticalViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void  init(){
        setPageTransformer(true, new VerticalPage());
        setOverScrollMode(OVER_SCROLL_NEVER);

    }

    private MotionEvent getIntercambioXY(MotionEvent event){
        float width = getWidth();
        float height = getHeight();

        float newX = (event.getY() / height) * width;
        float newY = (event.getX() / width) * height;

        event.setLocation(newX, newY);
        return event;
    }

    public boolean onInterceptTouchEvent(MotionEvent event){
        boolean intercepted = super.onInterceptTouchEvent(getIntercambioXY(event));
        getIntercambioXY(event);
        return intercepted;
    }

    public boolean onTouchEvent(MotionEvent event){
        return super.onTouchEvent(getIntercambioXY(event));
    }

    private class VerticalPage implements ViewPager.PageTransformer{

        public void transformPage(View view, float position){
            if(position < -1){
                view.setAlpha(0);
            }else if(position <= 1){
                view.setAlpha(1);
                view.setTranslationX(view.getWidth() * -position);
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);
            }else{
                view.setAlpha(0);
            }
        }
    }
}
