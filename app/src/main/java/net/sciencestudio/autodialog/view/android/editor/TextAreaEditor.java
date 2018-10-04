package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;

public class TextAreaEditor extends TextBoxEditor {
    public TextAreaEditor(Context context) {
        super(context);
    }

    @Override
    public boolean expandVertical() {
        return true;
    }

    @Override
    public boolean expandHorizontal() {
        return true;
    }

    @Override
    public LabelStyle getLabelStyle() {
        return LabelStyle.LABEL_HIDDEN;
    }

}
