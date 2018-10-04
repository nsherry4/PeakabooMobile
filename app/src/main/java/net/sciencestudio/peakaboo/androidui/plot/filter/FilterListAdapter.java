package net.sciencestudio.peakaboo.androidui.plot.filter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;

import java.util.List;
import java.util.function.Consumer;

import peakaboo.filter.model.Filter;

public class FilterListAdapter extends ArrayAdapter<Filter>{

    private Consumer<Filter> settings;

    public FilterListAdapter(@NonNull Context context, @NonNull List<Filter> filters, Consumer<Filter> settings) {
        super(context, 0, filters);
        this.settings = settings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Filter filter = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_filter_renderer, parent, false);
        }

        TextView name = convertView.findViewById(R.id.filter_renderer_name);
        ImageButton up = convertView.findViewById(R.id.filter_renderer_up);
        ImageButton down = convertView.findViewById(R.id.filter_renderer_down);
        ImageButton remove = convertView.findViewById(R.id.filter_renderer_remove);
        ImageButton settings = convertView.findViewById(R.id.filter_renderer_settings);

        up.setTag(position);
        up.setOnClickListener(this::onUp);

        down.setTag(position);
        down.setOnClickListener(this::onDown);

        remove.setTag(position);
        remove.setOnClickListener(this::onRemove);

        settings.setTag(position);
        settings.setOnClickListener(this::onSettings);

        name.setText(filter.getFilterName());

        return convertView;

    }

    private void onUp(View view) {
        AppState.controller.filtering().moveFilterUp((int) view.getTag());
    }

    private void onDown(View view) {
        AppState.controller.filtering().moveFilterDown((int) view.getTag());
    }

    private void onRemove(View view) {
        AppState.controller.filtering().removeFilter((int) view.getTag());
    }

    private void onSettings(View view) {
        Filter filter = getItem((int) view.getTag());
        settings.accept(filter);
    }

}
