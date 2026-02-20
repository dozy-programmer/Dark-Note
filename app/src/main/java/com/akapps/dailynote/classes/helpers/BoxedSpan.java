package com.akapps.dailynote.classes.helpers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BoxedSpan extends ReplacementSpan {

    private final int strokeColor;
    private final int strokeWidth;
    private final float radius;
    private final float padding;

    public BoxedSpan(int strokeColor, int strokeWidth, float radius, float padding) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.radius = radius;
        this.padding = padding;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return (int) (paint.measureText(text, start, end) + 2 * padding);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {

        float width = paint.measureText(text, start, end);
        RectF rect = new RectF(x, top, x + width + 2 * padding, bottom);

        // Draw rectangle outline
        Paint rectPaint = new Paint(paint);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setColor(strokeColor);
        rectPaint.setStrokeWidth(strokeWidth);

        canvas.drawRoundRect(rect, radius, radius, rectPaint);

        // Draw the text
        canvas.drawText(text, start, end, x + padding, y, paint);
    }
}
