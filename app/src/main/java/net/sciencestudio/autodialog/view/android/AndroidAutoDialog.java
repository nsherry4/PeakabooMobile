package net.sciencestudio.autodialog.view.android;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.widget.ScrollView;

import net.sciencestudio.autodialog.model.Group;
import net.sciencestudio.autodialog.model.Value;
import net.sciencestudio.autodialog.view.android.layout.AndroidLayout;
import net.sciencestudio.autodialog.view.android.layout.AndroidLayoutFactory;
import net.sciencestudio.peakaboo.androidui.R;

public class AndroidAutoDialog {

    public static AlertDialog create(Group group, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(group.getName());
        AndroidLayout layout = AndroidLayoutFactory.forGroup(group, context);
        ScrollView scroller = new ScrollView(context);
        scroller.setFillViewport(true);
        scroller.addView(layout.getView());
        builder.setView(scroller);
        builder.setPositiveButton(R.string.close, (button, something) -> {
            //It will close on it's own
        });
        return builder.create();
    }

}
