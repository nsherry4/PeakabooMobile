package net.sciencestudio.peakaboo.androidui.map;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;
import net.sciencestudio.scidraw.backend.android.AndroidSurface;

import java.util.ArrayList;

import cyclops.Coord;
import eventful.EventfulConfig;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.data.MapRenderData;
import peakaboo.controller.mapper.data.MapSetController;
import peakaboo.controller.mapper.settings.MapRenderSettings;
import peakaboo.display.map.Mapper;


public class MapActivity extends AppCompatActivity {

    private MappingController controller;
    private ImageView view;
    private Mapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        view = findViewById(R.id.map_image);
        mapper = new Mapper();

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
        EventfulConfig.uiThreadRunner.accept(() -> {
            update();
        });

    }

    private void update() {

        //Sometimes the view has size 0? I assume this happens when update() is called before the
        //view is laid out in the screen
        int width = Math.max(view.getWidth(), 100);
        int height = Math.max(view.getHeight(), 100);

        System.out.println(width);
        System.out.println(height);
        System.out.println("------------");

        MapRenderData data = controller.getMapRenderData();
        MapRenderSettings settings = controller.getRenderSettings();

        //TODO: Don't create a new bitmap every redraw, create it once and only replace it if the view size changes
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        AndroidSurface surface = new AndroidSurface(bm);
        mapper.draw(data, settings, surface, new Coord<>(width, height));

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


}
