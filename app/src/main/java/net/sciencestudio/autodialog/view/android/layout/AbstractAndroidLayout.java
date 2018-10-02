package net.sciencestudio.autodialog.view.android.layout;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.sciencestudio.autodialog.model.Group;
import net.sciencestudio.autodialog.model.Value;
import net.sciencestudio.autodialog.view.editors.Editor;

public abstract class AbstractAndroidLayout implements AndroidLayout {

    protected Group group;
    protected Context context;

    public AbstractAndroidLayout(Context context) {
        this.context = context;
    }

    @Override
    public void initialize(Group group) {
        this.group = group;
        layout();
    }

    @Override
    public String getTitle() {
        return group.getName();
    }

    @Override
    public boolean expandVertical() {
        return false;
    }

    @Override
    public boolean expandHorizontal() {
        return false;
    }

    @Override
    public Editor.LabelStyle getLabelStyle() {
        return Editor.LabelStyle.LABEL_HIDDEN;
    }

    @Override
    public Value<?> getValue() {
        return group;
    }
}
