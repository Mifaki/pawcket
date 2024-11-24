package com.mobile.pawcket.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class CircleExpandView extends View {
    private float radius = 0f;
    private float centerX, centerY;
    private Paint paint;
    private Path clipPath;

    public CircleExpandView(Context context) {
        super(context);
        init();
    }

    public CircleExpandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        clipPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        clipPath.reset();
        clipPath.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CCW);
        canvas.clipPath(clipPath);
        canvas.drawColor(Color.WHITE);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }
}