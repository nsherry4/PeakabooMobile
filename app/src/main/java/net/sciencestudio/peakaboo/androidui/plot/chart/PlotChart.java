package net.sciencestudio.peakaboo.androidui.plot.chart;

import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;


import net.sciencestudio.peakaboo.androidui.R;
import net.sciencestudio.peakaboo.androidui.plot.LineLabel;
import net.sciencestudio.peakaboo.androidui.plot.PlotActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.plotter.view.PlotData;
import peakaboo.controller.plotter.view.PlotSettings;
import peakaboo.curvefit.curve.fitting.FittingResult;
import peakaboo.curvefit.curve.fitting.FittingResultSet;
import peakaboo.curvefit.peak.transition.TransitionSeries;
import scitypes.ISpectrum;
import scitypes.ReadOnlySpectrum;
import scitypes.SigDigits;

/**
 * Manages the main plot and provides high-level access to it
 */
public abstract class PlotChart {

    private PlotActivity main;
    private PlotController controller;

    private LineChart chart;
    private Map<TransitionSeries, LineDataSet> fittingLines, proposedLines;
    private LineDataSet filteredPlot;
    private LineData datasets;

    private boolean logged = true;

    PlotChart(PlotActivity main, PlotController controller) {
        this.main = main;
        this.controller = controller;
        createPlot();
    }

    private List<Entry> spectrumToEntries(ReadOnlySpectrum s) {
        List<Entry> entries = new ArrayList<>();
        if (logged) {
            for (int i = 0; i < s.size(); i++) {
                entries.add(new Entry(i, log(s.get(i))));
            }
        } else {
            for (int i = 0; i < s.size(); i++) {
                entries.add(new Entry(i, s.get(i)));
            }
        }
        return entries;
    }

    //convert spectral data to chart-native format
    private LineDataSet spectrumToLineDataSet(ReadOnlySpectrum s, String label) {
        LineDataSet dataset = new LineDataSet(spectrumToEntries(s), label);
        dataset.setDrawFilled(true);
        dataset.setDrawCircles(false);
        dataset.setDrawValues(true);
        dataset.setLineWidth(1f);
        return dataset;
    }

    private void attachLineLabel(LineDataSet dataset, TransitionSeries ts) {
        float energy = ts.getStrongestTransition().energyValue;
        int channel = controller.fitting().getEnergyCalibration().channelFromEnergy(energy);
        IValueFormatter formatter = new LineLabel(ts.toString(), channel);
        dataset.setValueFormatter(formatter);


    }

    private void populateLineDataSet(LineDataSet ds, ReadOnlySpectrum s) {
        ds.setValues(spectrumToEntries(s));
    }

    public void update() {
        System.out.println("UPDATE CALLED");

        //If the chart is empty
        if (!controller.data().hasDataSet()) {
            updateEmpty();
            return;
        }



        //Fetch data from controller
        PlotData plotData = controller.getPlotData();
        PlotSettings plotSettings = controller.view().getPlotSettings();

        //Set this value before calling updateFits so that it populates the chart with the right data
        logged = plotSettings.logTransform;



        //Update Signal
        populateLineDataSet(filteredPlot, plotData.filtered);


        //Update/Add/Remove Fittings
        updateFits(fittingLines, plotData.selectionResults, 0xFF000000, 0xFF000000);
        updateFits(proposedLines, plotData.proposedResults, 0xFFA40000, 0xFFA40000);

        //Chart scale
        float maxIntensity = Math.max(plotData.dataset.getAnalysis().maximumIntensity(), plotData.filtered.max());
        if (logged) {
            maxIntensity = (float) Math.log1p(maxIntensity);
        }
        chart.getAxisLeft().setAxisMaximum(maxIntensity);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisRight().setAxisMaximum(maxIntensity);
        chart.getAxisRight().setAxisMinimum(0);

        chart.invalidate();

    }

