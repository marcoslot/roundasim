/**
 * 
 */
package dsg.roundagwt.gui;

import java.util.List;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.canvas.dom.client.Context2d.LineCap;
import com.google.gwt.canvas.dom.client.Context2d.LineJoin;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import dsg.rounda.gui.WorldScreenView;

/**
 * @author slotm
 *
 */
public class CanvasPainter {

    final Context2d g;
    
    /**
     * 
     */
    public CanvasPainter(Context2d g) {
        this.g = g;
    }

    public void drawLine(Coordinate from, Coordinate to, String color) {
        g.setStrokeStyle(color);
        g.beginPath();
        g.moveTo(from.x, from.y);
        g.lineTo(to.x, to.y);
        g.closePath();
        g.stroke();
    }

    public void drawLine(Coordinate from, Coordinate to, String color, WorldScreenView screenView) {
        from = screenView.toScreenCoord(from);
        to = screenView.toScreenCoord(to);
        drawLine(from, to, color);
    }

    public void drawCircle(Coordinate coord, String color, double radius, WorldScreenView screenView) {
        coord = screenView.toScreenCoord(coord);
        radius = screenView.toScreenWidth(radius);
        drawCircle(coord, color, radius);
        
    }

    public void drawCircle(Coordinate coord, String color, double radius) {
        g.setFillStyle(color);
        g.beginPath();
        g.arc(coord.x, coord.y, radius, 0, 2*Math.PI);
        g.closePath();
        g.fill();
    }

    public void drawLineString(LineString lineString, String style, double lineWidth) {
        drawLineString(lineString, style, lineWidth, null);
    }

    public void drawLineString(LineString lineString, String style, double lineWidth, WorldScreenView screenView) {
        Coordinate start = screenView != null ? screenView.toScreenCoord(lineString.getCoordinateN(0)) : lineString.getCoordinateN(0);

        g.setLineWidth(lineWidth);
        g.setStrokeStyle(style);
        g.beginPath();
        g.moveTo(start.x, start.y);

        for(int i = 1, len = lineString.getNumPoints(); i < len; i++) {
            Coordinate point = screenView != null ? screenView.toScreenCoord(lineString.getCoordinateN(i)) : lineString.getCoordinateN(i);
            g.lineTo(point.x, point.y);
        }

        g.stroke();
    }

    public void drawCoordinates(List<Coordinate> coordinates, String style, double lineWidth) {
       drawCoordinates(coordinates, style, lineWidth, null);
    }

    public void drawCoordinates(List<Coordinate> coordinates, String style, double lineWidth, WorldScreenView screenView) {
        Coordinate start = screenView != null ? screenView.toScreenCoord(coordinates.get(0)) : coordinates.get(0);

        g.setLineWidth(lineWidth);
        g.setStrokeStyle(style);
        g.beginPath();
        g.moveTo(start.x, start.y);

        for(int i = 1, len = coordinates.size(); i < len; i++) {
            Coordinate point = screenView != null ? screenView.toScreenCoord(coordinates.get(i)) : coordinates.get(i);
            g.lineTo(point.x, point.y);
        }

        g.stroke();
    }

    /**
     * @param x
     * @param y
     * @param radius
     * @param startAngle
     * @param endAngle
     * @see com.google.gwt.canvas.dom.client.Context2d#arc(double, double, double, double, double)
     */
    public final void arc(double x, double y, double radius, double startAngle,
            double endAngle) {
        g.arc(x, y, radius, startAngle, endAngle);
    }

    /**
     * @param x
     * @param y
     * @param radius
     * @param startAngle
     * @param endAngle
     * @param anticlockwise
     * @see com.google.gwt.canvas.dom.client.Context2d#arc(double, double, double, double, double, boolean)
     */
    public final void arc(double x, double y, double radius, double startAngle,
            double endAngle, boolean anticlockwise) {
        g.arc(x, y, radius, startAngle, endAngle, anticlockwise);
    }

    /**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param radius
     * @see com.google.gwt.canvas.dom.client.Context2d#arcTo(double, double, double, double, double)
     */
    public final void arcTo(double x1, double y1, double x2, double y2,
            double radius) {
        g.arcTo(x1, y1, x2, y2, radius);
    }

