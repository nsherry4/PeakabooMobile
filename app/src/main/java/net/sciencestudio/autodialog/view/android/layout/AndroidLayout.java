package net.sciencestudio.autodialog.view.android.layout;

import net.sciencestudio.autodialog.model.Group;
import net.sciencestudio.autodialog.view.android.AndroidView;
import net.sciencestudio.autodialog.view.layouts.Layout;

public interface AndroidLayout extends Layout, AndroidView {

    void initialize(Group group);
    void layout();

}
