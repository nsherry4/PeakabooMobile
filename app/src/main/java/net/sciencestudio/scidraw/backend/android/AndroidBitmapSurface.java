package net.sciencestudio.scidraw.backend.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import cyclops.visualization.Buffer;
import cyclops.visualization.SaveableSurface;
import cyclops.visualization.Surface;
import cyclops.visualization.palette.PaletteColour;


public class AndroidBitmapSurface extends AbstractAndroidSurface implements SaveableSurface {

    protected Bitmap bm;

    public AndroidBitmapSurface(int width, int height) {
        this(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
    }

    public AndroidBitmapSurface(Bitmap bm) {
        super(new Canvas(bm));
        this.bm = bm;
    }



    @Override
    public Surface getNewContextForSurface() {
        return new AndroidBitmapSurface(bm);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    }
}
