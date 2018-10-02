package net.sciencestudio.peakaboo.androidui.plot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import net.sciencestudio.autodialog.view.android.widget.SpinBox;
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


        SpinBox minEnergy = view.findViewById(R.id.min_energy);
        SpinBox maxEnergy = view.findViewById(R.id.max_energy);


//        NumberPicker minEnergy = view.findViewById(R.id.min_energy);
//        NumberPicker maxEnergy = view.findViewById(R.id.max_energy);
//
//        System.out.println("MinEnergy: " + AppState.controller.fitting().getMinEnergy());
//        System.out.println("MinEnergy: " + AppState.controller.fitting().getMaxEnergy());
//
        minEnergy.setMinValue(-1f);
        minEnergy.setMaxValue(1f);
        minEnergy.setValue(AppState.controller.fitting().getMinEnergy());
        minEnergy.setStep(0.01f);
        minEnergy.setOnValueChangeListener(newVal -> {
            try {
                AppState.controller.fitting().setMinEnergy(newVal);
            } catch (Exception e) {
                //BAD
            }
        });
//
//
        maxEnergy.setMinValue(0f);
        maxEnergy.setMaxValue(100f);
        maxEnergy.setValue(AppState.controller.fitting().getMaxEnergy());
        maxEnergy.setStep(0.01f);
        maxEnergy.setOnValueChangeListener(newVal -> {
            try {
                AppState.controller.fitting().setMaxEnergy(newVal);
            } catch (Exception e) {
                //BAD
            }
        });

        return dialog;
    }


}
