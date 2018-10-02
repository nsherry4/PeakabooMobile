package net.sciencestudio.autodialog.view.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.sciencestudio.autodialog.view.android.support.DisplayHelper;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class SpinBox extends LinearLayout {
    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */

    private float step = 1f;
    private float value = 0f;
    private Float minValue, maxValue;

    private EditText text;
    private Button minus, plus;
    private DecimalFormat format = new DecimalFormat("0.00");

    private Context context;

    private Consumer<Float> onValueChangeListener = v -> {};

    public SpinBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpinBox(Context context) {
        super(context);
        init(context);
    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        int accentColour = typedValue.data;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(accentColour);

        float rad = Math.min(canvas.getHeight(), canvas.getWidth());
        rad /= 2f;
        canvas.drawRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), rad, rad, paint);
        System.out.println("ASDFASDF");


    }

    private void init(Context context) {
        this.context = context;

        //Match theme
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        int accentColour = typedValue.data;

//        super.setBackground(new ShapeDrawable(new RoundRectShape(
//                new float[]{50f, 50f, 5f, 5f, 5f, 5f, 5f, 5f},
//                null,
//                new float[]{50f, 50f, 5f, 5f, 5f, 5f, 5f, 5f}
//                )));
//        super.setClipToOutline(true);
        super.setBackgroundColor(0x00000000);


        text = new EditText(context);
        text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
        text.setLayoutParams(textParams);
        text.setBackgroundColor(0x00000000);
        text.setTextColor(0xffffffff);
        text.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);


        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println("CHANGED: " + s.toString());
                fromView();
            }
        });

        minus = new Button(context);
        minus.setText("-");
        minus.setOnClickListener(v -> this.minus());
        formatButton(minus);


        plus = new Button(context);
        plus.setText("+");
        plus.setOnClickListener(v -> this.plus());
        formatButton(plus);

        this.addView(minus);
        this.addView(text);
        this.addView(plus);



        fromModel();

    }

    public float getStep() {
        return step;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public DecimalFormat getFormat() {
        return format;
    }

    public void setFormat(DecimalFormat format) {
        this.format = format;
    }

    public void setOnValueChangeListener(Consumer<Float> onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float newValue) {
        //only emit an event if the value really did change
        if (newValue == value) {
            return;
        }
        setValueInternal(newValue);
        fromModel();

    }

    private void setValueInternal(float newValue) {
        if (newValue == value) {
            return;
        }
        this.value = newValue;
        onValueChangeListener.accept(newValue);
    }

    public Float getMinValue() {
        return minValue;
    }

    public void setMinValue(Float minValue) {
        this.minValue = minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
    }





    ///////////////////////////////////////////////////////
    // INTERNAL
    ///////////////////////////////////////////////////////

    private boolean minus() {
        adjust(-step);
        return true;
    }
    private boolean plus() {
        adjust(+step);
        return true;
    }

    private void adjust(float step) {
        float newValue = value+step;
        if (minValue != null) {
            newValue = Math.max(newValue, minValue);
        }
        if (maxValue != null) {
            newValue = Math.min(newValue, maxValue);
        }
        setValue(newValue);
    }

    private void fromModel() {
        text.setText(format.format(this.value));
    }

    private void fromView() {
        try {
            Float f = Float.parseFloat(text.getText().toString());
            if (validate(f)) {
                setValueInternal(f);
            } else {
                text.setError("Invalid");
            }
        } catch (NumberFormatException e) {
            text.setError("Invalid");
        }
    }

    private boolean validate(float value) {
        if (minValue != null) {
            if (value < minValue) return false;
        }
        if (maxValue != null) {
            if (value > maxValue) return false;
        }
        return true;
    }


    private void formatButton(Button button) {
        button.setTextColor(0xffffffff);
        button.setTextSize(DisplayHelper.dpToPixel(8, context));
        button.setMinimumWidth(DisplayHelper.dpToPixel(48, context));
        button.setWidth(DisplayHelper.dpToPixel(48, context));
        button.setBackgroundColor(0x00000000);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0f);
        button.setLayoutParams(params);
    }

}
