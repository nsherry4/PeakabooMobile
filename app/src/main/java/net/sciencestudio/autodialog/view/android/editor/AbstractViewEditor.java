package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;

import net.sciencestudio.autodialog.model.Value;

import eventful.EventfulType;

public abstract class AbstractViewEditor<T> implements AndroidEditor<T> {

    protected Value<T> param;
    protected Context context;

    private EventfulType<T> editorValueHook = new EventfulType<>();

    public AbstractViewEditor(Context context) {
        this.context = context;
    }

    @Override
    public EventfulType<T> getEditorValueHook() {
        return editorValueHook;
    }

    @Override
    public Value<T> getValue() {
        return param;
    }

    @Override
    public String getTitle() {
        return param.getName();
    }

    @Override
    public void setFromParameter() {

        boolean equiv = false;
        if (param.getValue() == null && getEditorValue() == null) {
            equiv = true;
        } else if (param.getValue() == null) {
            equiv = false;
        } else if (param.getValue().equals(getEditorValue())) {
            equiv = true;
        }

        if (! equiv) {
            setEditorValue(param.getValue());
        }

        setEnabled(param.isEnabled());

    }

    protected void onComponentChanged() {
        getEditorValueHook().updateListeners(getEditorValue());
        if (!param.setValue(getEditorValue())) {
            setFromParameter();
        }
    }

    protected abstract void setEnabled(boolean enabled);

}
