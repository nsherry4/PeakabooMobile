package net.sciencestudio.cyclops.visualization.backend.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
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

    private float plotStartX = -1;
    private float plotStartY = -1;
    private float plotEndX = -1;
    private float plotEndY = -1;
    private boolean plotSizeInit = false;




    private boolean onX = false, onY = false;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CyclopsView(Context context, AttributeSet attributes, boolean onX, boolean onY) {
        super(context, attributes);
        this.onX = onX;
        this.onY = onY;
        this.setWillNotDraw(false);

        ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                plotWidth = (int)(plotEndX - plotStartX);
                plotHeight = (int)(plotEndY - plotStartY);

                //focus is pixel position on screen, need it in pixel position on plot
                float focusX = detector.getFocusX() - plotStartX;
                float focusY = detector.getFocusY() - plotStartY;
                float scaleX = (detector.getScaleFactor()-1f) * plotWidth;
                float scaleY = (detector.getScaleFactor()-1f) * plotHeight;

                //percentX/Y is focus point as percent of plot dimensions
                float percentX = focusX/(float)plotWidth;
                float percentY = focusY/(float)plotHeight;

                if (onX) {
                    plotStartX = Math.min(0, plotStartX - (percentX * scaleX));
                    plotEndX = Math.max(viewportWidth, plotEndX + ((1f - percentX) * scaleX));
                }
                if (onY) {
                    plotStartY = Math.min(0, plotStartY - (percentY * scaleY));
                    plotEndY = Math.max(viewportHeight, plotEndY + ((1f - percentY) * scaleY));
                }
                CyclopsView.this.invalidate();

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
                float x = rawx - plotStartX;
                x /= dpiAdjust;

                float rawy = e.getY();
                float y = rawy - plotStartY;
                y /= dpiAdjust;

                return CyclopsView.this.onSingleTap(x, y);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return CyclopsView.this.onScroll(distanceX, distanceY);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // Note: We perform a dpi-adjust here because the child will
                // need to use x, y to look up what's in that part of the drawing,
                // but it's reference drawing will be in dpi-adjusted units.
                float rawx = e.getX();
                float x = rawx - plotStartX;
                x /= dpiAdjust;

                float rawy = e.getY();
                float y = rawy - plotStartY;
                y /= dpiAdjust;

                CyclopsView.this.onLongPress(x, y);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

        gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float rawx = e.getX();
                float x = rawx - plotStartX;
                x /= dpiAdjust;

                float rawy = e.getY();
                float y = rawy - plotStartY;
                y /= dpiAdjust;

                return CyclopsView.this.onDoubleTap(x, y);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

    }

    private boolean onScroll(float distanceX, float distanceY) {

        if (onX) {
            if (plotStartX - distanceX <= 0 && plotEndX - distanceX >= viewportWidth) {
                plotStartX -= distanceX;
                plotEndX -= distanceX;
            }
        }
        if (onY) {
            if (plotStartY - distanceY <= 0 && plotEndY - distanceY >= viewportHeight) {
                plotStartY -= distanceY;
                plotEndY -= distanceY;
            }
        }

        CyclopsView.this.invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //the canvas is basically our viewport to the whole plot
        viewportWidth = canvas.getWidth();
        viewportHeight = canvas.getHeight();

        //initialize plot size if empty (eg first draw)
        if (plotSizeInit == false) {
            plotStartX = 0;
            plotStartY = 0;
            plotEndX = viewportWidth;
            plotEndY = viewportHeight;
            plotSizeInit = true;
        }

        //convenience
        plotWidth = (int)(plotEndX - plotStartX);
        plotHeight = (int)(plotEndY - plotStartY);

        //If the plot size is smaller than the viewport, reset and redo
        if (plotHeight < viewportHeight || plotWidth < viewportWidth) {
            plotStartX = 0;
            plotStartY = 0;
            plotEndX = viewportWidth;
            plotEndY = viewportHeight;
        }
        plotWidth = (int)(plotEndX - plotStartX);
        plotHeight = (int)(plotEndY - plotStartY);

        //Adjust for different screen densities
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.densityDpi;
        dpiAdjust = density / 160f;

        //create, scale, and translate a surface to draw the part of the plot we want
        AndroidBitmapSurface surface = ensureBitmap(canvas, viewportWidth, viewportHeight);
        surface.translate(plotStartX, plotStartY);
        surface.scale(dpiAdjust, dpiAdjust);
        Coord<Integer> size = new Coord<>((int)(plotWidth/dpiAdjust), (int)(plotHeight/dpiAdjust));
        paint(surface, size);
        canvas.drawBitmap(bitmap, 0, 0, new Paint());

    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {

        boolean handled = false;
        handled |= scaleDetector.onTouchEvent(me);
        handled |= gestureDetector.onTouchEvent(me);
        return handled;
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
    protected abstract boolean onDoubleTap(float x, float y);
    protected abstract void onLongPress(float x, float y);
}
