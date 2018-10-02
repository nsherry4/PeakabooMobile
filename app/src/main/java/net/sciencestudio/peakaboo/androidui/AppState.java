package net.sciencestudio.peakaboo.androidui;

import android.os.Handler;

import java.io.File;
import java.util.logging.Level;

import eventful.EventfulConfig;
import peakaboo.common.Env;
import peakaboo.common.PeakabooConfiguration;
import peakaboo.common.PeakabooLog;
import peakaboo.common.Version;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.plotter.data.DataLoader;
import peakaboo.curvefit.peak.table.PeakTable;
import peakaboo.curvefit.peak.table.SerializedPeakTable;
import peakaboo.dataset.DatasetReadResult;
import peakaboo.datasink.plugin.DataSinkPluginManager;
import peakaboo.datasource.plugin.DataSourcePluginManager;
import peakaboo.datasource.plugins.APSSector20.APSSector20;
import peakaboo.datasource.plugins.amptek.AmptekMCA;
import peakaboo.datasource.plugins.clscdfml.CDFMLSaxDataSource;
import peakaboo.datasource.plugins.incaemsa.Emsa;
import peakaboo.datasource.plugins.sigray2018.Sigray2018HDF5;
import peakaboo.datasource.plugins.vespers.ScienceStudioDataSource;
import peakaboo.filter.model.FilterPluginManager;
import peakaboo.mapping.results.MapResultSet;
import plural.executor.ExecutorSet;

public class AppState {

    //TODO: Boy is this ugly

    public static PlotController controller;
    public static DataLoader dataloader;
    public static ExecutorSet<DatasetReadResult> dataloaderjob;

    public static MapResultSet mapresults;
    public static MappingController mapcontroller;

    private static boolean inited = false;
    public static void init(String filesDir) {
        if (inited) {
            return;
        }
        inited = true;


        PeakabooConfiguration.diskstore = true;

        PeakabooLog.init(new File(filesDir + "/Logging/"));
        DataSourcePluginManager.init(new File(filesDir + "/Plugins/DataSource/"));
        DataSourcePluginManager.SYSTEM.registerPlugin(Sigray2018HDF5.class);
        DataSourcePluginManager.SYSTEM.registerPlugin(AmptekMCA.class);
        DataSourcePluginManager.SYSTEM.registerPlugin(CDFMLSaxDataSource.class);
        DataSourcePluginManager.SYSTEM.registerPlugin(Emsa.class);
        DataSourcePluginManager.SYSTEM.registerPlugin(ScienceStudioDataSource.class);
        DataSourcePluginManager.SYSTEM.registerPlugin(APSSector20.class);

        DataSinkPluginManager.init(new File(filesDir + "/Plugins/DataSink/"));
        FilterPluginManager.init(new File(filesDir + "/Plugins/Filters/"));
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
            File peakfile = new File(filesDir + "/" + filename);
            PeakTable.SYSTEM.setSource(new SerializedPeakTable(original, peakfile));
        }).start();


    }

}
