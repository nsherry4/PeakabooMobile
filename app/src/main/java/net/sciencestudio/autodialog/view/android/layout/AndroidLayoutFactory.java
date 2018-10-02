package net.sciencestudio.autodialog.view.android.layout;

import android.content.Context;

import net.sciencestudio.autodialog.model.Group;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AndroidLayoutFactory {


    private static Map<String, Function<Context, AndroidLayout>> styleProviders = new HashMap<>();



    public static void registerStyleProvider(String style, Function<Context, AndroidLayout> provider) {
        styleProviders.put(style, provider);
    }


    public static AndroidLayout forGroup(Group group, Context context) {

        AndroidLayout editor = null;

        for (String key : styleProviders.keySet()) {
            if (key.equals(group.getStyle().getStyle())) {
                editor = (AndroidLayout) styleProviders.get(key).apply(context);
                break;
            }
        }

        if (editor == null) {
            editor = fallback(context);
        }

        editor.initialize(group);
        return editor;


    }

    private static AndroidLayout fallback(Context context) {
        return new SimpleAndroidLayout(context);
    }

}
