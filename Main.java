import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main {
    public static final int windowWidth = 1000;
    public static final int windowHeight = 750;

    private static int bookmarksIndex;
    private static int mandelbrotIndex;
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setPreferredSize(new Dimension(windowWidth, windowHeight));
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        
        

        JButton saveChanges = new JButton("Save Changes");
        saveChanges.setFont(new Font("Ariel", Font.BOLD, 35));
        saveChanges.setEnabled(false);
        saveChanges.setBackground(Color.GRAY);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);

        BookmarkManager bookmarkManager = new BookmarkManager(saveChanges, tabs);

        
        tabs.setFont(new Font("Ariel", Font.PLAIN, 20));
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabs.getTitleAt(tabs.getSelectedIndex()).equals(bookmarkManager.title)) {
                    bookmarksIndex = tabs.getSelectedIndex();
                } else {
                    mandelbrotIndex = tabs.getSelectedIndex();
                }

                if (tabs.getSelectedIndex() == mandelbrotIndex && !bookmarkManager.areThereChanges()) {
                    bookmarkManager.loadBookmarks();
                    bookmarkManager.disableButton();
                } else if (tabs.getSelectedIndex() == mandelbrotIndex && bookmarkManager.areThereChanges())  {
                    int result = JOptionPane.showConfirmDialog(frame, "You have unsaved changes. Switch tabs anyway?", "Unsaved changes", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        bookmarkManager.saveBookmarks();
                    } else {
                        tabs.setSelectedIndex(bookmarksIndex);
                        bookmarkManager.enableButton();
                    }
                } else {
                    bookmarkManager.loadBookmarks();
                    bookmarkManager.disableButton();
                }
            }
        });
        MandelbrotZoom mz = new MandelbrotZoom();

        tabs.addTab(mz.title, mz);

        JPanel managerPanel = new JPanel();
        managerPanel.setLayout(new BorderLayout());

        managerPanel.add(bookmarkManager, BorderLayout.CENTER);

        managerPanel.add(saveChanges, BorderLayout.SOUTH);

        tabs.addTab(bookmarkManager.title, managerPanel);

        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.pack();
        mz.getReady();
    }
}
