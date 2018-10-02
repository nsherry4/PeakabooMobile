package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;
import android.view.View;

import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.view.android.widget.SpinBox;


public class FloatEditor extends AbstractViewEditor<Float> {

    private SpinBox component;

    public FloatEditor(Context context) {
        super(context);
    }

    @Override
    protected void setEnabled(boolean enabled) {
        component.setEnabled(enabled);
    }

    @Override
    public View getView() {
        return component;
    }

    @Override
    public void initialize(Parameter<Float> parameter) {
        super.param = parameter;
        component = new SpinBox(super.context);
        component.setStep(0.01f);
        component.setValue(parameter.getValue());

        component.setOnValueChangeListener(v -> onComponentChanged());

    }

    @Override
    public Float getEditorValue() {
        return component.getValue();
    }

    @Override
    public void setEditorValue(Float value) {
        component.setValue(value);
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
}
