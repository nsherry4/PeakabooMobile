package net.sciencestudio.autodialog.view.android.editor;

import android.content.Context;

import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.model.style.CoreStyle;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AndroidEditorFactory {

    private static Map<String, Function<Context, AndroidEditor<?>>> styleProviders = new HashMap<>();


    public static void registerStyleProvider(String style, Function<Context, AndroidEditor<?>> provider) {
        styleProviders.put(style, provider);
    }

    public static List<AndroidEditor<?>> forParameters(Collection<Parameter<?>> parameters, Context context) {
        return parameters.stream().map(p -> AndroidEditorFactory.forParameter(p, context)).collect(Collectors.toList());
    }

    public static <T> AndroidEditor<T> forParameter(Parameter<T> parameter, Context context) {

        AndroidEditor<T> editor = null;

        for (String key : styleProviders.keySet()) {
            if (key.equals(parameter.getStyle().getStyle())) {
                editor = (AndroidEditor<T>) styleProviders.get(key).apply(context);
                break;
            }
        }

        if (editor == null) {
            //Fallback to CoreStyle
            editor = fallback(parameter.getStyle().getFallbackStyle(), context);
        }

        editor.initialize(parameter);
        return editor;


    }

    private static <T> AndroidEditor<T> fallback(CoreStyle fallbackStyle, Context context) {
        switch (fallbackStyle) {
            case BOOLEAN: return (AndroidEditor<T>) new BooleanEditor(context);
            //case TEXT_VALUE: return (AndroidEditor<T>) new TextBoxEditor();
            //case TEXT_AREA: return (AndroidEditor<T>) new TextAreaEditor();
            case INTEGER: return (AndroidEditor<T>) new IntegerEditor(context);
            case FLOAT: return (AndroidEditor<T>) new FloatEditor(context);
            //case LIST: return (AndroidEditor<T>) new ListEditor<T>();
            case SPACING: return (AndroidEditor<T>) new SeparatorEditor(context);
            default: return null;
        }
    }

}
