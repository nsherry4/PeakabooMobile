package net.sciencestudio.peakaboo.androidui.plot;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.sciencestudio.autodialog.model.Group;
import net.sciencestudio.autodialog.view.android.AndroidAutoDialog;
import net.sciencestudio.bolt.plugin.core.BoltPluginController;
import net.sciencestudio.bolt.plugin.core.BoltPluginSet;
import net.sciencestudio.peakaboo.androidui.AndroidDataFile;
import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;
import net.sciencestudio.peakaboo.androidui.log.LogViewActivity;
import net.sciencestudio.peakaboo.androidui.map.MapActivity;
import net.sciencestudio.peakaboo.androidui.plot.chart.cyclops.CyclopsPlotView;
import net.sciencestudio.plural.android.ExecutorSetView;
import net.sciencestudio.plural.android.StreamExecutorView;
import net.sciencestudio.scratch.encoders.compressors.Compressors;
import net.sciencestudio.scratch.encoders.serializers.Serializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import cyclops.ISpectrum;
import eventful.EventfulConfig;
import peakaboo.common.Env;
import peakaboo.common.PeakabooConfiguration;
import peakaboo.common.PeakabooLog;
import peakaboo.common.Version;
import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.plotter.data.DataLoader;
import peakaboo.curvefit.curve.fitting.FittingResult;
import peakaboo.curvefit.peak.table.PeakTable;
import peakaboo.curvefit.peak.table.SerializedPeakTable;
import peakaboo.curvefit.peak.transition.TransitionSeries;
import peakaboo.dataset.DatasetReadResult;
import peakaboo.datasink.plugin.DataSinkPluginManager;
import peakaboo.datasource.model.DataSource;
import peakaboo.datasource.plugin.DataSourcePluginManager;
import peakaboo.filter.model.Filter;
import peakaboo.filter.model.FilterPluginManager;
import peakaboo.filter.plugins.FilterPlugin;
import peakaboo.mapping.results.MapResultSet;
import plural.executor.ExecutorSet;
import plural.streams.StreamExecutor;

public class PlotActivity extends AppCompatActivity {

    //private MPPlotChart chart;
    private CyclopsPlotView chart;


    private static final int ACTIVITY_OPEN_FILES = 1;


