package net.sciencestudio.peakaboo.androidui.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cyclops.Coord;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.data.MapSetController;
import peakaboo.controller.mapper.settings.MapFittingSettings;
import peakaboo.curvefit.peak.transition.TransitionSeries;
import peakaboo.display.map.MapScaleMode;


public class MapActivity extends AppCompatActivity {

    private CyclopsMapView mapChart;
    private Map<MenuItem, TransitionSeries> menuItems = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        // If there is no mapcontroller, or that mapcontroller's mapresults object isn't the same as
        // the current mapresults, we make everything anew
        if (AppState.mapcontroller == null || AppState.mapcontroller.mapsController.getMapResultSet() != AppState.mapresults) {
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

            //TODO: If we ever add Overlay/Ratio, this hard-coding will have to be removed
            AppState.mapcontroller.getSettings().getMapFittings().setMapScaleMode(MapScaleMode.RELATIVE);
        }


        setupUI();


    }

    void setupUI() {
        mapChart = findViewById(R.id.map_chart);
        NavigationView drawer = findViewById(R.id.plot_nav_drawer);
        drawer.bringToFront();
        Menu menu = drawer.getMenu();
        populateFittingsMenu(menu);
        drawer.invalidate();

        AppState.mapcontroller.addListener(event -> {
            updateUI();
            if (
                    !event.equals(MappingController.UpdateType.AREA_SELECTION.toString())
                            && !event.equals(MappingController.UpdateType.AREA_SELECTION.toString())) {

                mapChart.setNeedsRedraw();

            }
        });
    }

    void updateUI() {
        mapChart.invalidate();
    }

    private void populateFittingsMenu(Menu menu) {
        MapFittingSettings fitSettings = AppState.mapcontroller.getSettings().getMapFittings();
        int order = 0;

        for (TransitionSeries ts : fitSettings.getAllTransitionSeries()) {

            boolean visible = fitSettings.getTransitionSeriesVisibility(ts);
            MenuItem item = menu.add(Menu.NONE, Menu.NONE, order++, ts.toString());
            item.setOnMenuItemClickListener(this::onFittingItemClicked);
            item.setIcon(visible ? R.drawable.ic_check_box_black_24dp : R.drawable.ic_check_box_outline_blank_black_24dp);
            menuItems.put(item, ts);
        }
    }

    private boolean onFittingItemClicked(MenuItem item) {
        MapFittingSettings fitSettings = AppState.mapcontroller.getSettings().getMapFittings();

        if (menuItems.containsKey(item)) {
            TransitionSeries ts = menuItems.get(item);
            boolean visible = fitSettings.getTransitionSeriesVisibility(ts);

            //flip visibility
            visible = !visible;

            //update the model + view
            fitSettings.setTransitionSeriesVisibility(ts, visible);
            item.setIcon(visible ? R.drawable.ic_check_box_black_24dp : R.drawable.ic_check_box_outline_blank_black_24dp);
            //item.setChecked(visible);
            fitSettings.invalidateInterpolation();

            return true;
        }
        return false;
    }

}
