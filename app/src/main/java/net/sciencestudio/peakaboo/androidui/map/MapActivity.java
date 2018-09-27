package net.sciencestudio.peakaboo.androidui.map;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;

import java.util.ArrayList;

import cyclops.Coord;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.data.MapSetController;


public class MapActivity extends AppCompatActivity {

    private MapView view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //TODO: Check incoming Intent, this may be a reload due to rotation, etc

        MapSetController mapSetController = new MapSetController();
        mapSetController.setMapData(AppState.mapresults, "", new ArrayList<>(), null, null, null);
        AppState.mapcontroller = new MappingController(mapSetController, null, AppState.controller);

        if (AppState.mapcontroller.mapsController.hasOriginalDataDimensions()) {
            AppState.mapcontroller.getSettings().getView().setDataWidth(AppState.mapcontroller.mapsController.getOriginalDataWidth());
            AppState.mapcontroller.getSettings().getView().setDataHeight(AppState.mapcontroller.mapsController.getOriginalDataHeight());
        } else {
            //no option to set dimensions, so guess instead
            Coord<Integer> guess = AppState.mapcontroller.mapsController.guessDataDimensions().run().get();
            AppState.mapcontroller.getSettings().getView().setDataWidth(guess.x);
            AppState.mapcontroller.getSettings().getView().setDataHeight(guess.y);
        }


        ConstraintLayout layout = findViewById(R.id.map_top_layout);
        view = new MapView(this);
        layout.addView(view);



    }


}
