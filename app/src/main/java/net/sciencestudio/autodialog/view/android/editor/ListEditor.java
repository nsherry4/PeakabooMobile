package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.model.SelectionParameter;

public class ListEditor<T> extends AbstractViewEditor<T> {

    private Spinner control;
    private SelectionParameter<T> selparam;

    public ListEditor(Context context) {
        super(context);
    }

    @Override
    protected void setEnabled(boolean enabled) {
        control.setEnabled(enabled);
    }

    @Override
    public View getView() {
        return control;
    }

    @Override
    public void initialize(Parameter<T> parameter) {
        super.param = parameter;

        selparam = (SelectionParameter<T>) parameter;
        control = new Spinner(super.context);
        ArrayAdapter<T> adapter = new ArrayAdapter<>(super.context, android.R.layout.simple_spinner_dropdown_item, selparam.getPossibleValues());
        control.setAdapter(adapter);

        setEnabled(param.isEnabled());
    }

    @Override
    public T getEditorValue() {
        return (T) control.getSelectedItem();
    }

    @Override
    public void setEditorValue(T t) {
        int index = selparam.getPossibleValues().indexOf(t);
        if (index == -1) {
            return;
        }
        control.setSelection(index);
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
        return control;
    }
}
