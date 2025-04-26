import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;

public class MandelbrotZoom extends JFrame {

    private int windowWidth = 1000;
    private int windowHeight = 750;

    private int maxIters = 200;

    private int lowerIter = 1;
    private int upperIter = 1000;

    private MandelbrotRenderer mRenderer;

    private ColorChooser colorfulChooser = (p) -> {
        int iters = p.iters(maxIters);
        if (iters == maxIters) return 0xFF000000;
        float smooth = iters + 1 - (float)(Math.log(Math.log(p.x*p.x + p.y*p.y)) / Math.log(2));
        float hue = (float) (0.95f + 10 * smooth / (float)maxIters) % 1.0f;
        return Color.getHSBColor(hue, 0.3f, 1f).getRGB();
    };

    public MandelbrotZoom() {
        
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setLayout(new BorderLayout());
        setResizable(false);

        mRenderer = new MandelbrotRenderer(maxIters);
        mRenderer.setColorChooser(colorfulChooser);
        add(mRenderer, BorderLayout.CENTER);
        
        JPanel sidePanel = new JPanel();
        
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        Button resetButton = new Button("Click Here to Reset Zoom");
        resetButton.setFont(new Font("Ariel", Font.PLAIN, 20));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mRenderer.resetZoom();
            }
        });

        
        Label iterLabel = new Label("Detail of fractal:");
        iterLabel.setFont(new Font("Ariel", Font.PLAIN, 15));
        iterLabel.setAlignment(Label.CENTER);

        JSlider iterSlider = new JSlider(lowerIter, upperIter, maxIters);
        iterSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!iterSlider.getValueIsAdjusting()) {
                    maxIters = iterSlider.getValue();
                    mRenderer.setMaxIters(maxIters);
                }
            }
        });

        Panel sliderPanel = new Panel();
        sliderPanel.setLayout(new GridLayout(2, 1));
        sliderPanel.add(iterLabel);
        sliderPanel.add(iterSlider);

        Label instructions = new Label("Drag a rectangle over the fractal to zoom. Press Ctrl+Z to zoom out.");
        instructions.setFont(new Font("Ariel", Font.BOLD, 20));
        instructions.setAlignment(Label.CENTER);


        add(instructions, BorderLayout.NORTH);

        Panel southPanel = new Panel();
        southPanel.setLayout(new GridLayout(1, 2));
        southPanel.add(resetButton);
        southPanel.add(sliderPanel);
        add(southPanel, BorderLayout.SOUTH);

        add(sidePanel, BorderLayout.WEST);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        pack();
        mRenderer.readyMandelbrot();

        JRootPane rootPane = getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl Z"), "zoomOut");

        rootPane.getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mRenderer.zoomOut();
            }
        });
    }
}