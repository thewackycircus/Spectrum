package com.example.test;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ColourView extends View {

    int selectedColour;
    boolean isSelected = false;

    // CONSTRUCTORS
    public ColourView(Context context) {
        super(context);
    }

    public ColourView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColourView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            return true;
        }

        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();

        if(!isSelected) {
            Log.d("SELECTCOLOUR", "hello world");
            GradientDrawable border = new GradientDrawable();
            border.setColor(selectedColour);
            border.setStroke(Utils.pxFromDp(
                    getContext(),
                    4
            ), Color.YELLOW);
            this.setBackground(border);
            isSelected = true;
            return true;
        }
        else {
            this.setBackgroundColor(selectedColour);
            isSelected = false;
        }

        return false;
    }

    // SETTERS
    public void setSelectedColour(int colour) {
        selectedColour = colour;
        setBackgroundColor(colour);
    }

    // GETTERS
    public int getSelectedColour() {
        return selectedColour;
    }
}
