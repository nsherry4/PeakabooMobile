package net.sciencestudio.cyclops.visualization.backend.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.logging.Level;

import cyclops.Coord;
import cyclops.visualization.Surface;
import peakaboo.common.PeakabooLog;

public abstract class CyclopsView extends View {

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private Bitmap bitmap;

    private int plotWidth = 0;
    private int plotHeight = 0;
    private int viewportWidth = 0;
    private int viewportHeight = 0;
    public float dpiAdjust = 1;
    private float scaleFactor = 1f;
    private float scrollPercentX = 0f;
    private float scrollPercentY = 0f;

    private boolean onX = false, onY = false;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CyclopsView(Context context, boolean onX, boolean onY) {
        super(context);
        this.onX = onX;
        this.onY = onY;
        this.setWillNotDraw(false);

        ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                float newScale = scaleFactor *detector.getScaleFactor();
                if (newScale >= 1f && newScale <= 5f) {
                    scaleFactor = scaleFactor * detector.getScaleFactor();
                    CyclopsView.this.invalidate();
                }
                return true;
            }

        };
        scaleDetector = new ScaleGestureDetector(context, scaleListener);


        gestureDetector = new GestureDetector(super.getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {


            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                float rawx = e.getX();
                float offsetx = scrollOffsetX();
                float x = rawx + offsetx;
                x /= dpiAdjust;

                float rawy = e.getY();
                float offsety = scrollOffsetY();
                float y = rawy + offsety;
                y /= dpiAdjust;

                return CyclopsView.this.onSingleTap(x, y);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (plotWidth <= 0 || plotHeight <= 0) {
                    return true;
                }

                float extraPercentX = (distanceX / (float)plotWidth);
                scrollPercentX += extraPercentX;
                scrollPercentX = Math.max(0, Math.min(1f, scrollPercentX));

                float extraPercentY = (distanceY / (float)plotHeight);
                scrollPercentY += extraPercentY;
                scrollPercentY = Math.max(0, Math.min(1f, scrollPercentY));

                CyclopsView.this.invalidate();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                float rawx = e.getX();
                float offsetx = scrollOffsetX();
                float x = rawx + offsetx;
                x /= dpiAdjust;

                float rawy = e.getY();
                float offsety = scrollOffsetY();
                float y = rawy + offsety;
                y /= dpiAdjust;

                CyclopsView.this.onLongPress(x, y);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        plotWidth = (int) (canvas.getWidth() * scaleFactor);
        viewportWidth = canvas.getWidth();
        plotHeight = (int) (canvas.getHeight() * scaleFactor);
        viewportHeight = canvas.getHeight();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.densityDpi;
        dpiAdjust = density / 213f;




        try {

            //get base dimensions and positions for drawing
            int bmWidth = onX ? plotWidth : viewportWidth;
            int bmHeight = onY ? plotHeight : viewportHeight;
            float offsetX = onX ? scrollOffsetX() : 0f;
            float offsetY = onY ? scrollOffsetY() : 0f;

            //We can't generate a bitmap larger than the max allowed size. Rather than doing
            //that, we sacrifice resolution/clarity
            float maxWidth = canvas.getMaximumBitmapWidth();
            float maxHeight = canvas.getMaximumBitmapHeight();
            float adjust = dpiAdjust;
            float tooLargeRatio = -1f;
            if (maxWidth < bmWidth || maxHeight < bmHeight) {
                System.out.println(bmWidth + ", " + bmHeight);
                tooLargeRatio = Math.min(maxWidth / (float)bmWidth, maxHeight / (float)bmHeight);
                System.out.println(tooLargeRatio);
                bmHeight = (int) Math.min(maxHeight, bmHeight * tooLargeRatio);
                bmWidth = (int) Math.min(maxWidth, bmWidth * tooLargeRatio);
                adjust *= tooLargeRatio;
                System.out.println(bmWidth + ", " + bmHeight);
                System.out.println("----------------------------");
                canvas.scale(1f/tooLargeRatio, 1f/tooLargeRatio);
                offsetX *= tooLargeRatio;
                offsetY *= tooLargeRatio;
            }


            AndroidBitmapSurface surface = ensureBitmap(canvas, bmWidth, bmHeight);
            surface.scale(adjust, adjust);
            Coord<Integer> size = new Coord<>((int) (bmWidth / adjust), (int) (bmHeight / adjust));
            paint(surface, size);

            canvas.drawBitmap(bitmap, -offsetX, -offsetY, new Paint());

        } catch (RuntimeException e) {
            PeakabooLog.get().log(Level.SEVERE, "Failed to draw", e);
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {

        boolean handled = false;
        handled |= scaleDetector.onTouchEvent(me);
        handled |= gestureDetector.onTouchEvent(me);
        return handled;
    }

    protected float scrollOffsetX() {
        return (plotWidth-viewportWidth)*scrollPercentX;
    }

    protected float scrollOffsetY() {
        return (plotHeight-viewportHeight)*scrollPercentY;
    }

    public int getPlotWidth() {
        return plotWidth;
    }

    public int getPlotHeight() {
        return plotHeight;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }


    /**
     * Makes sure that the bitmap drawing surface is created, sized, and blanked
     */
    private AndroidBitmapSurface ensureBitmap(Canvas canvas, float width, float height) {

        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            createBitmap(width, height);
        }
        AndroidBitmapSurface surface = new AndroidBitmapSurface(bitmap);

        //blank
        surface.setSource(255, 255, 255, 255);
        surface.rectAt(0, 0, bitmap.getWidth(), bitmap.getHeight());
        surface.fill();

        return surface;

    }

    private void createBitmap(float width, float height) {
        bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
    }

    protected abstract void paint(Surface s, Coord<Integer> size);
    protected abstract boolean onSingleTap(float x, float y);
    protected abstract void onLongPress(float x, float y);
}
