package net.sciencestudio.peakaboo.androidui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;
import net.sciencestudio.scidraw.backend.android.AndroidBitmapSurface;

import cyclops.Coord;
import peakaboo.display.map.MapRenderData;
import peakaboo.display.map.MapRenderSettings;
import peakaboo.display.map.Mapper;

public class MapView extends View {

    private Mapper mapper;
    private ScaleGestureDetector scaleDetector;
    private float scale = 1f;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public MapView(Context context) {
        super(context);

        mapper = new Mapper();

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                System.out.println("zoom starting, scale: " + detector.getScaleFactor());
                return true;
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                System.out.println("zoom ongoing, scale: " + detector.getScaleFactor());

                ScrollView vscroll = findViewById(R.id.map_vscroll);
                HorizontalScrollView hscroll = findViewById(R.id.map_hscroll);

                float prevScale = scale;
                float scale = Math.min(10f, Math.max(1f, 1 - detector.getScaleFactor()));


                ScaleAnimation scaleAnimation = new ScaleAnimation(
                        1f / prevScale,
                        1f/scale,
                        1f / prevScale,
                        1f/scale,
                        detector.getFocusX(),
                        detector.getFocusY()
                    );
                scaleAnimation.setDuration(0);
                scaleAnimation.setFillAfter(true);
                hscroll.startAnimation(scaleAnimation);

                return true;
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Sometimes the view has size 0? I assume this happens when update() is called before the
        //view is laid out in the screen
        int width = Math.max(canvas.getWidth(), 100);
        int height = Math.max(canvas.getHeight(), 100);

        System.out.println(width);
        System.out.println(height);
        System.out.println("------------");

        MapRenderData data = AppState.mapcontroller.getMapRenderData();
        MapRenderSettings settings = AppState.mapcontroller.getRenderSettings();

        //TODO: Don't create a new bitmap every redraw, create it once and only replace it if the view size changes
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        AndroidBitmapSurface surface = new AndroidBitmapSurface(bm);
        surface.setFontSize(surface.getFontSize() * 1.2f);
        mapper.draw(data, settings, surface, new Coord<>(width, height));

        canvas.drawBitmap(bm, 0, 0, new Paint());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("Touch Event " + event);
        super.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);
        return true;
    }

}
