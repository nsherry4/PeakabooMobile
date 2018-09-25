package net.sciencestudio.peakaboo.androidui;

import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.data.MapSetController;
import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.plotter.data.DataLoader;
import peakaboo.dataset.DatasetReadResult;
import peakaboo.mapping.results.MapResultSet;
import plural.executor.ExecutorSet;

public class AppState {

    //TODO: Boy is this ugly

    public static PlotController controller;
    public static DataLoader dataloader;
    public static ExecutorSet<DatasetReadResult> dataloaderjob;

    public static MapResultSet mapresults;


}
