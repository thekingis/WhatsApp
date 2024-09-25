package com.example.whats_app.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.whats_app.R;

public class CustomView extends LinearLayout {

    private Path path;
    private int radius;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            path = new Path();
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ViewBorderRadius);
            int defaultRadius = 0;
            radius = styledAttrs.getDimensionPixelSize(R.styleable.ViewBorderRadius_borderRadius, defaultRadius);

            styledAttrs.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        @SuppressLint("DrawAllocation") RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        float radius = (float) this.radius;
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(path);
        super.draw(canvas);
        canvas.restore();
    }
}