    private void updateEmpty() {

        LineDataSet ds;

        //remove any fitting lines
        for (TransitionSeries ts : new ArrayList<>(fittingLines.keySet())) {
            ds = fittingLines.get(ts);
            datasets.removeDataSet(ds);
        }
        fittingLines.clear();

        //remove any proposed fitting lines
        for (TransitionSeries ts : new ArrayList<>(proposedLines.keySet())) {
            ds = proposedLines.get(ts);
            datasets.removeDataSet(ds);
        }
        proposedLines.clear();

        populateLineDataSet(filteredPlot, new ISpectrum(2048));

        chart.invalidate();
    }

    private void updateFits(Map<TransitionSeries, LineDataSet> lines, FittingResultSet results, int stroke, int fill) {

        LineDataSet ds;

        //Update/Add Fittings
        for (FittingResult fit : results.getFits()) {
            TransitionSeries ts = fit.getTransitionSeries();
            if (!lines.containsKey(ts)) {
                //create new dataset
                ds = spectrumToLineDataSet(fit.getFit(), ts.toString());
                lines.put(ts, ds);
                datasets.addDataSet(ds);
            }
            ds = lines.get(ts);
            populateLineDataSet(ds, fit.getFit());
            attachLineLabel(ds, ts);

            ds.setColor(stroke);
            ds.setFillColor(fill);
        }

        //Remove Fittings
        List<TransitionSeries> fitted = results.getFits().stream()
                .map(f -> f.getTransitionSeries())
                .collect(Collectors.toList());
        for (TransitionSeries ts : new ArrayList<>(lines.keySet())) {
            if (!fitted.contains(ts)) {
                LineDataSet toRemove = lines.get(ts);
                datasets.removeDataSet(toRemove);
                lines.remove(ts);
            }
        }

    }

    private void createPlot() {
        chart = main.findViewById(R.id.chart);
        datasets = new LineData();
        fittingLines = new HashMap<>();
        proposedLines = new HashMap<>();

        //axis formatter to un-log the axis values
        chart.getAxisLeft().setValueFormatter( (value, axis) -> {
            value = unlog(value);
            return SigDigits.roundFloatTo(value, 0);
        });

        //axis formatter to un-log the axis values
        chart.getAxisRight().setValueFormatter( (value, axis) -> {
            value = unlog(value);
            return SigDigits.roundFloatTo(value, 0);
        });

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);

        //zooming & panning
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(false); //TODO: should this be false?
        chart.setDoubleTapToZoomEnabled(true);
        chart.setDragDecelerationEnabled(true);

        filteredPlot = new LineDataSet(spectrumToEntries(new ISpectrum(2048)), "");
        filteredPlot.setDrawFilled(true);
        filteredPlot.setDrawCircles(false);
        filteredPlot.setLineWidth(0f);
        filteredPlot.setDrawValues(false);
        filteredPlot.setColor(0x388E3C, 0xFF);
        filteredPlot.setFillColor(0xFF388E3C);
        filteredPlot.setFillDrawable(ContextCompat.getDrawable(main, R.drawable.plot_green));
        datasets.addDataSet(filteredPlot);



        //Add input hooks

        chart.setOnChartGestureListener(new OnChartGestureListener() {

            private boolean moving = false;

            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                moving = false;
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

                if (moving) { return; }
                System.out.println("Action ID = " + me.getActionMasked());


                Entry e = chart.getEntryByTouchPoint(me.getX(), me.getY());
                onLongPress((int)e.getX());
                System.out.println("*******************");
                System.out.println("X = " + me.getX());
                System.out.println("rawX = " + me.getRawX());
                System.out.println("axisValue-X = " + me.getAxisValue(MotionEvent.AXIS_X));
                System.out.println("plotEntryForX = " + filteredPlot.getEntryForXValue(me.getX(), me.getY()));
                System.out.println();
                System.out.println("*******************");
                //System.out.println();
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                moving = true;
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                moving = true;
            }
        });

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                System.out.println("Something Selected");
                //onLongPress((int)e.getX());
            }

            @Override
            public void onNothingSelected() {
                System.out.println("Nothing Selected");
            }
        });

        chart.setData(datasets);
        chart.invalidate();
    }



    private float log(float value) {
        return (float) Math.log1p(value);
    }

    private float unlog(float value) {
        return (float) Math.expm1(value);
    }


    protected abstract void onLongPress(int channel);

}
