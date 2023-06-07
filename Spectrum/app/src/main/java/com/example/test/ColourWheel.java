package com.example.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.Random;

public class ColourWheel extends AppCompatImageView {

    // position of user interaction
    private float x;
    private float y;

    // subject view ref
    private View subject;

    // selected colour
    private int colour;

    // CONSTRUCTORS
    public ColourWheel(Context context) {
        super(context);
    }

    public ColourWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColourWheel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // SETTERS
    public void setSubject(View subject) {
        this.subject = subject;
    }

    // GETTERS
    public int getColour() {
        return colour;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            x = event.getX();
            y = event.getY();
            performClick();
            return true;
        }

        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        SelectColour();
        return true;
    }

    private void SelectColour() {

        // retrieve bitmap of drawing cache of the palette image
        Bitmap bitmap = this.getDrawingCache();
        // if user interaction is within the bounds of the bitmap
        if (x >= 0 && y >= 0 && x < bitmap.getWidth() && y < bitmap.getHeight()) {
            // retrieve the pixel that is in the position of the user's interaction
            int pixel = bitmap.getPixel((int)x, (int)y);
            // Getting RGB and Hex value of pixel
            // Color.color returns the value of that color in the given value
            int r = Color.red(pixel);
            int g = Color.green(pixel);
            int b = Color.blue(pixel);
            // set selected colour
            colour = Color.rgb(r, g, b);
            // set subject view to selected colour
            subject.setBackgroundColor(colour);
        }
    }
}
