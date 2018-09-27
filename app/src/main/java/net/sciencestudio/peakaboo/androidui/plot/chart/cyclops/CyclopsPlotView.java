package net.sciencestudio.peakaboo.androidui.plot.chart.cyclops;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import net.sciencestudio.cyclops.visualization.backend.android.CyclopsView;
import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.cyclops.visualization.backend.android.AndroidBitmapSurface;

import java.util.function.Consumer;

import cyclops.Coord;
import cyclops.visualization.Surface;
import cyclops.visualization.drawing.plot.PlotDrawing;
import peakaboo.curvefit.curve.fitting.FittingResult;
import peakaboo.display.plot.PlotData;
import peakaboo.display.plot.PlotSettings;
import peakaboo.display.plot.Plotter;

public class CyclopsPlotView extends CyclopsView {

    private Plotter plotter;
    private PlotDrawing plotDrawing;

    private Consumer<Integer> onRequestFitting = (i) -> {};
    private Consumer<FittingResult> onSelectFitting = (i) -> {};

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CyclopsPlotView(Context context, AttributeSet attributes) {
        super(context, attributes, true, false);
        plotter = new Plotter();
    }


    @Override
    protected void paint(Surface surface, Coord<Integer> size) {
        PlotData plotData = AppState.controller.getPlotData();
        PlotSettings plotSettings = AppState.controller.view().getPlotSettings();
        plotDrawing = plotter.draw(plotData, plotSettings, surface, size);
    }

    @Override
    protected boolean onSingleTap(float x, float y) {
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

        onSelectFitting.accept(bestFit);
        return true;
    }

    @Override
    protected void onLongPress(float x, float y) {
        int channel = plotter.getChannel((int)x);
        onRequestFitting.accept(channel);
    }

    public void setOnRequestFitting(Consumer<Integer> onRequestFitting) {
        this.onRequestFitting = onRequestFitting;
    }

    public void setOnSelectFitting(Consumer<FittingResult> onSelectFitting) {
        this.onSelectFitting = onSelectFitting;
    }
}
