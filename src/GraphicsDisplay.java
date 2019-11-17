import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.ArrayList;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {

    private Double[][] graphicsData;
    private ArrayList<Double> regions = new ArrayList<>();

    private static final double TURN_ANGLE = Math.PI/2;

    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showRegions = false;
    private byte turnCount = 0;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double scale = 1;

    private BasicStroke graphicsStroke;
    //длину штрихов и промежутков между штрихами — массив dash;
    // элементы массива с четными индексами задают длину штриха в пикселах,
    // элементы с нечетными индексами — длину промежутка;
    // массив перебирается циклически;
    float[] graphicsDash = new float[]{3, 1, 1, 1, 1, 1, 2, 1, 2, 1};
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;

    private Font axisFont;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, graphicsDash, 0.0f);

        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        findRegions();
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowRegions(boolean showRegions) {
        this.showRegions = showRegions;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }

        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);

        scale = Math.min(scaleX, scaleY);
        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
        Graphics2D canvas = (Graphics2D) g;
        if(turnCount != 0) paintTurn(canvas);
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);
        if (showRegions) paintRegions(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    protected void paintGraphics(Graphics2D canvas) {

        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();

        Point2D.Double point = xyToPoint(graphicsData[0][0], graphicsData[0][1]);
        graphics.moveTo(point.getX(), point.getY());
        for (int i = 1; i < graphicsData.length; i++) {
            point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            graphics.lineTo(point.getX(), point.getY());

        }
        canvas.draw(graphics);
    }

    protected boolean checkPoint(Double y) {
        StringBuffer str = new StringBuffer(y.toString());
        if (str.indexOf("e") != -1) str.delete(str.indexOf("e"), str.length());
        if (str.indexOf(".") != -1) {
            for (int i = str.length() - 1; str.charAt(i) == '0'; i--) {
                str.deleteCharAt(i);
            }
            str.deleteCharAt(str.indexOf("."));
        }
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i - 1) >= str.charAt(i)) return false;
        }
        return true;
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        for (Double[] point : graphicsData) {
            canvas.setPaint(Color.BLACK);
            canvas.setColor(Color.BLACK);

            Point2D.Double center = xyToPoint(point[0], point[1]);
            Line2D.Double line1 = new Line2D.Double(shiftPoint(center, -5, 0), shiftPoint(center, 5, 0));
            Line2D.Double line2 = new Line2D.Double(shiftPoint(center, 0, -5), shiftPoint(center, 0, 5));
            Line2D.Double line3 = new Line2D.Double(shiftPoint(center, -5, -5), shiftPoint(center, 5, 5));
            Line2D.Double line4 = new Line2D.Double(shiftPoint(center, -5, 5), shiftPoint(center, 5, -5));

            if (checkPoint(point[1])) {
                canvas.setPaint(Color.GREEN);
                canvas.setColor(Color.GREEN);
            }

            canvas.draw(line1);
            canvas.draw(line2);
            canvas.draw(line3);
            canvas.draw(line4);

            canvas.fill(line1);
            canvas.fill(line2);
            canvas.fill(line3);
            canvas.fill(line4);
        }
    }

    protected void paintRegions(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        canvas.setPaint(Color.BLACK);
        canvas.setColor(Color.BLACK);

        for (int itReg = 0; itReg < regions.size() - 1; itReg += 1) {
            GeneralPath region = new GeneralPath();
            Point2D.Double point = xyToPoint(regions.get(itReg), 0);
            region.moveTo(point.getX(), point.getY());

            int firstIndex = 0;
            for(; firstIndex < graphicsData.length - 1 && regions.get(itReg) > graphicsData[firstIndex][0]; firstIndex++);

            point = xyToPoint(graphicsData[firstIndex][0], graphicsData[firstIndex][1]);
            region.lineTo(point.getX(), point.getY());

            for (int i = firstIndex + 1; i < graphicsData.length && graphicsData[i][0] <= regions.get(itReg + 1); i++) {

                point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                region.lineTo(point.getX(), point.getY());
            }
            point = xyToPoint(regions.get(itReg + 1), 0);
            region.lineTo(point.getX(), point.getY());
            region.closePath();
            canvas.draw(region);
            canvas.fill(region);

            Double sq = calcSquare(itReg);


            Double maxHeight = 0.0;
            for (Double[] p : graphicsData){
                if (p[0] < regions.get(itReg)) continue;
                if (p[0] > regions.get(itReg + 1)) break;
                if(Math.abs(maxHeight) < Math.abs(p[1])) maxHeight = p[1];
            }
            maxHeight = xyToPoint(0, maxHeight).getY();
            Font regFont = new Font("TimesRoman", Font.BOLD, 13);
            canvas.setFont(regFont);
            Point2D.Double labelPos = xyToPoint((regions.get(itReg + 1) + regions.get(itReg))/2, 0);
            canvas.setPaint(Color.RED);
            Rectangle2D bounds = regFont.getStringBounds(String.format("%.2f", sq), canvas.getFontRenderContext());
            canvas.drawString(String.format("%.2f", sq), (float) (labelPos.getX() - bounds.getWidth()),
                    (float)  ((labelPos.getY() + maxHeight + Math.signum(maxHeight) * bounds.getHeight())) / 2 );
            canvas.setPaint(Color.BLACK);
            canvas.setColor(Color.BLACK);
        }

    }


    protected void findRegions() {
        regions.clear();
        if (graphicsData[0][1] == 0) regions.add(graphicsData[0][0]);
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] == 0) {
                regions.add(graphicsData[i][0]);
                continue;
            }
            if (graphicsData[i - 1][1] * graphicsData[i][1] < 0) {
                Double x = (graphicsData[i][0] * graphicsData[i - 1][1] - graphicsData[i - 1][0] * graphicsData[i][1])
                        /(graphicsData[i - 1][1] - graphicsData[i][1]);
                regions.add(x);
            }
        }
    }

    protected Double calcSquare(int j){
        Double sq = 0.0;
        int first = 0;
        for(; first < graphicsData.length && graphicsData[first][0] < regions.get(j); first++);
        if (graphicsData[first][1] != 0){
            sq += graphicsData[first][1]*(graphicsData[first][0] - regions.get(j));
        }
        int i;
        for(i = first + 1; i < graphicsData.length - 1 && graphicsData[i][0] <= regions.get(j + 1); i++) {
            sq += (graphicsData[i][1] + graphicsData[i - 1][1])*(graphicsData[i][0] - graphicsData[i - 1][0]);
        }
        i--;
        if (graphicsData[i][1] != 0){
            sq += graphicsData[i][1]*(regions.get(j + 1) - graphicsData[i][0]);
        }
        return Math.abs(sq/2);
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (minX <= 0.0 && maxX >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }
        if (minY <= 0.0 && maxY >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }

    public void turnLeft(){
        if(--turnCount == -4) turnCount = 0;
        repaint();
    }

    public void turnRight(){
        if(++turnCount == 4) turnCount = 0;
        repaint();
    }

    protected void paintTurn(Graphics2D canvas) {
        double angle = turnCount * TURN_ANGLE;
        Point2D center = xyToPoint(0,0);
        AffineTransform at = AffineTransform.getRotateInstance(angle, getSize().getWidth() / 2, getSize().getHeight() / 2);
        canvas.setTransform(at);
    }

    /* Метод-помощник, осуществляющий преобразование координат.
     * Оно необходимо, т.к. верхнему левому углу холста с координатами
     * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY), где
     * minX - это самое "левое" значение X, а
     * maxY - самое "верхнее" значение Y.
     */
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
}