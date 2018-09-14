package net.sciencestudio.peakaboo.androidui.net.sciencestudio.peakaboo.androidui.plot;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.sciencestudio.autodialog.model.Group;
import net.sciencestudio.peakaboo.androidui.R;

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
import peakaboo.datasource.model.datafile.AndroidDataFile;
import peakaboo.datasource.plugin.DataSourcePluginManager;
import peakaboo.filter.model.FilterPluginManager;
import plural.executor.ExecutorSet;

public class PlotActivity extends AppCompatActivity {

    private PlotController controller;
    private PlotManager chart;

    private static final int ACTIVITY_OPEN_FILES = 1;



    //UI Lookups
    private DrawerLayout mDrawerLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lookupUI();

        setupToolbar();

        startup();
        processIntent();


        controller.addListener(event -> {
            chart.update();
        });

        chart.update();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.plot_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.home:
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
                return true;

            case R.id.action_help:
                return true;

            case R.id.action_about:
                return true;
        }

        return false;
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







    private void processIntent() {
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
        }
    }



    private void startup() {

        PeakabooLog.init(new File(this.getFilesDir() + "/Logging/"));
        DataSourcePluginManager.init(new File(this.getFilesDir() + "/Plugins/DataSource/"));
        DataSinkPluginManager.init(new File(this.getFilesDir() + "/Plugins/DataSink/"));
        FilterPluginManager.init(new File(this.getFilesDir() + "/Plugins/Filters/"));
        EventfulConfig.uiThreadRunner = new Handler()::post;

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


        controller = new PlotController(this.getFilesDir());
        chart = new PlotManager(this, controller) {
            @Override
            protected void onLongPress(int channel) {
                tryFit(channel);
            }
        };
    }

    private void selectDataSet() {

        Intent choose;
        choose = new Intent(Intent.ACTION_GET_CONTENT);
        choose.setType("file/*");
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
        System.out.println("LongPress of " + channel);
        System.out.println(controller.fitting().getEnergyCalibration().getMaxEnergy());
        System.out.println(controller.fitting().getFittedTransitionSeries());

        List<TransitionSeries> proposals = controller.fitting().proposeTransitionSeriesFromChannel(channel, null);
        System.out.println(proposals);
        if (proposals == null || proposals.size() == 0) {
            return;
        }
        controller.fitting().addProposedTransitionSeries(proposals.get(0));
    }

    private void loadDataSet(List<AndroidDataFile> datafiles) throws IOException {

        List<Path> paths = new ArrayList<>();
        for (AndroidDataFile adf : datafiles) {
            Path path = adf.getAndEnsurePath();
            paths.add(path);
        }

        System.out.println("Loading " + paths.toString());

        DataLoader loader = new DataLoader(controller, paths) {
            @Override
            public void onLoading(ExecutorSet<DatasetReadResult> executorSet) {
                //TODO:
                System.out.println("Loading...");
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
                controller.fitting().setMaxEnergy(20.58f);
                controller.fitting().setMinEnergy(0f);
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

    }

}
