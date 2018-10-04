package net.sciencestudio.peakaboo.androidui.plot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import net.sciencestudio.peakaboo.androidui.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import eventful.EventfulConfig;
import peakaboo.common.PeakabooLog;

public class LogViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);

        TextView logtext = findViewById(R.id.log_text);
        String logs = "";
        try {
            logs = new String(Files.readAllBytes(new File(PeakabooLog.getLogFilename()).toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logtext.setMovementMethod(new ScrollingMovementMethod());
        logtext.setText(logs);

    }
}
