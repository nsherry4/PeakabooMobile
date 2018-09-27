package net.sciencestudio.cyclops.visualization.backend.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

import cyclops.visualization.Buffer;
import cyclops.visualization.Surface;
import cyclops.visualization.palette.PaletteColour;

public abstract class AbstractAndroidSurface implements Surface {

    protected Canvas canvas;
    private Path path;
    private Paint paint;
    private CompositeModes compositeMode;
    private Map<Integer, Paint> paintSaveStack = new HashMap<>();

    public AbstractAndroidSurface(Canvas canvas) {
        this.canvas = canvas;
        path = new Path();
        paint = new Paint();

        setLineStyle(1f, EndCap.ROUND, LineJoin.ROUND);
        setAntialias(true);
        setCompositeMode(CompositeModes.OVER);
    }

    @Override
    public void lineTo(float x, float y) {
        path.lineTo(x, y);
    }

    @Override
    public void moveTo(float x, float y) {
        path.moveTo(x, y);
    }

    public void rectAt(float x, float y, float width, float height) {
        path.addRect(new RectF(x, y, x+width, y+height), Path.Direction.CW);
    }

    public void roundRectAt(float x, float y, float width, float height, float xradius, float yradius) {
        path.addRoundRect(new RectF(x, y, x+width, y+height), xradius, yradius, Path.Direction.CW);
    }


    @Override
    public void stroke() {
        strokePreserve();
        path = new Path();
    }

    @Override
    public void strokePreserve() {
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }

    @Override
    public void fill() {
        fillPreserve();
        path = new Path();
    }

    @Override
    public void fillPreserve() {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
    }

    @Override
    public void writeText(String s, float x, float y) {
        Paint.Style style = paint.getStyle();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(s, x, y, paint);
        paint.setStyle(style);
    }

    @Override
    public void setSource(int r, int g, int b) {
        setSource(new PaletteColour(255, r, g, b));
    }

    @Override
    public void setSource(int a, int r, int g, int b) {
        setSource(new PaletteColour(a, r, g, b));
    }

    @Override
    public void setSource(float r, float g, float b) {
        setSource(new PaletteColour(255, (int)(r*255f), (int)(g*255f), (int)(b*255f)));
    }

    @Override
    public void setSource(float a, float r, float g, float b) {
        setSource(new PaletteColour((int)(a*255f), (int)(r*255f), (int)(g*255f), (int)(b*255f)));
    }

    @Override
    public void setSource(PaletteColour paletteColour) {
        paint.setColor(paletteColour.getARGB());
    }

    @Override
    public void setSourceGradient(float x1, float y1, PaletteColour colour1, float x2, float y2, PaletteColour colour2) {
        paint.setShader(new LinearGradient(x1, y1, x2, y2, colour1.getARGB(), colour2.getARGB(), Shader.TileMode.CLAMP));
    }

    @Override
    public void setLineWidth(float w) {
        paint.setStrokeWidth(w);
    }

    @Override
    public void setLineJoin(Surface.LineJoin lineJoin) {
        switch (lineJoin) {

            case ROUND:
                paint.setStrokeJoin(Paint.Join.ROUND);
                break;
            case BEVEL:
                paint.setStrokeJoin(Paint.Join.BEVEL);
                break;
            case MITER:
                paint.setStrokeJoin(Paint.Join.MITER);
                break;
        }

    }

    @Override
    public void setLineEnd(Surface.EndCap endCap) {
        switch (endCap) {

            case BUTT:
                paint.setStrokeCap(Paint.Cap.BUTT);
                break;
            case ROUND:
                paint.setStrokeCap(Paint.Cap.ROUND);
                break;
            case SQUARE:
                paint.setStrokeCap(Paint.Cap.SQUARE);
                break;
        }
    }

    @Override
    public void setLineStyle(float width, Surface.EndCap endCap, Surface.LineJoin lineJoin) {
        setLineWidth(width);
        setLineEnd(endCap);
        setLineJoin(lineJoin);
    }

    @Override
    public float getTextWidth(String s) {
        Rect rect = new Rect();
        paint.getTextBounds(s, 0, s.length(), rect);
        return rect.width();
    }

    @Override
    public float getFontHeight() {
        Paint.FontMetrics m = paint.getFontMetrics();
        return -m.ascent + m.descent + m.leading;
    }

    @Override
    public float getFontLeading() {
        return paint.getFontMetrics().leading;
    }

    @Override
    public float getFontAscent() {
        return -paint.getFontMetrics().ascent;
    }

    @Override
    public float getFontDescent() {
        return paint.getFontMetrics().descent;
    }

    @Override
    public void save() {
        saveWithMarker();
    }

    @Override
    public int saveWithMarker() {
        int i = canvas.save();
        paintSaveStack.put(i, paint);
        paint = new Paint(paint);
        return i;
    }

    @Override
    public void restoreFromMarker(int i) {
        canvas.restoreToCount(i);
        paint = paintSaveStack.remove(i);
    }

    @Override
    public void restore() {
        //savecount seems to report 1 higher than the last saved marker
        paint = paintSaveStack.remove(canvas.getSaveCount()-1);
        canvas.restore();
    }

    @Override
    public void clip() {
        canvas.clipPath(path);
        path = new Path();
    }


    @Override
    public void translate(float dx, float dy) {
        canvas.translate(dx, dy);
    }

    @Override
    public void scale(float sx, float sy) {
        canvas.scale(sx, sy);
    }

    @Override
    public void rotate(float radians) {
        canvas.rotate((float)Math.toDegrees(radians));
    }

    @Override
    public void setFontSize(float v) {
        paint.setTextSize(v);
    }

    @Override
    public float getFontSize() {
        return paint.getTextSize();
    }

    @Override
    public void useMonoFont() {
        paint.setTypeface(Typeface.MONOSPACE);
    }

    @Override
    public void useSansFont() {
        paint.setTypeface(Typeface.SANS_SERIF);
    }

    @Override
    public void setFont(String s) {
        paint.setTypeface(Typeface.create(s, Typeface.NORMAL));
    }

    @Override
    public void setFontBold(boolean b) {
        paint.setFakeBoldText(b);
    }

    @Override
    public boolean isVectorSurface() {
        return false;
    }

    @Override
    public Buffer getImageBuffer(int x, int y) {
        return new BitmapBuffer(x, y);
    }

    @Override
    public void compose(Buffer buffer, int x, int y, float scale) {
        Bitmap bm = (Bitmap) buffer.getImageSource();
        canvas.drawBitmap(bm,
                new Rect(x, y, x+bm.getWidth(), y+bm.getHeight()),
                new Rect(x, y, (int)(x+bm.getWidth()*scale), (int)(y+bm.getHeight()*scale)),
                paint);
    }

    @Override
    public void setAntialias(boolean b) {
        paint.setAntiAlias(b);
    }

    @Override
    public void setCompositeMode(Surface.CompositeModes compositeMode) {

        this.compositeMode = compositeMode;

        switch (compositeMode) {

            case OVER:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                break;
            case OUT:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
                break;
            case IN:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                break;
            case ATOP:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                break;
            case SOURCE:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                break;
            case XOR:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
                break;
            case ADD:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
                break;
        }
    }

    @Override
    public Surface.CompositeModes getCompositeMode() {
        return compositeMode;
    }
}
