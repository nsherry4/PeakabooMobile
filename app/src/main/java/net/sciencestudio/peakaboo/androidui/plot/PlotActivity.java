package net.sciencestudio.peakaboo.androidui.plot;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.sciencestudio.autodialog.model.Group;
import net.sciencestudio.peakaboo.androidui.R;
import net.sciencestudio.peakaboo.androidui.log.LogViewActivity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import eventful.EventfulConfig;
import peakaboo.common.Env;
import peakaboo.common.PeakabooConfiguration;
import peakaboo.common.PeakabooLog;
import peakaboo.common.Version;
import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.plotter.data.DataLoader;
import peakaboo.curvefit.peak.table.PeakTable;
import peakaboo.curvefit.peak.table.SerializedPeakTable;
import peakaboo.curvefit.peak.transition.TransitionSeries;
import peakaboo.dataset.DatasetReadResult;
import peakaboo.datasink.plugin.DataSinkPluginManager;
import peakaboo.datasource.model.DataSource;
import net.sciencestudio.peakaboo.androidui.AndroidDataFile;
import net.sciencestudio.peakaboo.androidui.plot.chart.PlotChart;
import net.sciencestudio.plural.android.ExecutorSetView;

import peakaboo.datasource.plugin.DataSourcePluginManager;
import peakaboo.filter.model.FilterPluginManager;
import plural.executor.ExecutorSet;

public class PlotActivity extends AppCompatActivity {

    private static final String STATE_PLOT_CONTROLLER = "plotController";
    private PlotChart chart;


    private static final int ACTIVITY_OPEN_FILES = 1;



    //UI Lookups
    private DrawerLayout mDrawerLayout;
    private MenuItem approveFitting, rejectFitting;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeakabooLog.get().log(Level.INFO, "Starting Plot Activity");
        lookupUI();

        //set up drawer
        NavigationView drawer = findViewById(R.id.plot_drawer);
//        drawer.setNavigationItemSelectedListener(menuItem -> {
//            System.out.println("Item Selected " + menuItem);
//        });

        setupToolbar();

        startup();

        PeakabooLog.get().log(Level.WARNING, "Starting Up OnCreate");

        processIntentOrRestore();



        PlotState.controller.addListener(event -> {
            chart.update();
        });

        chart.update();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.plot_menu, menu);


        approveFitting = menu.findItem(R.id.action_approvefitting);
        rejectFitting = menu.findItem(R.id.action_rejectfitting);
        System.out.println("-------> " + mDrawerLayout);

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

            case R.id.action_approvefitting:
                actionApproveProposedFitting();
                return true;

            case R.id.action_rejectfitting:
                actionRejectProposedFitting();
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


    private void lookupUI() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_plot_drawer);
    }







    private void processIntentOrRestore() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (intent.getAction() == Intent.ACTION_VIEW && data != null) {

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
            return;
        }

    }



    private void startup() {

        PeakabooConfiguration.compression = false;
        PeakabooConfiguration.diskstore = true;

        PeakabooLog.init(new File(this.getFilesDir() + "/Logging/"));
        DataSourcePluginManager.init(new File(this.getFilesDir() + "/Plugins/DataSource/"));
        DataSinkPluginManager.init(new File(this.getFilesDir() + "/Plugins/DataSink/"));
        FilterPluginManager.init(new File(this.getFilesDir() + "/Plugins/Filters/"));
        EventfulConfig.uiThreadRunner = new Handler()::post;


        PeakabooLog.get().log(Level.INFO, "Max heap size = " + Env.maxHeap() + "MB");



        new Thread(() -> {
            PeakTable original = PeakTable.SYSTEM.getSource();
            String filename;
            if (Version.release) {
                filename = "derived-peakfile-" + Version.longVersionNo + ".yaml";
            } else {
                filename = "derived-peakfile-" + Version.longVersionNo + "-" + Version.buildDate + ".yaml";
            }
            File peakfile = new File(this.getFilesDir() + "/" + filename);
            PeakTable.SYSTEM.setSource(new SerializedPeakTable(original, peakfile));
        }).start();


        if (PlotState.controller == null) {
            PlotState.controller = new PlotController(this.getFilesDir());
        }

        chart = new PlotChart(this, PlotState.controller) {
            @Override
            protected void onLongPress(int channel) {
                tryFit(channel);
            }
        };
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
            Path tempDir = Files.createTempDirectory(cacheDir, "Temp");
            List<AndroidDataFile> datafiles = new ArrayList<>();

            for (Uri uri : uris) {
                Cursor cursor = getContentResolver().query(data, null, null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    PeakabooLog.get().log(Level.SEVERE, "Could not open dataset, cursor failure");
                    return;
                }

                String filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(data, "r");

                AndroidDataFile datafile = new AndroidDataFile(afd, filename, tempDir);
                datafiles.add(datafile);

            }



            loadDataSet(datafiles);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void tryFit(int channel) {
        List<TransitionSeries> proposals = PlotState.controller.fitting().proposeTransitionSeriesFromChannel(channel, null);
        if (proposals == null || proposals.size() == 0) {
            return;
        }
        PlotState.controller.fitting().clearProposedTransitionSeries();
        PlotState.controller.fitting().addProposedTransitionSeries(proposals.get(0));
        showFittingProposalControls();
    }

    private void showFittingProposalControls() {
        approveFitting.setVisible(true);
        rejectFitting.setVisible(true);
    }

    private void hideFittingProposalControls() {
        approveFitting.setVisible(false);
        rejectFitting.setVisible(false);
    }

    private void actionApproveProposedFitting() {
        PlotState.controller.fitting().commitProposedTransitionSeries();
        hideFittingProposalControls();
    }

    private void actionRejectProposedFitting() {
        PlotState.controller.fitting().clearProposedTransitionSeries();
        hideFittingProposalControls();
    }

    private void loadDataSet(List<AndroidDataFile> datafiles) throws IOException {

        List<Path> paths = new ArrayList<>();
        for (AndroidDataFile adf : datafiles) {
            Path path = adf.getAndEnsurePath();
            paths.add(path);
        }

        PeakabooLog.get().log(Level.INFO, "Loading New Data Set From AndroidDataFiles");

        DataLoader loader = new DataLoader(PlotState.controller, paths) {
            @Override
            public void onLoading(ExecutorSet<DatasetReadResult> executorSet) {
                //TODO:
                PeakabooLog.get().log(Level.INFO, "Showing Progress Dialog");
                ExecutorSetView progress = new ExecutorSetView(PlotActivity.this, executorSet);
                progress.show();
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
                //TODO: Hack
                PlotState.controller.fitting().setMaxEnergy(20.58f);
                PlotState.controller.fitting().setMinEnergy(0f);
                chart.update();
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
                Toast.makeText(PlotActivity.this, R.string.toast_newersession, Toast.LENGTH_LONG);
            }

            @Override
            public void onSessionHasData(File file, Consumer<Boolean> consumer) {
                //TODO:
                System.out.println("onSessionHasData");
                consumer.accept(true);
            }
        };

        loader.load();
    }


    private void showLog() {
        Intent i = new Intent(this, LogViewActivity.class);
        startActivity(i);
    }

}
