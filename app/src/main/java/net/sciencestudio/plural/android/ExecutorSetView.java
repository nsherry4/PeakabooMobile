package net.sciencestudio.plural.android;

import android.app.ProgressDialog;
import android.content.Context;

import java.util.logging.Level;

import peakaboo.common.PeakabooLog;
import plural.executor.ExecutorSet;
import plural.executor.ExecutorState;
import plural.executor.PluralExecutor;

public class ExecutorSetView extends ProgressDialog {

    private ExecutorSet<?> executors;
    private int percent = -1;
    private String title = "";

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     */
    public ExecutorSetView(Context context, ExecutorSet<?> executors) {
        super(context);
        init(executors);
    }

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     * @param theme   the resource ID of the theme against which to inflate
     *                this dialog, or {@code 0} to use the parent
     *                {@code context}'s default alert dialog theme
     */
    public ExecutorSetView(Context context, int theme, ExecutorSet<?> executors) {
        super(context, theme);
        init(executors);
    }


    private void init(ExecutorSet<?> executors) {
        this.executors = executors;

        this.setCancelable(false);
        this.setTitle(executors.getDescription());
        this.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        for (PluralExecutor executor : executors) {
            executors.addListener(() -> {
                if (executor.getState() == ExecutorState.WORKING || executor.getState() == ExecutorState.STALLED) {
                    update(executor);
                }
            });
        }

        executors.addListener(() -> {
            if (executors.isAborted() && executors.isResultSet()){
                this.hide();
            }
            else if (executors.getCompleted()){
                this.hide();
            } else {
                //NOOP
            }
        });

        this.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

    }

    private void update(PluralExecutor executor) {

        //Conditionally update title
        String newTitle = executor.getName();
        if (!title.equals(newTitle)) {
            title = newTitle;
            this.setTitle(title);
        }

        //Conditionally update progress indicator
        if (executor.getState() == ExecutorState.STALLED) {
            this.setIndeterminate(true);
            if (this.getProgress() != 0) {
                this.setProgress(0);
            }
        } else {
            int count = executor.getWorkUnitsCompleted();
            int size = executor.getWorkUnits();
            int newPercent = (int)(((float)count)/((float)size) * 100);
            if (percent != newPercent) {
                this.setMax(executor.getWorkUnits());
                this.setProgress(executor.getWorkUnitsCompleted());
                this.setIndeterminate(false);
                percent = newPercent;
            }
        }
    }


}
