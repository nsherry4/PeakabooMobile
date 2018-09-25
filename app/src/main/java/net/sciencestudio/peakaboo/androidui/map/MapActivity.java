package net.sciencestudio.peakaboo.androidui.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.widget.ImageView;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;
import net.sciencestudio.scidraw.backend.android.AndroidSurface;
import net.sciencestudio.scidraw.backend.android.AndroidSurfaceFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import peakaboo.common.PeakabooConfiguration;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.data.MapRenderData;
import peakaboo.controller.mapper.data.MapSetController;
import peakaboo.controller.mapper.settings.MapRenderSettings;
import peakaboo.controller.mapper.settings.MapViewSettings;
import peakaboo.display.map.Mapper;
import peakaboo.mapping.results.MapResultSet;
import plural.streams.StreamExecutor;
import scitypes.Coord;
import scitypes.GridPerspective;
import scitypes.Pair;
import scitypes.Spectrum;
import scitypes.visualization.SaveableSurface;
import scitypes.visualization.SurfaceType;
import scitypes.visualization.palette.palettes.AbstractPalette;
import scitypes.visualization.palette.palettes.ThermalScalePalette;

public class MapActivity extends AppCompatActivity {

    private MappingController controller;
    private ImageView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        view = findViewById(R.id.map_image);

        //TODO: Check incoming Intent, this may be a reload due to rotation, etc

        MapSetController mapSetController = new MapSetController();
        mapSetController.setMapData(AppState.mapresults, "", new ArrayList<>(), null, null, null);
        controller = new MappingController(mapSetController, null, AppState.controller);

        if (controller.mapsController.hasOriginalDataDimensions()) {
            controller.getSettings().getView().setDataWidth(controller.mapsController.getOriginalDataWidth());
            controller.getSettings().getView().setDataHeight(controller.mapsController.getOriginalDataHeight());
        } else {
            //no option to set dimensions, so guess instead
            Coord<Integer> guess = controller.mapsController.guessDataDimensions().run().get();
            controller.getSettings().getView().setDataWidth(guess.x);
            controller.getSettings().getView().setDataHeight(guess.y);
        }

        update();

    }

    private void drawMap(MapRenderData data, MapRenderSettings settings) {

        Mapper mapper = new Mapper();

        Bitmap bm = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        AndroidSurface surface = new AndroidSurface(bm);
        mapper.draw(data, settings, surface, false, new Coord<>(view.getWidth(), view.getHeight()));

//
//        Bitmap bmp = Bitmap.createBitmap(settings.dataWidth, settings.dataHeight, Bitmap.Config.ARGB_8888, true);
//
//        Spectrum map = data.compositeData;
//        float max = map.max();
//        GridPerspective<Float> grid = new GridPerspective<>(settings.dataWidth, settings.dataHeight, 0f);
//
//        AbstractPalette palette = new ThermalScalePalette(1000, false);
//
//        for (int i = 0; i < map.size(); i++) {
//            Pair<Integer, Integer> coord = grid.getXYFromIndex(i);
//            int x = coord.first;
//            int y = coord.second;
//            bmp.setPixel(x, y, palette.getFillColour(map.get(i), max).getARGB());
//        }


        BitmapDrawable drawable = new BitmapDrawable(getResources(), bm);
        drawable.getPaint().setFilterBitmap(false);
        view.setImageDrawable(drawable);

    }

    private void update() {

        MapRenderData data = controller.getMapRenderData();
        MapRenderSettings settings = controller.getRenderSettings();

        Spectrum map;
        switch (settings.mode) {

            case COMPOSITE:
                drawMap(data, settings);
                break;
            case OVERLAY:
                break;
            case RATIO:
                break;
        }



    }

}
