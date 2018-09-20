package net.sciencestudio.peakaboo.androidui.map;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;

import java.util.Collections;

import ca.hss.heatmaplib.HeatMap;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.data.MapRenderData;
import peakaboo.controller.mapper.data.MapSetController;
import peakaboo.controller.mapper.settings.MapRenderSettings;
import peakaboo.controller.mapper.settings.MapViewSettings;
import peakaboo.mapping.results.MapResultSet;
import scitypes.GridPerspective;
import scitypes.Pair;
import scitypes.Spectrum;

public class MapActivity extends AppCompatActivity {

    private HeatMap heatmap;
    private MappingController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        heatmap = (HeatMap) findViewById(R.id.map_heatmap);


        //TODO: Check incoming Intent, this may be a reload due to rotation, etc

        MapSetController mapSetController = new MapSetController();
        mapSetController.setMapData(AppState.mapresults, "", null, null, null, null);
        controller = new MappingController(mapSetController, null, AppState.controller);

        update();

    }


    private void update() {

        MapRenderData data = controller.getMapRenderData();
        MapRenderSettings settings = controller.getRenderSettings();

        Spectrum map;
        switch (settings.mode) {

            case COMPOSITE:
                populateComposite(data, settings);
                break;
            case OVERLAY:
                break;
            case RATIO:
                break;
        }



    }

    private void populateComposite(MapRenderData data, MapRenderSettings settings) {

        Spectrum map = data.compositeData;
        GridPerspective<Float> grid = new GridPerspective<>(settings.dataWidth, settings.dataHeight, 0f);


        heatmap.setMinimum(0d);
        heatmap.setMaximum(map.max());
        for (int i = 0; i < map.size(); i++) {
            Pair<Integer, Integer> coord = grid.getXYFromIndex(i);
            int x = coord.first;
            int y = coord.second;
            HeatMap.DataPoint point = new HeatMap.DataPoint(x, y, map.get(i));
            heatmap.addData(point);
        }
    }
}
