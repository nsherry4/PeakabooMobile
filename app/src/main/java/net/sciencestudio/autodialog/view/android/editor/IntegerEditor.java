package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.view.android.widget.SpinBox;

import java.text.DecimalFormat;

public class IntegerEditor extends AbstractViewEditor<Integer> {

    private SpinBox component;

    public IntegerEditor(Context context) {
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
    public void initialize(Parameter<Integer> parameter) {
        super.param = parameter;

        component = new SpinBox(super.context);
        component.setFormat(new DecimalFormat("0"));
        component.setStep(1);
        component.setValue(parameter.getValue());


        component.setOnValueChangeListener(v -> onComponentChanged());
        setEnabled(param.isEnabled());
    }

    @Override
    public Integer getEditorValue() {
        return (int)component.getValue();
    }

    @Override
    public void setEditorValue(Integer value) {
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
