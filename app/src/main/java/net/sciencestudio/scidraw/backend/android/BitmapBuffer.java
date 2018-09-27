package net.sciencestudio.scidraw.backend.android;

import android.graphics.Bitmap;

import cyclops.visualization.Buffer;
import cyclops.visualization.palette.PaletteColour;

public class BitmapBuffer extends AndroidBitmapSurface implements Buffer {

    private int width, height;

    BitmapBuffer(int x, int y) {
        this(x, y, Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888, true));
    }

    private BitmapBuffer(int x, int y, Bitmap bm) {
        super(bm);
        this.width = x;
        this.height = y;
    }

    @Override
    public Bitmap getImageSource() {
        return bm;
    }

    @Override
    public void setPixelValue(int x, int y, PaletteColour paletteColour) {
        bm.setPixel(x, y, paletteColour.getARGB());
    }

    @Override
    public void setPixelValue(int i, PaletteColour paletteColour) {
        int px = i % width;
        int py = i / width;
        bm.setPixel(px, py, paletteColour.getARGB());
    }
}
