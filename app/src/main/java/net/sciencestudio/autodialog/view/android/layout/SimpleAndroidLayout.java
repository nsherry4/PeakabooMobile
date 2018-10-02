package net.sciencestudio.autodialog.view.android.layout;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.sciencestudio.autodialog.model.Group;
import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.model.Value;
import net.sciencestudio.autodialog.view.android.AndroidView;
import net.sciencestudio.autodialog.view.android.editor.AndroidEditorFactory;
import net.sciencestudio.autodialog.view.android.support.DisplayHelper;

public class SimpleAndroidLayout extends AbstractAndroidLayout {

    private TableLayout table;

    public SimpleAndroidLayout(Context context) {
        super(context);
    }

    @Override
    public void layout() {
        if (table == null) {
            table = new TableLayout(super.context);
        }
        table.removeAllViews();

        for (Value<?> param : group.getValue()) {
            //TODO: Honour requested label positions
            TableRow row = new TableRow(super.context);

            TextView label = new TextView(super.context);
            label.setText(param.getName());
            AndroidView editor = fromValue(param);
            View view = editor.getView();

            if (view == null) {
                System.out.println("NULL View for " + param.getName());
                continue;
            }

            System.out.println(editor.getLabelStyle());

            TableRow.LayoutParams params;
            switch (editor.getLabelStyle()) {

                case LABEL_ON_TOP:
                    params = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                           TableRow.LayoutParams.MATCH_PARENT
                    );
                    pad(params);
                    params.span = 2;
                    params.weight = 1f;
                    label.setLayoutParams(params);
                    row.addView(label);
                    table.addView(row);

                    row = new TableRow(super.context);
                    params = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT
                    );
                    pad(params);
                    params.span = 2;
                    params.weight = 1f;
                    view.setLayoutParams(params);
                    row.addView(view);
                    break;

                case LABEL_ON_SIDE:
                    params = new TableRow.LayoutParams(
                            editor.expandHorizontal() ? TableRow.LayoutParams.WRAP_CONTENT : TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT
                    );
                    pad(params);
                    params.weight = editor.expandHorizontal() ? 0f : 1f;
                    label.setLayoutParams(params);
                    row.addView(label);


                    params = new TableRow.LayoutParams(
                            editor.expandHorizontal() ? TableRow.LayoutParams.MATCH_PARENT : TableRow.LayoutParams.WRAP_CONTENT,
                            editor.expandVertical() ? TableRow.LayoutParams.MATCH_PARENT : TableRow.LayoutParams.WRAP_CONTENT
                    );
                    params.column = 2;
                    params.weight = editor.expandHorizontal() ? 1f : 0f;
                    pad(params);
                    view.setLayoutParams(params);
                    row.addView(view);
                    break;

                case LABEL_HIDDEN:
                    params = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT
                    );
                    pad(params);
                    params.span = 2;
                    params.weight = 1f;
                    view.setLayoutParams(params);
                    row.addView(view);
                    break;
            }



            table.addView(row);
        }

    }

    private void pad(TableRow.LayoutParams params) {
        params.leftMargin = DisplayHelper.dpToPixel(10, context);
        params.rightMargin = DisplayHelper.dpToPixel(10, context);
        params.bottomMargin = DisplayHelper.dpToPixel(10, context);
        params.topMargin = DisplayHelper.dpToPixel(10, context);
    }

    @Override
    public View getView() {
        return table;
    }

    @Override
    public Object getComponent() {
        return table;
    }

    protected AndroidView fromValue(Value<?> value) {
        if (value instanceof Parameter) {
            return AndroidEditorFactory.forParameter((Parameter<?>)value, super.context);
        } else if (value instanceof Group) {
            return AndroidLayoutFactory.forGroup((Group)value, super.context);
        } else {
            return null;
        }
    }

}
