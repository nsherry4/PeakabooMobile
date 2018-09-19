package net.sciencestudio.peakaboo.androidui.plot.chart;

import com.github.mikephil.charting.highlight.Highlight;

import peakaboo.curvefit.peak.transition.TransitionSeries;

public class TransitionSeriesHighlight extends Highlight {

    private TransitionSeries ts;

    public TransitionSeriesHighlight(float x, float y, int dataSetIndex, TransitionSeries ts) {
        super(x, y, dataSetIndex);
        this.ts = ts;
    }

    public TransitionSeries getTransitionSeries() {
        return ts;
    }
}
