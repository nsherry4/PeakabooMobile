package net.sciencestudio.plural.android;

import android.app.ProgressDialog;
import android.content.Context;

import plural.executor.ExecutorSet;
import plural.executor.ExecutorState;
import plural.executor.PluralExecutor;
import plural.streams.StreamExecutor;
import plural.streams.StreamExecutorSet;

public class StreamExecutorView extends ProgressDialog {

    private StreamExecutor<?> executors;

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     */
    public StreamExecutorView(Context context, StreamExecutor<?> executor) {
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
    public StreamExecutorView(Context context, int theme, StreamExecutor<?> executors) {
        super(context, theme);
        init(executors);
    }


    private void init(StreamExecutor<?> executors) {
        this.executors = executors;

        this.setCancelable(true);
        this.setTitle(executors.getName());

        executors.addListener((StreamExecutor.Event event) -> {
            switch (event) {
                case COMPLETED:
                    this.hide();;
                    return;
                case ABORTED:
                    this.hide();
                    return;
                case PROGRESS:
                    update();
                    return;
            }
        });

    }

    private void update() {

        if (executors.getSize() <= 0) {
            this.setIndeterminate(true);
        } else {
            this.setIndeterminate(false);
            this.setProgress(executors.getCount());
            this.setMax(executors.getSize());
        }
    }


}
