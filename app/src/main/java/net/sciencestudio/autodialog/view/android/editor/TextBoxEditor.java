package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.view.editors.Editor;

public class TextBoxEditor extends AbstractViewEditor<String> {

    private EditText control;

    public TextBoxEditor(Context context) {
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
    public void initialize(Parameter<String> parameter) {
        super.param = parameter;
        control = new EditText(super.context);

        setEnabled(param.isEnabled());
    }

    @Override
    public String getEditorValue() {
        return control.getText().toString();
    }

    @Override
    public void setEditorValue(String s) {
        control.setText(s, TextView.BufferType.EDITABLE);
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
