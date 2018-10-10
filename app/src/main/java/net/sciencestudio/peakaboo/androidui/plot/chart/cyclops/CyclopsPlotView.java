package net.sciencestudio.peakaboo.androidui.plot.chart.cyclops;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.sciencestudio.autodialog.view.android.support.DisplayHelper;
import net.sciencestudio.cyclops.visualization.backend.android.CyclopsView;
import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.cyclops.visualization.backend.android.AndroidBitmapSurface;
import net.sciencestudio.peakaboo.androidui.R;

import java.util.function.Consumer;

import cyclops.Coord;
import cyclops.visualization.Surface;
import cyclops.visualization.drawing.plot.PlotDrawing;
import peakaboo.curvefit.curve.fitting.FittingResult;
import peakaboo.curvefit.peak.transition.TransitionSeries;
import peakaboo.display.plot.PlotData;
import peakaboo.display.plot.PlotSettings;
import peakaboo.display.plot.Plotter;

public class CyclopsPlotView extends CyclopsView {

    private Plotter plotter;
    private PlotDrawing plotDrawing;

    private Consumer<Integer> onRequestFitting = (i) -> {};
    private Consumer<TransitionSeries> onSelectFitting = (i) -> {};

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CyclopsPlotView(Context context, AttributeSet attributes) {
        super(context, attributes, true, false);
        plotter = new Plotter();
        for (int i = 0; i < attributes.getAttributeCount(); i++) {
            System.out.println(attributes.getAttributeName(i));
        }
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
        TransitionSeries best = AppState.controller.fitting().selectTransitionSeriesAtChannel(channel);
        onSelectFitting.accept(best);
        return true;
    }

    @Override
    protected boolean onDoubleTap(float x, float y) {
        int channel = plotter.getChannel((int)x);
        TransitionSeries best = AppState.controller.fitting().selectTransitionSeriesAtChannel(channel);
        if (best == null) {
            return true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (AppState.controller.fitting().hasAnnotation(best)) {
            input.setText(AppState.controller.fitting().getAnnotation(best));
        }

        LinearLayout layout = new LinearLayout(getContext());
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginSize = DisplayHelper.dpToPixel(20, getContext());
        margin.setMargins(marginSize, marginSize, marginSize, marginSize);
        input.setLayoutParams(margin);
        layout.addView(input);
        builder.setView(layout);
        builder.setTitle("Annotation for " + best.toString());

        builder.setPositiveButton("OK", (d, e) -> {
            AppState.controller.fitting().setAnnotation(best, input.getText().toString());
        });
        builder.setNegativeButton("Cancel", (d, e) -> {});

        builder.show();

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

    public void setOnSelectFitting(Consumer<TransitionSeries> onSelectFitting) {
        this.onSelectFitting = onSelectFitting;
    }
}
