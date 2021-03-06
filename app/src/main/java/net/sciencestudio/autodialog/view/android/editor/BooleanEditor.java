package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;

import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.model.Value;
import net.sciencestudio.autodialog.view.editors.Editor;

import eventful.EventfulType;

public class BooleanEditor extends AbstractViewEditor<Boolean> {

    private Switch component;

    public BooleanEditor(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        return component;
    }

    @Override
    public void initialize(Parameter<Boolean> parameter) {
        super.param = parameter;

        component = new Switch(super.context);
        component.setOnCheckedChangeListener((button, checked) -> {
            onComponentChanged();
        });

        setEnabled(param.isEnabled());
    }

    @Override
    public Boolean getEditorValue() {
        return component.isChecked();
    }

    @Override
    public void setEditorValue(Boolean selected) {
        component.setSelected(selected);
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
    public LabelStyle getLabelStyle() {
        return LabelStyle.LABEL_ON_SIDE;
    }

    @Override
    public Object getComponent() {
        return component;
    }

    @Override
    protected void setEnabled(boolean enabled) {
        component.setEnabled(enabled);
    }
}
