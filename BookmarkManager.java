import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BookmarkManager extends JScrollPane {

    public final String title = "Bookmarks";
    private JPanel bookmarks;
    private JButton saveButton;
    private ArrayList<Bookmark> bookmarkList = new ArrayList<>();
    private JTabbedPane tabs;

    private int mandelbrotIndex;
    private int bookmarksIndex;

    public BookmarkManager(JButton sButton, JTabbedPane tabs) {
        for (int tabIndex = 0; tabIndex < tabs.getTabCount(); tabIndex++) {
            if (tabs.getTitleAt(tabIndex).equals(title)) {
                bookmarksIndex = tabIndex;
            } else {
                mandelbrotIndex = tabIndex;
            }
        }
        saveButton = sButton;
        this.tabs = tabs;
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBookmarks();
                disableButton();
            }
        });

        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        bookmarks = new JPanel();
        bookmarks.setLayout(new BoxLayout(bookmarks, BoxLayout.Y_AXIS));
 
        setViewportView(bookmarks);

        loadBookmarks();
    }
    public static void addBookmark(String name, MandelbrotPoint lower, MandelbrotPoint upper) {
        try {
            FileWriter writer = new FileWriter("bookmarks", true);
            writer.write(name+" "+lower+" "+upper+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void addBookmark(Bookmark bookmark) {
        addBookmark(bookmark.name, bookmark.lower, bookmark.upper);
    }
    private void listenForBookmarkChanges(Bookmark b) {
        b.nameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                b.name = b.nameText.getText();
                enableButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                b.name = b.nameText.getText();
                enableButton();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        b.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookmarks.remove(b);
                bookmarkList.remove(b);
                revalidate();
                repaint();
                enableButton();
            }
        });
        BookmarkManager dis = this;
        b.viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (areThereChanges()) {
                    int result = JOptionPane.showConfirmDialog(dis, "You have unsaved changes. Switch tabs anyway?", "Unsaved changes", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        saveBookmarks();
                        tabs.setSelectedIndex(mandelbrotIndex);
                        zoomToBookmark(b);
                    }
                } else {
                    zoomToBookmark(b);
                }
            }
        });
    }
    public void zoomToBookmark(Bookmark b) {
        tabs.setSelectedIndex(mandelbrotIndex);
        MandelbrotZoom zoomer = (MandelbrotZoom)tabs.getComponentAt(mandelbrotIndex);
        zoomer.setBoundsToBookmark(b);
    }
    public boolean areThereChanges() {
        return saveButton.isEnabled();
    }
    public void enableButton() {
        saveButton.setBackground(Color.GREEN);
        saveButton.setEnabled(true);
    }
    public void disableButton() {
        saveButton.setBackground(Color.GRAY);
        saveButton.setEnabled(false);
    }
    public void loadBookmarks() {
        bookmarks.removeAll();
        bookmarkList.clear();
        try {
            List<String> lines = Files.readAllLines(Paths.get("bookmarks"));
            int index = 0;
            for (String line : lines) {
                Bookmark bookmark = Bookmark.parseString(line);
                listenForBookmarkChanges(bookmark);
                bookmark.index = index;
                bookmarks.add(bookmark);
                bookmarkList.add(bookmark);
            }
        } catch (Exception e) {
        }
    }
    public void saveBookmarks() {
        try {
            FileWriter writer = new FileWriter("bookmarks");
            for (Bookmark b : bookmarkList) {
                writer.append(b+"\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        disableButton();
    }
}