    /**
     * 
     * @see com.google.gwt.canvas.dom.client.Context2d#beginPath()
     */
    public final void beginPath() {
        g.beginPath();
    }

    /**
     * @param cp1x
     * @param cp1y
     * @param cp2x
     * @param cp2y
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#bezierCurveTo(double, double, double, double, double, double)
     */
    public final void bezierCurveTo(double cp1x, double cp1y, double cp2x,
            double cp2y, double x, double y) {
        g.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @see com.google.gwt.canvas.dom.client.Context2d#clearRect(double, double, double, double)
     */
    public final void clearRect(double x, double y, double w, double h) {
        g.clearRect(x, y, w, h);
    }

    /**
     * 
     * @see com.google.gwt.canvas.dom.client.Context2d#clip()
     */
    public final void clip() {
        g.clip();
    }

    /**
     * 
     * @see com.google.gwt.canvas.dom.client.Context2d#closePath()
     */
    public final void closePath() {
        g.closePath();
    }

    /**
     * @param imagedata
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createImageData(com.google.gwt.canvas.dom.client.ImageData)
     */
    public final ImageData createImageData(ImageData imagedata) {
        return g.createImageData(imagedata);
    }

    /**
     * @param w
     * @param h
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createImageData(int, int)
     */
    public final ImageData createImageData(int w, int h) {
        return g.createImageData(w, h);
    }

    /**
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createLinearGradient(double, double, double, double)
     */
    public final CanvasGradient createLinearGradient(double x0, double y0,
            double x1, double y1) {
        return g.createLinearGradient(x0, y0, x1, y1);
    }

    /**
     * @param image
     * @param repetition
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createPattern(com.google.gwt.dom.client.CanvasElement, com.google.gwt.canvas.dom.client.Context2d.Repetition)
     */
    public final CanvasPattern createPattern(CanvasElement image,
            Repetition repetition) {
        return g.createPattern(image, repetition);
    }

    /**
     * @param image
     * @param repetition
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createPattern(com.google.gwt.dom.client.CanvasElement, java.lang.String)
     */
    public final CanvasPattern createPattern(CanvasElement image,
            String repetition) {
        return g.createPattern(image, repetition);
    }

    /**
     * @param image
     * @param repetition
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createPattern(com.google.gwt.dom.client.ImageElement, com.google.gwt.canvas.dom.client.Context2d.Repetition)
     */
    public final CanvasPattern createPattern(ImageElement image,
            Repetition repetition) {
        return g.createPattern(image, repetition);
    }

    /**
     * @param image
     * @param repetition
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createPattern(com.google.gwt.dom.client.ImageElement, java.lang.String)
     */
    public final CanvasPattern createPattern(ImageElement image,
            String repetition) {
        return g.createPattern(image, repetition);
    }

    /**
     * @param x0
     * @param y0
     * @param r0
     * @param x1
     * @param y1
     * @param r1
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#createRadialGradient(double, double, double, double, double, double)
     */
    public final CanvasGradient createRadialGradient(double x0, double y0,
            double r0, double x1, double y1, double r1) {
        return g.createRadialGradient(x0, y0, r0, x1, y1, r1);
    }

    /**
     * @param image
     * @param dx
     * @param dy
     * @see com.google.gwt.canvas.dom.client.Context2d#drawImage(com.google.gwt.dom.client.CanvasElement, double, double)
     */
    public final void drawImage(CanvasElement image, double dx, double dy) {
        g.drawImage(image, dx, dy);
    }

    /**
     * @param image
     * @param dx
     * @param dy
     * @param dw
     * @param dh
     * @see com.google.gwt.canvas.dom.client.Context2d#drawImage(com.google.gwt.dom.client.CanvasElement, double, double, double, double)
     */
    public final void drawImage(CanvasElement image, double dx, double dy,
            double dw, double dh) {
        g.drawImage(image, dx, dy, dw, dh);
    }

    /**
     * @param image
     * @param sx
     * @param sy
     * @param sw
     * @param sh
     * @param dx
     * @param dy
     * @param dw
     * @param dh
     * @see com.google.gwt.canvas.dom.client.Context2d#drawImage(com.google.gwt.dom.client.CanvasElement, double, double, double, double, double, double, double, double)
     */
    public final void drawImage(CanvasElement image, double sx, double sy,
            double sw, double sh, double dx, double dy, double dw, double dh) {
        g.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    /**
     * @param image
     * @param dx
     * @param dy
     * @see com.google.gwt.canvas.dom.client.Context2d#drawImage(com.google.gwt.dom.client.ImageElement, double, double)
     */
    public final void drawImage(ImageElement image, double dx, double dy) {
        g.drawImage(image, dx, dy);
    }

    /**
     * @param image
     * @param dx
     * @param dy
     * @param dw
     * @param dh
     * @see com.google.gwt.canvas.dom.client.Context2d#drawImage(com.google.gwt.dom.client.ImageElement, double, double, double, double)
     */
    public final void drawImage(ImageElement image, double dx, double dy,
            double dw, double dh) {
        g.drawImage(image, dx, dy, dw, dh);
    }

    /**
     * @param image
     * @param sx
     * @param sy
     * @param sw
     * @param sh
     * @param dx
     * @param dy
     * @param dw
     * @param dh
     * @see com.google.gwt.canvas.dom.client.Context2d#drawImage(com.google.gwt.dom.client.ImageElement, double, double, double, double, double, double, double, double)
     */
    public final void drawImage(ImageElement image, double sx, double sy,
            double sw, double sh, double dx, double dy, double dw, double dh) {
        g.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    /**
     * 
     * @see com.google.gwt.canvas.dom.client.Context2d#fill()
     */
    public final void fill() {
        g.fill();
    }

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @see com.google.gwt.canvas.dom.client.Context2d#fillRect(double, double, double, double)
     */
    public final void fillRect(double x, double y, double w, double h) {
        g.fillRect(x, y, w, h);
    }

    /**
     * @param text
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#fillText(java.lang.String, double, double)
     */
    public final void fillText(String text, double x, double y) {
        g.fillText(text, x, y);
    }

    /**
     * @param text
     * @param x
     * @param y
     * @param maxWidth
     * @see com.google.gwt.canvas.dom.client.Context2d#fillText(java.lang.String, double, double, double)
     */
    public final void fillText(String text, double x, double y, double maxWidth) {
        g.fillText(text, x, y, maxWidth);
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getCanvas()
     */
    public final CanvasElement getCanvas() {
        return g.getCanvas();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getFillStyle()
     */
    public final FillStrokeStyle getFillStyle() {
        return g.getFillStyle();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getFont()
     */
    public final String getFont() {
        return g.getFont();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getGlobalAlpha()
     */
    public final double getGlobalAlpha() {
        return g.getGlobalAlpha();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getGlobalCompositeOperation()
     */
    public final String getGlobalCompositeOperation() {
        return g.getGlobalCompositeOperation();
    }

    /**
     * @param sx
     * @param sy
     * @param sw
     * @param sh
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getImageData(double, double, double, double)
     */
    public final ImageData getImageData(double sx, double sy, double sw,
            double sh) {
        return g.getImageData(sx, sy, sw, sh);
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getLineCap()
     */
    public final String getLineCap() {
        return g.getLineCap();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getLineJoin()
     */
    public final String getLineJoin() {
        return g.getLineJoin();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getLineWidth()
     */
    public final double getLineWidth() {
        return g.getLineWidth();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getMiterLimit()
     */
    public final double getMiterLimit() {
        return g.getMiterLimit();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getShadowBlur()
     */
    public final double getShadowBlur() {
        return g.getShadowBlur();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getShadowColor()
     */
    public final String getShadowColor() {
        return g.getShadowColor();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getShadowOffsetX()
     */
    public final double getShadowOffsetX() {
        return g.getShadowOffsetX();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getShadowOffsetY()
     */
    public final double getShadowOffsetY() {
        return g.getShadowOffsetY();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getStrokeStyle()
     */
    public final FillStrokeStyle getStrokeStyle() {
        return g.getStrokeStyle();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getTextAlign()
     */
    public final String getTextAlign() {
        return g.getTextAlign();
    }

    /**
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#getTextBaseline()
     */
    public final String getTextBaseline() {
        return g.getTextBaseline();
    }

    /**
     * @param x
     * @param y
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#isPointInPath(double, double)
     */
    public final boolean isPointInPath(double x, double y) {
        return g.isPointInPath(x, y);
    }

    /**
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#lineTo(double, double)
     */
    public final void lineTo(double x, double y) {
        g.lineTo(x, y);
    }

    /**
     * @param text
     * @return
     * @see com.google.gwt.canvas.dom.client.Context2d#measureText(java.lang.String)
     */
    public final TextMetrics measureText(String text) {
        return g.measureText(text);
    }

    /**
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#moveTo(double, double)
     */
    public final void moveTo(double x, double y) {
        g.moveTo(x, y);
    }

    /**
     * @param imagedata
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#putImageData(com.google.gwt.canvas.dom.client.ImageData, double, double)
     */
    public final void putImageData(ImageData imagedata, double x, double y) {
        g.putImageData(imagedata, x, y);
    }

    /**
     * @param cpx
     * @param cpy
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#quadraticCurveTo(double, double, double, double)
     */
    public final void quadraticCurveTo(double cpx, double cpy, double x,
            double y) {
        g.quadraticCurveTo(cpx, cpy, x, y);
    }

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @see com.google.gwt.canvas.dom.client.Context2d#rect(double, double, double, double)
     */
    public final void rect(double x, double y, double w, double h) {
        g.rect(x, y, w, h);
    }

    /**
     * 
     * @see com.google.gwt.canvas.dom.client.Context2d#restore()
     */
    public final void restore() {
        g.restore();
    }

    /**
     * @param angle
     * @see com.google.gwt.canvas.dom.client.Context2d#rotate(double)
     */
    public final void rotate(double angle) {
        g.rotate(angle);
    }

    /**
     * 
     * @see com.google.gwt.canvas.dom.client.Context2d#save()
     */
    public final void save() {
        g.save();
    }

    /**
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#scale(double, double)
     */
    public final void scale(double x, double y) {
        g.scale(x, y);
    }

    /**
     * @param fillStyle
     * @see com.google.gwt.canvas.dom.client.Context2d#setFillStyle(com.google.gwt.canvas.dom.client.FillStrokeStyle)
     */
    public final void setFillStyle(FillStrokeStyle fillStyle) {
        g.setFillStyle(fillStyle);
    }

    /**
     * @param fillStyleColor
     * @see com.google.gwt.canvas.dom.client.Context2d#setFillStyle(java.lang.String)
     */
    public final void setFillStyle(String fillStyleColor) {
        g.setFillStyle(fillStyleColor);
    }

    /**
     * @param f
     * @see com.google.gwt.canvas.dom.client.Context2d#setFont(java.lang.String)
     */
    public final void setFont(String f) {
        g.setFont(f);
    }

    /**
     * @param alpha
     * @see com.google.gwt.canvas.dom.client.Context2d#setGlobalAlpha(double)
     */
    public final void setGlobalAlpha(double alpha) {
        g.setGlobalAlpha(alpha);
    }

    /**
     * @param composite
     * @see com.google.gwt.canvas.dom.client.Context2d#setGlobalCompositeOperation(com.google.gwt.canvas.dom.client.Context2d.Composite)
     */
    public final void setGlobalCompositeOperation(Composite composite) {
        g.setGlobalCompositeOperation(composite);
    }

    /**
     * @param globalCompositeOperation
     * @see com.google.gwt.canvas.dom.client.Context2d#setGlobalCompositeOperation(java.lang.String)
     */
    public final void setGlobalCompositeOperation(
            String globalCompositeOperation) {
        g.setGlobalCompositeOperation(globalCompositeOperation);
    }

    /**
     * @param lineCap
     * @see com.google.gwt.canvas.dom.client.Context2d#setLineCap(com.google.gwt.canvas.dom.client.Context2d.LineCap)
     */
    public final void setLineCap(LineCap lineCap) {
        g.setLineCap(lineCap);
    }

    /**
     * @param lineCap
     * @see com.google.gwt.canvas.dom.client.Context2d#setLineCap(java.lang.String)
     */
    public final void setLineCap(String lineCap) {
        g.setLineCap(lineCap);
    }

    /**
     * @param lineJoin
     * @see com.google.gwt.canvas.dom.client.Context2d#setLineJoin(com.google.gwt.canvas.dom.client.Context2d.LineJoin)
     */
    public final void setLineJoin(LineJoin lineJoin) {
        g.setLineJoin(lineJoin);
    }

    /**
     * @param lineJoin
     * @see com.google.gwt.canvas.dom.client.Context2d#setLineJoin(java.lang.String)
     */
    public final void setLineJoin(String lineJoin) {
        g.setLineJoin(lineJoin);
    }

    /**
     * @param lineWidth
     * @see com.google.gwt.canvas.dom.client.Context2d#setLineWidth(double)
     */
    public final void setLineWidth(double lineWidth) {
        g.setLineWidth(lineWidth);
    }

    /**
     * @param miterLimit
     * @see com.google.gwt.canvas.dom.client.Context2d#setMiterLimit(double)
     */
    public final void setMiterLimit(double miterLimit) {
        g.setMiterLimit(miterLimit);
    }

    /**
     * @param shadowBlur
     * @see com.google.gwt.canvas.dom.client.Context2d#setShadowBlur(double)
     */
    public final void setShadowBlur(double shadowBlur) {
        g.setShadowBlur(shadowBlur);
    }

    /**
     * @param shadowColor
     * @see com.google.gwt.canvas.dom.client.Context2d#setShadowColor(java.lang.String)
     */
    public final void setShadowColor(String shadowColor) {
        g.setShadowColor(shadowColor);
    }

    /**
     * @param shadowOffsetX
     * @see com.google.gwt.canvas.dom.client.Context2d#setShadowOffsetX(double)
     */
    public final void setShadowOffsetX(double shadowOffsetX) {
        g.setShadowOffsetX(shadowOffsetX);
    }

    /**
     * @param shadowOffsetY
     * @see com.google.gwt.canvas.dom.client.Context2d#setShadowOffsetY(double)
     */
    public final void setShadowOffsetY(double shadowOffsetY) {
        g.setShadowOffsetY(shadowOffsetY);
    }

    /**
     * @param strokeStyle
     * @see com.google.gwt.canvas.dom.client.Context2d#setStrokeStyle(com.google.gwt.canvas.dom.client.FillStrokeStyle)
     */
    public final void setStrokeStyle(FillStrokeStyle strokeStyle) {
        g.setStrokeStyle(strokeStyle);
    }

    /**
     * @param strokeStyleColor
     * @see com.google.gwt.canvas.dom.client.Context2d#setStrokeStyle(java.lang.String)
     */
    public final void setStrokeStyle(String strokeStyleColor) {
        g.setStrokeStyle(strokeStyleColor);
    }

    /**
     * @param align
     * @see com.google.gwt.canvas.dom.client.Context2d#setTextAlign(java.lang.String)
     */
    public final void setTextAlign(String align) {
        g.setTextAlign(align);
    }

    /**
     * @param align
     * @see com.google.gwt.canvas.dom.client.Context2d#setTextAlign(com.google.gwt.canvas.dom.client.Context2d.TextAlign)
     */
    public final void setTextAlign(TextAlign align) {
        g.setTextAlign(align);
    }

    /**
     * @param baseline
     * @see com.google.gwt.canvas.dom.client.Context2d#setTextBaseline(java.lang.String)
     */
    public final void setTextBaseline(String baseline) {
        g.setTextBaseline(baseline);
    }

    /**
     * @param baseline
     * @see com.google.gwt.canvas.dom.client.Context2d#setTextBaseline(com.google.gwt.canvas.dom.client.Context2d.TextBaseline)
     */
    public final void setTextBaseline(TextBaseline baseline) {
        g.setTextBaseline(baseline);
    }

    /**
     * @param m11
     * @param m12
     * @param m21
     * @param m22
     * @param dx
     * @param dy
     * @see com.google.gwt.canvas.dom.client.Context2d#setTransform(double, double, double, double, double, double)
     */
    public final void setTransform(double m11, double m12, double m21,
            double m22, double dx, double dy) {
        g.setTransform(m11, m12, m21, m22, dx, dy);
    }

    /**
     * 
     * @see com.google.gwt.canvas.dom.client.Context2d#stroke()
     */
    public final void stroke() {
        g.stroke();
    }

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @see com.google.gwt.canvas.dom.client.Context2d#strokeRect(double, double, double, double)
     */
    public final void strokeRect(double x, double y, double w, double h) {
        g.strokeRect(x, y, w, h);
    }

    /**
     * @param text
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#strokeText(java.lang.String, double, double)
     */
    public final void strokeText(String text, double x, double y) {
        g.strokeText(text, x, y);
    }

    /**
     * @param text
     * @param x
     * @param y
     * @param maxWidth
     * @see com.google.gwt.canvas.dom.client.Context2d#strokeText(java.lang.String, double, double, double)
     */
    public final void strokeText(String text, double x, double y,
            double maxWidth) {
        g.strokeText(text, x, y, maxWidth);
    }

    /**
     * @param m11
     * @param m12
     * @param m21
     * @param m22
     * @param dx
     * @param dy
     * @see com.google.gwt.canvas.dom.client.Context2d#transform(double, double, double, double, double, double)
     */
    public final void transform(double m11, double m12, double m21, double m22,
            double dx, double dy) {
        g.transform(m11, m12, m21, m22, dx, dy);
    }

    /**
     * @param x
     * @param y
     * @see com.google.gwt.canvas.dom.client.Context2d#translate(double, double)
     */
    public final void translate(double x, double y) {
        g.translate(x, y);
    }

    public void fillPolygon(
            Polygon polygon, 
            String color,
            WorldScreenView screenView) {
        fillPolygon(
                polygon.getExteriorRing(),
                color,
                screenView);
    }

    public void fillPolygon(
            Polygon polygon, 
            String color) {
        fillPolygon(
                polygon.getExteriorRing(),
                color,
                null);
    }
    
    public void fillPolygon(
            LineString lines, 
            String color,
            WorldScreenView screenView) {
        fillPolygon(
                lines.getCoordinates(),
                color,
                screenView);
    }

    public void fillPolygon(
            LineString lines, 
            String color) {
        fillPolygon(
                lines.getCoordinates(),
                color,
                null);
    }

    public void fillPolygon(
            Coordinate[] coordinates, 
            String color,
            WorldScreenView screenView) {
        g.setFillStyle(color);
        g.beginPath();
        
        Coordinate coordinate = screenView != null ? screenView.toScreenCoord(coordinates[0]) : coordinates[0];
        
        g.moveTo(coordinate.x, coordinate.y);
        
        for(int i = 1, len = coordinates.length; i < len; i++) {
            coordinate = screenView != null ? screenView.toScreenCoord(coordinates[i]) : coordinates[i];
            
            g.lineTo(coordinate.x, coordinate.y);
        }
        
        g.closePath();
        g.fill();
    }

    public void fillPolygon(
            List<Coordinate> coordinates, 
            String color) {
        fillPolygon(
                coordinates,
                color,
                null);
    }
    
    public void fillPolygon(
            List<Coordinate> coordinates, 
            String color,
            WorldScreenView screenView) {
        g.setFillStyle(color);
        doPolygon(coordinates, screenView);
        g.fill();
    }

    public void strokePolygon(
            Polygon shape, 
            String color, 
            double lineWidth,
            WorldScreenView screenView) {
        g.setLineWidth(lineWidth);
        g.setStrokeStyle(color);
        doPolygon(shape.getExteriorRing().getCoordinates(), screenView);
        g.stroke();
    }

    private void doPolygon(
            List<Coordinate> coordinates,
            WorldScreenView screenView) {
        g.beginPath();
        
        moveTo(coordinates.get(0), screenView);
        
        for(int i = 1, len = coordinates.size(); i < len; i++) {
            lineTo(coordinates.get(i), screenView);
        }
        
        g.closePath();
    }

    private void doPolygon(
            Coordinate[] coordinates,
            WorldScreenView screenView) {
        g.beginPath();
        
        moveTo(coordinates[0], screenView);
        
        for(int i = 1, len = coordinates.length; i < len; i++) {
            lineTo(coordinates[i], screenView);
        }
        
        g.closePath();
    }

    private void lineTo(Coordinate coordinate, WorldScreenView screenView) {
        coordinate = screenView != null ? screenView.toScreenCoord(coordinate) : coordinate;
        g.lineTo(coordinate.x, coordinate.y);
    }

    private void moveTo(Coordinate coordinate, WorldScreenView screenView) {
        coordinate = screenView != null ? screenView.toScreenCoord(coordinate) : coordinate;
        g.moveTo(coordinate.x, coordinate.y);
    }

}
