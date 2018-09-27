package net.sciencestudio.cyclops.visualization.backend.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.IOException;
import java.io.OutputStream;

import cyclops.visualization.SaveableSurface;
import cyclops.visualization.Surface;


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
