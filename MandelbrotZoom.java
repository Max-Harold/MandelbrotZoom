import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.Color;
import java.awt.Component;

public class MandelbrotZoom extends JPanel {

    final String prevDir = "prevDir";

    File lastUsedDirectory = null;

    private int maxIters = 200;

    private int lowerIter = 2;
    private int upperIter = 1000;

    public String title = "Mandelbrot Fractal";

    private MandelbrotRenderer mRenderer;

    private ColorChooser colorfulChooser = (p) ->  {
        int iters = p.iters(maxIters);
        if (iters == maxIters) return 0xFF000000;
        
        float hue = (float) (0.95f +  10 * (float)iters / (float)maxIters) % 1.0f;
        return Color.getHSBColor(hue, 0.3f, 1f).getRGB();
    };

    public MandelbrotZoom() {
        try {
            lastUsedDirectory = new File(Files.readString(Path.of(prevDir)));
        } catch (Exception e) {
            
        }
        setLayout(new BorderLayout());

        mRenderer = new MandelbrotRenderer(maxIters);
        mRenderer.setColorChooser(colorfulChooser);
        add(mRenderer, BorderLayout.CENTER);
        

        JButton resetButton = new JButton("Reset Zoom");
        resetButton.setFont(new Font("Ariel", Font.PLAIN, 20));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mRenderer.resetZoom();
            }
        });

        
        Label iterLabel = new Label("Detail of fractal:");
        iterLabel.setFont(new Font("Ariel", Font.PLAIN, 20));
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

        JButton saveImageButtom = new JButton("Save Image");
        saveImageButtom.setFont(new Font("Ariel", Font.PLAIN, 20));
        saveImageButtom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser folderChooser = new JFileChooser();
                folderChooser.setDialogTitle("Select a Folder");
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                folderChooser.setAcceptAllFileFilterUsed(false); // optional
                if (lastUsedDirectory != null) {
                    folderChooser.setCurrentDirectory(lastUsedDirectory);
                }

                int result = folderChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFolder = folderChooser.getSelectedFile();
                    lastUsedDirectory = selectedFolder;
                    try {
                        FileWriter writer = new FileWriter(prevDir);
                        writer.write(selectedFolder.getAbsolutePath());
                        writer.close();
                    } catch (IOException error) {
                        error.printStackTrace();
                    }
                    File outputFile = new File(selectedFolder, "Mandelbrot"+System.currentTimeMillis()+".png");
                    try {
                        ImageIO.write(mRenderer.renderedImage, "png", outputFile);
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }
            }
        });

        Label instructions = new Label("Drag a rectangle over the fractal to zoom. Press Ctrl+Z to zoom out.");
        instructions.setFont(new Font("Ariel", Font.BOLD, 20));
        instructions.setAlignment(Label.CENTER);


        add(instructions, BorderLayout.NORTH);

        JPanel bookmarkButtonPanel = new JPanel();
        bookmarkButtonPanel.setLayout(new BorderLayout());
        JButton bookmarkButton = new JButton("Bookmark");

        bookmarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(null, "Enter name for bookmark:");
                if (name == null) {
                    JOptionPane.showMessageDialog(null, "Bookmark not saved :(");
                } else {
                    BookmarkManager.addBookmark(name, mRenderer.getLowerBounds(), mRenderer.getUpperBounds());
                }
            }
        });

        bookmarkButton.setFont(new Font("Ariel", Font.PLAIN, 20));
        bookmarkButton.setOpaque(false);
        bookmarkButtonPanel.add(bookmarkButton);
        try {
            BufferedImage baseBookmarkImage = ImageIO.read(new File("img/bookmark.png"));
            
            bookmarkButton.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);

                    int w = bookmarkButton.getWidth();
                    int h = bookmarkButton.getHeight();

                    double widthOverHeight = (double)baseBookmarkImage.getWidth()/(double)baseBookmarkImage.getHeight();

                    if (w > 0 && h > 0) {
                        int newWidth = (int)((double)h * widthOverHeight);
                        Image scaled =  baseBookmarkImage.getScaledInstance(newWidth, h, Image.SCALE_SMOOTH);
                        bookmarkButton.setIcon(new ImageIcon(scaled));
                    }

                }
            });
            bookmarkButton.setPreferredSize(new Dimension(50, 50));
        } catch (IOException e) {
            e.printStackTrace();
        }   

        Panel southPanel = new Panel();
        southPanel.setLayout(new GridLayout(1, 3));
        southPanel.add(bookmarkButtonPanel);
        southPanel.add(resetButton);
        southPanel.add(sliderPanel);
        southPanel.add(saveImageButtom);
        add(southPanel, BorderLayout.SOUTH);
    }

    public void setBoundsToBookmark(Bookmark b) {
        mRenderer.setBoundsToBookmark(b);
        mRenderer.renderMandelbrot();
    }

    public void getReady() {
        mRenderer.readyMandelbrot();
        JRootPane rootPane = getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl Z"), "zoomOut");
        
        Component parent = getParent();
        final JTabbedPane[] tabs = {null};
        if (parent instanceof JTabbedPane) {
            tabs[0] = (JTabbedPane)parent;
        }
        rootPane.getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTabbedPane theTabs = tabs[0];
                if (tabs != null && theTabs.getTitleAt(theTabs.getSelectedIndex()).equals(title))
                    mRenderer.zoomOut();
            }
        });
    }
}