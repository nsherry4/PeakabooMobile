package net.sciencestudio.peakaboo.androidui.plot.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import net.sciencestudio.peakaboo.androidui.R;

import peakaboo.curvefit.peak.transition.TransitionSeries;

public class FittingMarkerView extends MarkerView {

    private TextView label;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public FittingMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        label = (TextView) findViewById(R.id.plot_marker);
        //label.setVisibility(VISIBLE);



    }

    public void refreshContent(Entry e, Highlight highlight) {


        if (highlight instanceof TransitionSeriesHighlight) {
            TransitionSeries ts = ((TransitionSeriesHighlight)highlight).getTransitionSeries();
            label.setText(ts.toElementString());
        } else {
            label.setText("?");
        }


        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }


    private MPPointF mOffset;
    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }

}
