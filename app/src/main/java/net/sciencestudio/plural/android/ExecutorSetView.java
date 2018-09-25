package net.sciencestudio.plural.android;

import android.app.ProgressDialog;
import android.content.Context;

import plural.executor.ExecutorSet;
import plural.executor.ExecutorState;
import plural.executor.PluralExecutor;

public class ExecutorSetView extends ProgressDialog {

    private ExecutorSet<?> executors;

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


    }

    private void update(PluralExecutor executor) {

        //String title = executors.getDescription() + ": " + executor.getName();
        //this.setTitle(title);


        if (executor.getState() == ExecutorState.STALLED) {
            this.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        } else {
            this.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.setMax(executor.getWorkUnits());
            this.setProgress(executor.getWorkUnitsCompleted());
        }
    }


}
