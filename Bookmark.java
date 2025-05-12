import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Bookmark extends JPanel {
    private int height = 100;
    private int width = Main.windowWidth;
    public int index;

    public String name;
    public MandelbrotPoint lower;
    public MandelbrotPoint upper;

    public JTextField nameText;
    public JButton deleteButton;
    public JButton viewButton;
    private Dimension size = new Dimension(width, height);
    public Bookmark(String name, MandelbrotPoint lower, MandelbrotPoint upper) {
        this.name = name;
        this.lower = lower;
        this.upper = upper;

        setLayout(new GridBagLayout());
        
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        GridBagConstraints nameConstraints = new GridBagConstraints();
        nameConstraints.fill = GridBagConstraints.HORIZONTAL;
        nameConstraints.gridx = 0;
        nameConstraints.gridy = 0;
        nameConstraints.gridwidth = 3;
        nameConstraints.weightx = 1.0;
        nameText = new JTextField(10);
        nameText.setFont(new Font("Ariel", Font.PLAIN, 20));
        nameText.setText(name);
        add(nameText, nameConstraints);

        GridBagConstraints boundsConstraints = new GridBagConstraints();
        boundsConstraints.fill = GridBagConstraints.HORIZONTAL;
        boundsConstraints.gridx = 3;
        boundsConstraints.gridy = 0;
        boundsConstraints.gridwidth = 5;
        boundsConstraints.weightx = 1.0;
        JLabel bounds = new JLabel();
        bounds.setFont(new Font("Ariel", Font.PLAIN, 12));
        bounds.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        bounds.setPreferredSize(new Dimension(100,100));
        bounds.setText("<html><body>Bounds:<br/>"+lower+"<br/>"+upper+"</body></html>");
        add(bounds, boundsConstraints);

        GridBagConstraints viewConstraints = new GridBagConstraints();
        viewConstraints.gridx = 8;
        viewConstraints.fill = GridBagConstraints.HORIZONTAL;
        viewConstraints.gridy = 0;
        viewConstraints.weightx = .5;
        viewConstraints.gridwidth = 1;
        viewButton = new JButton("View");
        viewButton.setFont(new Font("Ariel", Font.PLAIN, 20));
        add(viewButton, viewConstraints);

        GridBagConstraints deleteConstraints = new GridBagConstraints();
        deleteConstraints.gridx = 9;
        deleteConstraints.gridy = 0;
        deleteConstraints.weightx = .54;
        deleteConstraints.gridwidth = 1;
        deleteButton = new JButton();
        Dimension deleteButtonSize = new Dimension(height/2, height/2);
        try {
            BufferedImage baseImage = ImageIO.read(new File("img/delete.png"));
            Image scaled = baseImage.getScaledInstance(deleteButtonSize.width, deleteButtonSize.height, Image.SCALE_SMOOTH);
            deleteButton.setIcon(new ImageIcon(scaled));
        } catch (IOException e) {
            e.printStackTrace();
        }
        add(deleteButton, deleteConstraints);
    }
    public static Bookmark parseString(String s) {
        
        String[] split = s.split(" ");
        int len = split.length;
        String name = split[0];
        for (int i = 1; i < len - 2; i++) {
            if (i <= len - 3) {
                name += " ";
            }
            name += split[i];
        }
        MandelbrotPoint lower = MandelbrotPoint.parseString(split[len-2]);
        MandelbrotPoint upper = MandelbrotPoint.parseString(split[len-1]);

        return new Bookmark(name, lower, upper);
    }

    public String toString() {
        return name + " "+lower+" "+upper;
    }
 }