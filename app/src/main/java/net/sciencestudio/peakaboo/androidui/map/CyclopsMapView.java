package net.sciencestudio.peakaboo.androidui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import net.sciencestudio.cyclops.visualization.backend.android.CyclopsView;
import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;
import net.sciencestudio.cyclops.visualization.backend.android.AndroidBitmapSurface;

import cyclops.Coord;
import cyclops.visualization.Surface;
import peakaboo.controller.mapper.settings.MapFittingSettings;
import peakaboo.curvefit.peak.transition.TransitionSeries;
import peakaboo.display.map.MapRenderData;
import peakaboo.display.map.MapRenderSettings;
import peakaboo.display.map.Mapper;

public class CyclopsMapView extends CyclopsView {

    private Mapper mapper;
    private ScaleGestureDetector scaleDetector;
    private float scale = 1f;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CyclopsMapView(Context context, AttributeSet attributes) {
        super(context, attributes, true, true);
        mapper = new Mapper();

    }

    @Override
    protected void paint(Surface surface, Coord<Integer> size) {

        MapRenderData data = AppState.mapcontroller.getMapRenderData();
        MapRenderSettings settings = AppState.mapcontroller.getRenderSettings();
        mapper.draw(data, settings, surface, size);
    }

    void setNeedsRedraw() {
        mapper.setNeedsRedraw();
    }

    @Override
    protected boolean onSingleTap(float x, float y) {
        return true;
    }

    @Override
    protected boolean onDoubleTap(float x, float y) {
        return false;
    }

    @Override
    protected void onLongPress(float x, float y) {

    }




}
