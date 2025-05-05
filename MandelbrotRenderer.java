import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class MandelbrotRenderer extends JPanel {

    private final MandelbrotPoint defaultLowerBounds = new MandelbrotPoint(-2.0, -1.12);
    private final MandelbrotPoint defaultUpperBounds = new MandelbrotPoint(.47, 1.12); 

    private MandelbrotPoint lowerBounds = defaultLowerBounds;
    private MandelbrotPoint upperBounds = defaultUpperBounds;

    public int maxIters = 200;

    BufferedImage renderedImage;

    boolean isDrawingBox = false;
    Point boxStartPoint;
    Point boxEndPoint;
    BufferedImage boxImage;
    double boxRatio;

    private Stack<MandelbrotPoint[]> zoomStack = new Stack<>();

    private ColorChooser defaultChooser = (p) -> {
        int iters = p.iters(maxIters);
        if (iters == maxIters) return 0xFF000000;
        return 0xFFFFFFFF;
    };

    private ColorChooser colorChooser = defaultChooser;

    public MandelbrotRenderer(int maxIters) {
        this.maxIters = maxIters;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDrawingBox = true;
                boxStartPoint = e.getPoint();
                boxEndPoint = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawingBox = false;
                clearBox();
                if (Math.abs(boxStartPoint.x - boxEndPoint.x) > 0 && Math.abs(boxStartPoint.y - boxEndPoint.y) > 0) {
                    addToZoomStack();
                    zoom();
                }
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                boxEndPoint = e.getPoint();
                int deltaY = boxEndPoint.y - boxStartPoint.y;
                int deltaX = boxEndPoint.x - boxStartPoint.x;

                boxEndPoint.x = (int)(boxStartPoint.x + Math.signum(deltaX * deltaY) * boxRatio * (boxEndPoint.y - boxStartPoint.y));
                
                drawBox();
                repaint();
            }
        });
    }

    private void addToZoomStack() {
        zoomStack.push(new MandelbrotPoint[]{lowerBounds, upperBounds});
    }

    public void zoomOut() {
        if (zoomStack.size() > 0) {
            MandelbrotPoint[] newBounds = zoomStack.pop();
            lowerBounds = newBounds[0];
            upperBounds = newBounds[1];
            renderMandelbrot();
        }
    }

    public void setColorChooser(ColorChooser newColorChooser) {
        colorChooser = newColorChooser;
    }

    public void resetZoom() {
        lowerBounds = defaultLowerBounds;
        upperBounds = defaultUpperBounds;
        zoomStack.clear();
        renderMandelbrot();
    }

    private void zoom() {
        double boundsDeltaX = upperBounds.a - lowerBounds.a;
        double boundsDeltaY = upperBounds.b - lowerBounds.b;

        double startX = Math.min(boxStartPoint.x, boxEndPoint.x);
        double startY = Math.min(boxStartPoint.y, boxEndPoint.y);   
        double endX = Math.max(boxStartPoint.x, boxEndPoint.x);
        double endY = Math.max(boxStartPoint.y, boxEndPoint.y);   


        double a1 = lowerBounds.a + boundsDeltaX * startX / (double)getWidth();
        double b1 = lowerBounds.b + boundsDeltaY * startY / (double)getHeight();
        double a2 = lowerBounds.a + boundsDeltaX * endX / (double)getWidth();
        double b2 = lowerBounds.b + boundsDeltaY * endY / (double)getHeight();

        MandelbrotPoint newLowerBounds = new MandelbrotPoint(Math.min(a1, a2), Math.min(b1, b2));
        MandelbrotPoint newUpperBounds = new MandelbrotPoint(Math.max(a1,a2), Math.max(b1,b2));

        lowerBounds = newLowerBounds;
        upperBounds = newUpperBounds;


        renderMandelbrot();
    }

    public void setMaxIters(int maxIters) {
        this.maxIters = maxIters;
        renderMandelbrot();
    }

    private void clearBox() {
        Graphics2D g2d = (Graphics2D)boxImage.getGraphics();
        
        g2d.setComposite(AlphaComposite.Clear);
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawBox() {
        Graphics2D g2d = (Graphics2D)boxImage.getGraphics();

        clearBox();
        
        int boxWidth = Math.abs(boxEndPoint.x - boxStartPoint.x);
        int boxHeight = Math.abs(boxEndPoint.y - boxStartPoint.y);
        int startX = Math.min(boxStartPoint.x, boxEndPoint.x);
        int startY = Math.min(boxStartPoint.y, boxEndPoint.y);

        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.ORANGE);
        g2d.drawRect(startX, startY, boxWidth, boxHeight);
    }

    public void readyMandelbrot() {
        renderedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        boxImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        double boundsHeight = defaultUpperBounds.b - defaultLowerBounds.b;
        double xMidpoint = (defaultLowerBounds.a + defaultUpperBounds.a) / 2.0;
        boxRatio = (double)(getWidth()) / (double) (getHeight()); 
        double halfBoundsWidth = boundsHeight * boxRatio / 2.0;
        defaultLowerBounds.a = xMidpoint - halfBoundsWidth;
        defaultUpperBounds.a = xMidpoint + halfBoundsWidth;
        renderMandelbrot();
    }

    public void renderMandelbrot() {
        double w = getWidth();
        double h = getHeight();

        double deltaABounds = upperBounds.a - lowerBounds.a;
        double deltaBBounds = upperBounds.b - lowerBounds.b;
        double lowA = lowerBounds.a;
        double lowB = lowerBounds.b;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                MandelbrotPoint point = new MandelbrotPoint(lowA + (x / w) * deltaABounds, lowB + (y / h) * deltaBBounds);
                renderedImage.setRGB(x, y, colorChooser.colorForPoint(point));
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        g2d.drawImage(renderedImage, 0, 0, renderedImage.getWidth(), renderedImage.getHeight(), this);
        if (isDrawingBox) {
            g2d.drawImage(boxImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public void setBoundsToBookmark(Bookmark b) {
        lowerBounds = b.lower;
        upperBounds = b.upper;
    }

    public MandelbrotPoint getLowerBounds() {
        return lowerBounds;
    }
    public MandelbrotPoint getUpperBounds() {
        return upperBounds;
    }
}
