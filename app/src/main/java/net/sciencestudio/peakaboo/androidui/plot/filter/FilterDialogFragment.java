package net.sciencestudio.peakaboo.androidui.plot.filter;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import net.sciencestudio.autodialog.view.android.AndroidAutoDialog;
import net.sciencestudio.bolt.plugin.core.BoltPluginPrototype;
import net.sciencestudio.bolt.plugin.core.BoltPluginSet;
import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.PeakabooUtil;
import net.sciencestudio.peakaboo.androidui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eventful.EventfulListener;
import peakaboo.filter.model.Filter;
import peakaboo.filter.model.FilterPluginManager;
import peakaboo.filter.plugins.FilterPlugin;

public class FilterDialogFragment extends DialogFragment {

    private ListView list;
    private FilterListAdapter adapter;
    private ImageButton add, remove, settings;
    private EventfulListener controllerListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_filters, null);
        builder.setView(view);
        builder.setOnDismissListener(dialog -> {
            AppState.controller.filtering().removeListener(controllerListener);
        });
        builder.setTitle(R.string.action_filters);

        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();

        add = view.findViewById(R.id.dialog_filter_add);
        //remove = view.findViewById(R.id.filter_renderer_remove);
        //settings = view.findViewById(R.id.filter_renderer_settings);
        list = view.findViewById(R.id.list_active_filters);
        adapter = new FilterListAdapter(getContext(), new ArrayList<>(), this::showSettings);
        list.setAdapter(adapter);
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        list.setSelector(new ColorDrawable(PeakabooUtil.accentColour(getContext())));
        populateAdapter();

        controllerListener = () -> {
            populateAdapter();
        };
        AppState.controller.filtering().addListener(controllerListener);

        add.setOnClickListener(v -> {
            promptAddFilter();
        });

//        remove.setOnClickListener(v -> {
//            int index = list.getCheckedItemPosition();
//            System.err.println(index);
//            if (index == AdapterView.INVALID_POSITION) {
//                return;
//            }
//            AppState.controller.filtering().removeFilter(index);
//        });

//        settings.setOnClickListener(v -> {
//            int index = list.getCheckedItemPosition();
//            System.err.println(index);
//            if (index == AdapterView.INVALID_POSITION) {
//                return;
//            }
//            Filter filter = AppState.controller.filtering().getActiveFilters().get(index);
//            showSettings(filter);
//        });




        return dialog;
    }


    private void promptAddFilter() {
        BoltPluginSet<FilterPlugin> plugins = FilterPluginManager.SYSTEM.getPlugins();
        List<BoltPluginPrototype<? extends FilterPlugin>> indexedPlugins = new ArrayList<>();
        for (BoltPluginPrototype<? extends FilterPlugin> plugin : plugins.getAll()) {
            indexedPlugins.add(plugin);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Filter");

        String[] names = indexedPlugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[0]);
        builder.setItems(names, (dialog, index) -> {
            BoltPluginPrototype<? extends FilterPlugin> selected = indexedPlugins.get(index);
            Filter filter = selected.create();
            filter.initialize();
            AppState.controller.filtering().addFilter(filter);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void populateAdapter() {
        adapter.clear();
        adapter.addAll(AppState.controller.filtering().getActiveFilters().getFilters());
    }

    private void showSettings(Filter filter) {
        AlertDialog dialog = AndroidAutoDialog.create(filter.getParameterGroup(), getContext());
        dialog.show();
    }
}