    //UI Lookups
    private DrawerLayout mDrawerLayout;
    private MenuItem energyCalibration, mapFittings, filterMenuItem;
    private FloatingActionButton fabAcceptFitting, fabRejectFitting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeakabooLog.get().log(Level.INFO, "Starting Plot Activity");

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_peakaboo);
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, getColor(R.color.peakaboo_primary_dark));
        PlotActivity.this.setTaskDescription(taskDescription);

        mDrawerLayout = findViewById(R.id.drawer_layout);


        setupToolbar();

        startup();

        PeakabooLog.get().log(Level.WARNING, "Starting Up OnCreate");

        processIntentOrRestore();

        //set up drawer, add menu selection hook
        NavigationView drawer = findViewById(R.id.plot_nav_drawer);
        drawer.bringToFront();
        drawer.setNavigationItemSelectedListener(menuItem -> {
            mDrawerLayout.closeDrawers();
            return onOptionsItemSelected(menuItem);
        });
        energyCalibration = drawer.getMenu().findItem(R.id.action_energy);
        mapFittings = drawer.getMenu().findItem(R.id.action_map);
        filterMenuItem = drawer.getMenu().findItem(R.id.action_filters);

        AppState.controller.addListener(event -> {
            updateUI();
        });

        updateUI();

        if (AppState.dataloader != null && AppState.dataloaderjob != null) {
            showDataLoaderProgress();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.plot_menu, menu);
        System.out.println("-------> " + mDrawerLayout);
        EventfulConfig.uiThreadRunner.accept(this::updateUI);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;


            case R.id.action_opendataset:
                selectDataSet();
                return true;

            case R.id.action_savesession:
                return true;

            case R.id.action_export:
                return true;

            case R.id.action_undo:
                return true;

            case R.id.action_redo:
                return true;

            case R.id.action_plugins:
                return true;

            case R.id.action_logs:
                showLog();
                return true;

            case R.id.action_help:
                return true;

            case R.id.action_about:
                return true;


            //DRAWER MENU

            case R.id.action_map:

                //Calculate and Launch Map
                PeakabooLog.get().log(Level.INFO, "Mapping!");
                calculateMap();
                return true;

            case R.id.action_filters:
                return true;

            case R.id.action_fittings:
                return true;

            case R.id.action_energy:
                showEnergyCalibration();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {

            case ACTIVITY_OPEN_FILES:
                if (resultCode != RESULT_OK) { return; }
                if (intent.getClipData() != null) {
                    List<Uri> uris = new ArrayList<>();
                    for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                        Uri uri = intent.getClipData().getItemAt(i).getUri();
                        uris.add(uri);
                    }
                    openDataSet(uris);
                } else {
                    Uri uri = intent.getData();
                    openDataSet(Collections.singletonList(uri));
                }
                return;

            default:
                return;
        }


    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_plot_drawer);
    }







    private void processIntentOrRestore() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (intent.getAction().equals(Intent.ACTION_VIEW) && data != null) {

            System.out.println("EXTRAS:");
            if (intent.getExtras() != null) {
                for (String extra : intent.getExtras().keySet()) {
                    System.out.println(extra);
                }
            }
            System.out.println("INFO:");
            System.out.println(intent.getDataString());
            System.out.println(intent.getData());


            openDataSet(Collections.singletonList(intent.getData()));
        }

    }



    private void startup() {

        AppState.init(this.getFilesDir().toString());


        if (AppState.controller == null) {
            AppState.controller = new PlotController(this.getFilesDir());
        }


        chart = findViewById(R.id.plot_chart);
        chart.setOnRequestFitting(this::tryFit);
        chart.setOnSelectFitting(this::onFittingSelected);

        CoordinatorLayout mainLayout = findViewById(R.id.plot_main_area);
        fabAcceptFitting = findViewById(R.id.plot_fab_acceptfitting);
        fabAcceptFitting.setOnClickListener(v -> actionApproveFitting());
        fabRejectFitting = findViewById(R.id.plot_fab_rejectfitting);
        fabRejectFitting.setOnClickListener(v -> actionRejectFitting());

        PeakabooLog.get().log(Level.INFO, "Added chart component");


    }

    private void onFabClick(View view) {

    }

    private void updateUI() {

        //Drawer menu items enabled when dataset present
        if (energyCalibration != null) energyCalibration.setEnabled(true);
        if (mapFittings != null) mapFittings.setEnabled(
                AppState.controller.data().hasDataSet() &&
                !AppState.controller.fitting().getFittingSelections().isEmpty() &&
                !AppState.controller.fitting().getEnergyCalibration().isZero()
        );

        //Floating Action Buttons
        if (!AppState.controller.fitting().getProposedTransitionSeries().isEmpty()) {
            if (fabAcceptFitting != null){
                fabAcceptFitting.invalidate();
                if (fabRejectFitting != null) fabRejectFitting.hide();
                if (!fabAcceptFitting.isShown()) fabAcceptFitting.show();
            }
        } else if (!AppState.controller.fitting().getHighlightedTransitionSeries().isEmpty()) {
            if (fabRejectFitting != null){
                fabRejectFitting.invalidate();
                if (fabAcceptFitting != null) fabAcceptFitting.hide();
                if (!fabRejectFitting.isShown()) fabRejectFitting.show();
            }
        } else {
            if (fabAcceptFitting != null && fabAcceptFitting.isShown()) fabAcceptFitting.hide();
            if (fabRejectFitting != null && fabRejectFitting.isShown()) fabRejectFitting.hide();
        }

        //Filter Submenu
        Menu filterMenu = filterMenuItem.getSubMenu();
        filterMenu.clear();
        MenuItem item = filterMenu.add("Add Filter");
        item.setOnMenuItemClickListener(mi -> {
            promptAddFilter();
            return true;
        });

        for (Filter filter : AppState.controller.filtering().getActiveFilters()) {
            createFilterMenuEntry(filter);
        }


        chart.invalidate();
    }

    private void createFilterMenuEntry(Filter filter) {
        Menu filterMenu = filterMenuItem.getSubMenu();
        MenuItem item = filterMenu.add(filter.getFilterName());
        item.setOnMenuItemClickListener(menuitem -> {
            AlertDialog dialog = AndroidAutoDialog.create(filter.getParameterGroup(), this);
            dialog.show();
            return true;
        });
    }


    private void selectDataSet() {

        Intent choose;
        choose = new Intent(Intent.ACTION_GET_CONTENT);
        choose.setType("*/*");
        choose.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent intent = Intent.createChooser(choose, "Select Data Set");
        startActivityForResult(intent, ACTIVITY_OPEN_FILES);

    }

    private void openDataSet(List<Uri> uris) {

        Uri data = uris.get(0);

        try {

            Path cacheDir = getCacheDir().toPath();
            //TODO: Implement a cleanup method which checks for older dataset folders and clears them?
            Path tempDir = Files.createTempDirectory(cacheDir, "DS-Temp");

            List<AndroidDataFile> datafiles = new ArrayList<>();

            for (Uri uri : uris) {
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    PeakabooLog.get().log(Level.SEVERE, "Could not open dataset, cursor failure");
                    return;
                }

                String filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(data, "r");

                AndroidDataFile datafile = new AndroidDataFile(afd, filename, tempDir);
                datafiles.add(datafile);
                cursor.close();

            }



            loadDataSet(datafiles);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void tryFit(int channel) {
        if (AppState.controller.fitting().getEnergyCalibration().isZero()) {
            promptEnergyCalibration();
            return;
        }

        List<TransitionSeries> proposals = AppState.controller.fitting().proposeTransitionSeriesFromChannel(channel, null);
        if (proposals == null || proposals.size() == 0) {
            return;
        }
        AppState.controller.fitting().clearProposedTransitionSeries();
        AppState.controller.fitting().addProposedTransitionSeries(proposals.get(0));
        updateUI();
    }



    private void actionApproveFitting() {
        AppState.controller.fitting().commitProposedTransitionSeries();
        updateUI();
    }

    private void actionRejectFitting() {
        if (AppState.controller.fitting().getProposedTransitionSeries().isEmpty()) {
            //if no proposals, this is shown for a selected fitting
            List<TransitionSeries> selected = AppState.controller.fitting().getHighlightedTransitionSeries();
            for (TransitionSeries ts : selected) {
                AppState.controller.fitting().removeTransitionSeries(ts);
            }
            AppState.controller.fitting().setHighlightedTransitionSeries(Collections.emptyList());
        } else {
            AppState.controller.fitting().clearProposedTransitionSeries();
        }

        updateUI();
    }

    private void onFittingSelected(FittingResult fitting) {
        AppState.controller.fitting().clearProposedTransitionSeries();
        AppState.controller.fitting().setHighlightedTransitionSeries(Collections.emptyList());
        if (fitting != null) {
            AppState.controller.fitting().setHighlightedTransitionSeries(Collections.singletonList(fitting.getTransitionSeries()));
        }
        updateUI();
    }



    private void loadDataSet(List<AndroidDataFile> datafiles) throws IOException {

        List<Path> paths = new ArrayList<>();
        for (AndroidDataFile adf : datafiles) {
            Path path = adf.getAndEnsurePath();
            paths.add(path);
        }

        PeakabooLog.get().log(Level.INFO, "Loading New Data Set From AndroidDataFiles");

        AppState.dataloader = new DataLoader(AppState.controller, paths) {
            @Override
            public void onLoading(ExecutorSet<DatasetReadResult> executorSet) {
                //TODO:
                AppState.dataloaderjob = executorSet;
                showDataLoaderProgress();
            }

            @Override
            public void onSuccess(List<Path> list) {
                //clean up cache from loading
                for (AndroidDataFile datafile : datafiles) {
                    try {
                        datafile.close();
                    } catch (Exception e) {
                        //NOOP
                    }
                }
                System.out.println("Success!");
                AppState.dataloader = null;
                AppState.dataloaderjob = null;

                updateUI();

            }

            @Override
            public void onFail(List<Path> list, String s) {
                System.out.println("FAILED!!!!! - " + s);
                //TODO:
            }

            @Override
            public void onParameters(Group group, Consumer<Boolean> consumer) {
                //TODO:
                System.out.println("onParameters");
                consumer.accept(true);
            }

            @Override
            public void onSelection(List<DataSource> list, Consumer<DataSource> consumer) {
                //TODO:
                System.out.println("onSelection");
                consumer.accept(list.get(0));
            }

            @Override
            public void onSessionNewer() {
                //TODO:
                System.out.println("onSessionNewer");
                Toast t = Toast.makeText(PlotActivity.this, R.string.toast_newersession, Toast.LENGTH_LONG);
                t.show();
            }

            @Override
            public void onSessionHasData(File file, Consumer<Boolean> consumer) {
                //TODO:
                System.out.println("onSessionHasData");
                consumer.accept(true);
            }
        };

        AppState.dataloader.load();
    }

    private void showDataLoaderProgress() {
        PeakabooLog.get().log(Level.INFO, "Showing Progress Dialog");
        ExecutorSetView progress = new ExecutorSetView(PlotActivity.this, AppState.dataloaderjob);
        progress.show();
    }


    private void showLog() {
        Intent i = new Intent(this, LogViewActivity.class);
        startActivity(i);
    }

    private void calculateMap() {
        StreamExecutor<MapResultSet> results = AppState.controller.getMapTask();
        System.out.println("RESULTS: " + results);
        StreamExecutorView dialog = new StreamExecutorView(PlotActivity.this, results);
        results.addListener(event -> {
            if (event == StreamExecutor.Event.COMPLETED && results.getResult().isPresent()) {
                showMap(results.getResult().get());
            }
        });

        dialog.show();
        results.start();
    }

    private void showMap(MapResultSet maps) {
        Intent mapIntent = new Intent(PlotActivity.this, MapActivity.class);
        AppState.mapresults = maps;
        AppState.mapcontroller = null;
        PlotActivity.this.startActivity(mapIntent);
    }

    private void promptAddFilter() {
        BoltPluginSet<FilterPlugin> plugins = FilterPluginManager.SYSTEM.getPlugins();
        List<BoltPluginController<? extends FilterPlugin>> indexedPlugins = new ArrayList<>();
        for (BoltPluginController<? extends FilterPlugin> plugin : plugins.getAll()) {
            indexedPlugins.add(plugin);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Filter");

        String[] names = indexedPlugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[0]);
        builder.setItems(names, (dialog, index) -> {
            BoltPluginController<? extends FilterPlugin> selected = indexedPlugins.get(index);
            Filter filter = selected.create();
            filter.initialize();
            AppState.controller.filtering().addFilter(filter);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void promptEnergyCalibration() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cannot add fittings without energy calibration. Calibrate now?");
        builder.setTitle("No Energy Calibration");
        builder.setNegativeButton("Cancel", (dialog, id) -> {
        });
        builder.setPositiveButton("OK", (dialog, id) -> {
            showEnergyCalibration();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEnergyCalibration() {

        EnergyCalibrationDialogFragment dialogFragment = new EnergyCalibrationDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "energy");

    }

}
