package net.sciencestudio.peakaboo.androidui.plot.chart.cyclops;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.scidraw.backend.android.AndroidBitmapSurface;

import java.util.logging.Level;

import cyclops.Coord;
import cyclops.visualization.drawing.plot.PlotDrawing;
import peakaboo.common.PeakabooLog;
import peakaboo.curvefit.curve.fitting.FittingResult;
import peakaboo.display.plot.PlotData;
import peakaboo.display.plot.PlotSettings;
import peakaboo.display.plot.Plotter;

public abstract class CyclopsPlotView extends View {

    private Plotter plotter;
    private PlotDrawing plotDrawing;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private int plotWidth = 0;
    private int viewportWidth = 0;
    private float scaleFactor = 1f;
    private float scaleFocusX = 0f;
    private float scrollPercentX = 0f;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CyclopsPlotView(Context context) {
        super(context);
        this.setWillNotDraw(false);

        plotter = new Plotter();


        ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                float newScale = scaleFactor *detector.getScaleFactor();
                if (newScale >= 1f && newScale <= 10f) {
                    scaleFocusX = detector.getFocusX();
                    scaleFactor = scaleFactor * detector.getScaleFactor();
                    PeakabooLog.get().log(Level.INFO, "Scale Event: " + detector.getScaleFactor() + ", " + scaleFactor);
                    CyclopsPlotView.this.invalidate();
                }
                return true;
            }

        };

        scaleDetector = new ScaleGestureDetector(context, scaleListener);



        gestureDetector = new GestureDetector(super.getContext(), new OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {


            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                PeakabooLog.get().log(Level.INFO, "Single Tap Up Event");

                float rawx = e.getX();
                float offset = scrollOffset();
                float x = rawx + offset;
                int channel = plotter.getChannel((int)x);

                float bestValue = 1f;
                FittingResult bestFit = null;

                if (AppState.controller.fitting().getFittingSelectionResults() == null) {
                    return true;
                }

                for (FittingResult fit : AppState.controller.fitting().getFittingSelectionResults()) {
                    float value = fit.getFit().get(channel);
                    if (value > bestValue) {
                        bestValue = value;
                        bestFit = fit;
                    }
                }

                onSelectFitting(bestFit);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (plotWidth <= 0) {
                    return true;
                }
                float extraPercent = (distanceX / (float)plotWidth);
                PeakabooLog.get().log(Level.INFO, "onScroll " + distanceX + ", " +  plotWidth + ", " + extraPercent);
                scrollPercentX += extraPercent;
                PeakabooLog.get().log(Level.INFO, "onScroll " + scrollPercentX);
                scrollPercentX = Math.max(0, Math.min(1f, scrollPercentX));
                PeakabooLog.get().log(Level.INFO, "onScroll " + scrollPercentX);
                CyclopsPlotView.this.invalidate();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

                float rawx = e.getX();
                float offset = scrollOffset();
                float x = rawx + offset;

                int channel = plotter.getChannel((int)x);
                onRequestFitting(channel);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        widthMeasureSpec = widthMeasureSpec*2;
//
//        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
//    }

    @Override
    protected void onDraw(Canvas canvas) {

        PeakabooLog.get().log(Level.INFO, "onDraw");


        int width = Math.max(canvas.getWidth(), 100);
        int height = Math.max(canvas.getHeight(), 100);

        System.out.println("width=" + width);
        System.out.println("height=" + height);

        plotWidth = (int) (width * scaleFactor);
        viewportWidth = width;


        PlotData plotData = AppState.controller.getPlotData();
        PlotSettings plotSettings = AppState.controller.view().getPlotSettings();

        Bitmap bm = Bitmap.createBitmap(plotWidth, height, Bitmap.Config.ARGB_8888);
        AndroidBitmapSurface surface = new AndroidBitmapSurface(bm);
        plotDrawing = plotter.draw(plotData, plotSettings, surface, new Coord<>(plotWidth, height));

        System.out.println(plotWidth*scrollPercentX);

        canvas.drawBitmap(bm, -scrollOffset(), 0, new Paint());

    }

    private float scrollOffset() {
        return (plotWidth-viewportWidth)*scrollPercentX;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {

        boolean handled = false;
        handled |= scaleDetector.onTouchEvent(me);
        handled |= gestureDetector.onTouchEvent(me);
        return handled;
    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        super.dispatchTouchEvent(event);
//        scaleDetector.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
//        return true;
//    }

    public void update() {
        this.invalidate();
    }

    public void resetView() {
        this.invalidate();
    }

    protected abstract void onRequestFitting(int channel);
    protected abstract void onSelectFitting(FittingResult fitting);


}
