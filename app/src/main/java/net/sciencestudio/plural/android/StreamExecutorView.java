package net.sciencestudio.plural.android;

import android.app.ProgressDialog;
import android.content.Context;

import plural.streams.StreamExecutor;

public class StreamExecutorView extends ProgressDialog {

    private StreamExecutor<?> executor;

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     */
    public StreamExecutorView(Context context, StreamExecutor<?> executor) {
        super(context);
        init(executor);
    }

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     * @param theme   the resource ID of the theme against which to inflate
     *                this dialog, or {@code 0} to use the parent
     *                {@code context}'s default alert dialog theme
     */
    public StreamExecutorView(Context context, int theme, StreamExecutor<?> executor) {
        super(context, theme);
        init(executor);
    }


    private void init(StreamExecutor<?> executor) {
        this.executor = executor;

        this.setCancelable(false);
        this.setTitle(executor.getName());

        executor.addListener((StreamExecutor.Event event) -> {
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

        if (executor.getSize() <= 0) {
            this.setIndeterminate(true);
        } else {
            this.setIndeterminate(false);
            this.setProgress(executor.getCount());
            this.setMax(executor.getSize());
        }
    }


}
