package net.sciencestudio.peakaboo.androidui.plot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import net.sciencestudio.peakaboo.androidui.AppState;
import net.sciencestudio.peakaboo.androidui.R;

public class EnergyCalibrationDialogFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_energy_calibration, null);
        builder.setView(view).setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });

        builder.setTitle(R.string.action_energy);




        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();


        NumberPicker minEnergy = view.findViewById(R.id.min_energy);
        NumberPicker maxEnergy = view.findViewById(R.id.max_energy);

        System.out.println("MinEnergy: " + AppState.controller.fitting().getMinEnergy());
        System.out.println("MinEnergy: " + AppState.controller.fitting().getMaxEnergy());

        minEnergy.setMinValue(0*1000);
        minEnergy.setMaxValue(1*1000);
        minEnergy.setValue((int)AppState.controller.fitting().getMinEnergy()*1000);
        minEnergy.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> {
            try {
                AppState.controller.fitting().setMinEnergy(((float) newVal) / 1000f);
            } catch (Exception e) {
                //BAD
            }
        });


        maxEnergy.setMinValue(0*1000);
        maxEnergy.setMaxValue(100*1000);
        maxEnergy.setValue((int)(AppState.controller.fitting().getMaxEnergy()*1000));
        maxEnergy.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> {
            try {
                AppState.controller.fitting().setMaxEnergy(((float) newVal) / 1000f);
            } catch (Exception e) {
                //BAD
            }
        });

        return dialog;
    }


}
