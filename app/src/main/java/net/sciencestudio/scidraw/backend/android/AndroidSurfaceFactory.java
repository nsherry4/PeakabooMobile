package net.sciencestudio.scidraw.backend.android;

import android.graphics.Bitmap;

import scitypes.visualization.SaveableSurface;
import scitypes.visualization.Surface;
import scitypes.visualization.SurfaceFactory;
import scitypes.visualization.SurfaceType;

public class AndroidSurfaceFactory implements SurfaceFactory {

    @Override
    public Surface createScreenSurface(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SaveableSurface createSaveableSurface(SurfaceType surfaceType, int x, int y) {
        switch (surfaceType) {

            case RASTER:
                return new AndroidSurface(Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888));
            case VECTOR:
                throw new UnsupportedOperationException();
            case PDF:
                throw new UnsupportedOperationException();
        }

        return null;
    }
}
