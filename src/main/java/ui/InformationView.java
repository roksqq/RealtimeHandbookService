package ui;

import extensions.CustomPair;
import org.apache.log4j.Logger;
import storage.Callable;
import viewModel.BookVewModel;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseEvent;

public class InformationView implements Callable {

    private final static Logger logger = Logger.getLogger(InformationView.class);

    private static int width = 150;

    private JPanel panel;
    private SectionTreeModel bookTreeModel;
    private SectionTreeModel chapterTreeModel;
    private BookVewModel bookVewModel;
    private GridBagConstraints constraints = new GridBagConstraints(0, 0,
                                                            1, 1,
                                                            0, 0.03,
                                                            GridBagConstraints.PAGE_START,
                                                            GridBagConstraints.BOTH,
                                                            new Insets(5, 5, 5, 5),
                                                            0, 0);


    InformationView(BookVewModel bookVewModel) {
        this.bookVewModel = bookVewModel;
        createPanel();
        addLabelsToPanel();
        createTreeView();
    }

    private void createPanel() {
        this.panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(width * 2, 600));
    }

    private void addLabelsToPanel() {
        JLabel books = new JLabel("-- Available books --", SwingConstants.CENTER);
        books.setPreferredSize(new Dimension(width,50));
        panel.add(books, constraints);

        JLabel chapters = new JLabel("----- Chapters -----", SwingConstants.CENTER);
        chapters.setPreferredSize(new Dimension(width,50));
        constraints.gridx = 1;
        panel.add(chapters, constraints);
    }

    private void createTreeView() {
        bookTreeModel = new SectionTreeModel();
        constraints.weighty= 0.97;

        JTree bookList = new JTree(bookTreeModel);
        bookList.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    super.mouseClicked(e);
                    DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) bookList.getLastSelectedPathComponent();
                    CustomPair<String, String> entry = (CustomPair<String, String>) clickedNode.getUserObject();
                    String bookUid = entry.getKey();
                    chapterTreeModel.removeAllChild();
                    requestBookChapters(bookUid);
                    bookVewModel.setActiveBookUid(bookUid);
                }
            }
        });
        bookList.setRootVisible(false);
        bookList.setShowsRootHandles(true);
        constraints.gridx = 0;
        constraints.gridy = 1;
        JScrollPane scrollingBookTreeView = new JScrollPane(bookList);
        scrollingBookTreeView.setPreferredSize(new Dimension(width,550));
        panel.add(scrollingBookTreeView, constraints);

        chapterTreeModel = new SectionTreeModel();
        JTree chapterLists = new JTree(chapterTreeModel);
        chapterLists.setRootVisible(false);
        chapterLists.setShowsRootHandles(true);
        constraints.gridx = 1;
        JScrollPane scrollingChapterTreeView = new JScrollPane(chapterLists);
        scrollingChapterTreeView.setPreferredSize(new Dimension(width,550));
        panel.add(scrollingChapterTreeView, constraints);
    }


    private void requestBookChapters(String bookUid){
        bookVewModel.getBookChapters(bookUid, this);
    }

    public void requestBookList() {
        bookVewModel.requestBookList(this);
    }

    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void completion(Object obj, String event) {
        logger.info("InformationView completion(" + obj + ", " + event + ")");
        CustomPair<String, String> entry = (CustomPair<String, String>)obj;
        switch (event) {
            case "books":
                bookTreeModel.addSection(entry);
                bookVewModel.addBookHash(entry);
                break;
            case "chapters":
                chapterTreeModel.addSection(entry);
                bookVewModel.addChapterHash(entry);
                break;
        }

    }

    @Override
    public void error(String error) {

    }

    @Override
    public void change(Object obj,  String event) {
        logger.info("InformationView change(" + obj + ", " + event + ")");
        CustomPair<String, String> newEntry = (CustomPair<String, String>)obj;
        String key = newEntry.getKey();
        switch (event) {
            case "books":
                String oldName = bookVewModel.getBookNameByKey(key);
                bookVewModel.updateBookHash(newEntry);
                CustomPair<String, String> oldEntry = new CustomPair<>(key, oldName);
                bookTreeModel.replaceSection(oldEntry, newEntry);
                break;
            case "chapters":
                bookVewModel.updateChapterHash(newEntry);
                break;
        }
    }

    @Override
    public void remove(Object obj, String event) {

    }
}