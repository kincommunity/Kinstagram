package kin.com.kinstagram.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import kin.com.kinstagram.R;


/**
 * Created by kyungsoohong on 11/15/17.
 */

public class ArcImageView extends View
{
    private float _arcStartDegree;
    private float _sweepAngle;
    private int _colorResId;
    private int _strokeWidthPixel;
    private Context _context;

    public ArcImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;
        _colorResId = context.getResources().getColor(R.color.camera_blue);
        float dipscaling = context.getResources().getDisplayMetrics().density;
        _strokeWidthPixel = (int) (5 * dipscaling);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Paint p = new Paint();
        RectF rectF = new RectF(0, 0, getWidth(), getHeight());

        rectF.inset(_strokeWidthPixel / 2, _strokeWidthPixel / 2);
        p.setColor(_colorResId);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(_strokeWidthPixel);
        canvas.drawArc(rectF, _arcStartDegree, _sweepAngle, false, p);

        super.onDraw(canvas);
    }

    public void setArcStartDegree(float arcStartDegree)
    {
        _arcStartDegree = arcStartDegree;
    }

    public void setSweepAngle(float arcDegree)
    {
        _sweepAngle = arcDegree;
        invalidate();
    }

    public void setColorResId(int resId)
    {
        _colorResId = _context.getResources().getColor(resId);
    }

    public void setStrokeWidthDp(int strokeWidthDp)
    {
        float dipscaling = _context.getResources().getDisplayMetrics().density;
        _strokeWidthPixel = (int) (5 * dipscaling);
    }

    public void setStrokeWidthPixel(int strokeWidthPixel)
    {
        _strokeWidthPixel = strokeWidthPixel;
    }
}