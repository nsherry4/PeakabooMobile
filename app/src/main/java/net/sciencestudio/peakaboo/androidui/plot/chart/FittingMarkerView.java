package net.sciencestudio.peakaboo.androidui.plot.chart;

import android.content.Context;

import com.github.mikephil.charting.components.MarkerView;

public class FittingMarkerView extends MarkerView {

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public FittingMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
    }
}
