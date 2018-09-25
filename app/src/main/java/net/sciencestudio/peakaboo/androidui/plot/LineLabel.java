package net.sciencestudio.peakaboo.androidui.plot;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import peakaboo.curvefit.peak.transition.TransitionSeries;

public class LineLabel implements IValueFormatter {

    private TransitionSeries ts;
    private int channel;
    private String label;

    public LineLabel(String label, int channel) {
//        this.ts = ts;
//        Transition t = ts.getStrongestTransition();
//        channel = calibration.channelFromEnergy(t.energyValue);
        this.label = label;
        this.channel = channel;
    }

    /**
     * Called when a value (from labels inside the chart) is formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     *
     * @param value           the value to be formatted
     * @param entry           the entry the value belongs to - in e.g. BarChart, this is of class BarEntry
     * @param dataSetIndex    the index of the DataSet the entry in focus belongs to
     * @param viewPortHandler provides information about the current chart state (scale, translation, ...)
     * @return the formatted label ready for being drawn
     */
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

        if (entry.getX() == channel) {
            System.out.println(label);
            return label;
        }
        return "";
    }
}
