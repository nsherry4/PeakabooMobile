package net.sciencestudio.scidraw.backend.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.io.IOException;
import java.io.OutputStream;

import scitypes.visualization.Buffer;
import scitypes.visualization.SaveableSurface;
import scitypes.visualization.Surface;
import scitypes.visualization.palette.PaletteColour;
import scitypes.visualization.template.SurfaceTemplate;

public class AndroidSurface implements SaveableSurface {

    protected Bitmap bm;
    private Canvas canvas;
    private Path path;
    private Paint paint;
    private CompositeModes compositeMode;

    public AndroidSurface(Bitmap bm) {
        this.bm = bm;
        this.canvas = new Canvas(bm);
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

    @Override
    public void arcTo(float x, float y, float width, float height, float angle, float extent) {
        path.arcTo(x, y, x+width, y+height, angle, angle+extent, false);
    }

    @Override
    public void addShape(SurfaceTemplate template) {
        template.apply(this   );
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
        canvas.drawText(s, x, y, paint);
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
    public void setLineJoin(LineJoin lineJoin) {
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
    public void setLineEnd(EndCap endCap) {
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
    public void setLineStyle(float width, EndCap endCap, LineJoin lineJoin) {
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
        return m.ascent + m.descent + m.leading;
    }

    @Override
    public float getFontLeading() {
        return paint.getFontMetrics().leading;
    }

    @Override
    public float getFontAscent() {
        return paint.getFontMetrics().ascent;
    }

    @Override
    public float getFontDescent() {
        return paint.getFontMetrics().descent;
    }

    @Override
    public void save() {
        canvas.save();
    }

    @Override
    public int saveWithMarker() {
        return canvas.save();
    }

    @Override
    public void restoreFromMarker(int i) {
        canvas.restoreToCount(i);
    }

    @Override
    public void restore() {
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
        canvas.drawBitmap(bm, x, y, paint);
    }

    @Override
    public void setAntialias(boolean b) {
        paint.setAntiAlias(b);
    }

    @Override
    public void setCompositeMode(CompositeModes compositeMode) {

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
    public CompositeModes getCompositeMode() {
        return compositeMode;
    }

    @Override
    public Surface getNewContextForSurface() {
        return new AndroidSurface(bm);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    }
}