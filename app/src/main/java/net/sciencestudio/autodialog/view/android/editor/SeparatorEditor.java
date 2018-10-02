package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;
import android.view.View;

import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.view.editors.Editor;

public class SeparatorEditor extends AbstractViewEditor<Object> {

    private View sep;

    public SeparatorEditor(Context context) {
        super(context);
    }

    @Override
    protected void setEnabled(boolean enabled) {

    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public void initialize(Parameter<Object> parameter) {
        super.param = parameter;

        sep = new View(super.context);
        sep.setBackgroundColor(0x00000000);
        sep.setMinimumWidth(1);
    }

    @Override
    public Object getEditorValue() {
        return null;
    }

    @Override
    public void setEditorValue(Object o) {

    }

    @Override
    public boolean expandVertical() {
        return false;
    }

    @Override
    public boolean expandHorizontal() {
        return true;
    }

    @Override
    public LabelStyle getLabelStyle() {
        return LabelStyle.LABEL_HIDDEN;
    }

    @Override
    public Object getComponent() {
        return sep;
    }
}
