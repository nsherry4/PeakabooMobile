package net.sciencestudio.peakaboo.androidui;

import android.content.Context;
import android.os.Environment;
import android.util.TypedValue;

public class PeakabooUtil {

    public static boolean saveFile(String name, String contents) {
        if (!canSave()) {
            return false;
        }


        return true;
    }

    private static boolean canSave() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static int accentColour(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

}
