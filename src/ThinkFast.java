package src;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

// Classes and objects

// Inheritance

public class ThinkFast extends JFrame {

    // Encapsulation

    // ArrayList 

    private List<Task> tasks = new ArrayList<>();
    private List<Task> completedTasks = new ArrayList<>();
    private DefaultListModel<Task> listModel = new DefaultListModel<>();
    private JList<Task> taskList;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> viewComboBox;
    private DefaultComboBoxModel<String> viewComboBoxModel;

    private final Color DARK_RED = new Color(150, 40, 40);
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245);
    private final Color PAPER_LINE_COLOR = new Color(139, 0, 0);

    private ImageIcon createAppIcon() {

        // Exception Handling

        try {
            File iconFile = new File("./assets/icon.png");
            if (iconFile.exists()) {
                return new ImageIcon(iconFile.getAbsolutePath());
            }
            
            InputStream imgStream = getClass().getResourceAsStream("/icon.png");
            if (imgStream != null) {
                return new ImageIcon(ImageIO.read(imgStream));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public ThinkFast() {
        setTitle("ThinkFast");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        centerFrame();

        getContentPane().setBackground(WHITE);

        ImageIcon appIcon = createAppIcon();
        if (appIcon != null) {
            setIconImage(appIcon.getImage());
            
            try {
                Taskbar.getTaskbar().setIconImage(appIcon.getImage());
            } catch (Exception e) {
            }
        }

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(DARK_RED);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("ThinkFast: To-Do List");
        titleLabel.setForeground(WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel sortViewPanel = new JPanel(new BorderLayout());
        sortViewPanel.setBackground(WHITE);
        sortViewPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        sortPanel.setBackground(WHITE);
        sortComboBox = new JComboBox<>(new String[]{"Custom", "Due Date", "Title"});
        sortComboBox.setSelectedItem("Due Date");
        sortComboBox.addActionListener(e -> sortTasks());
        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(sortComboBox);

        JPanel viewPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        viewPanel.setBackground(WHITE);
        viewComboBoxModel = new DefaultComboBoxModel<>(new String[]{"Active Tasks", "Completed"});
        viewComboBox = new JComboBox<>(viewComboBoxModel);
        viewComboBox.addActionListener(e -> {
            if (viewComboBox.getSelectedIndex() == 1) {
                showCompletedTasks();
            } else {
                updateList();
            }
        });

        viewPanel.add(new JLabel("View:"));
        viewPanel.add(viewComboBox);

        sortViewPanel.add(sortPanel, BorderLayout.WEST);
        sortViewPanel.add(viewPanel, BorderLayout.EAST);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(headerPanel);
        topPanel.add(sortViewPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(LIGHT_GRAY);

        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskListRenderer());
        taskList.setBackground(LIGHT_GRAY);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setBorder(new EmptyBorder(0, 0, 0, 0));
        taskList.setFixedCellHeight(60);

        taskList.setDragEnabled(true);
        taskList.setDropMode(DropMode.INSERT);
        taskList.setTransferHandler(new ReorderListTransferHandler(listModel));

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(LIGHT_GRAY);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(LIGHT_GRAY);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(LIGHT_GRAY);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        leftButtonPanel.setBackground(LIGHT_GRAY);
        JButton markDoneButton = new JButton("Mark as Done");
        markDoneButton.setBackground(new Color(0, 128, 0));
        markDoneButton.setForeground(Color.WHITE);
        markDoneButton.setFocusPainted(false);
        markDoneButton.setBorderPainted(false);
        markDoneButton.setMargin(new Insets(10, 20, 10, 20));
        markDoneButton.setOpaque(true);      
        markDoneButton.addActionListener(e -> markTaskAsDone());
        leftButtonPanel.add(markDoneButton);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightButtonPanel.setBackground(LIGHT_GRAY);
        JButton addButton = createFloatingActionButton("+");
        JButton deleteButton = createIconButton("Delete", UIManager.getIcon("FileView.fileIcon"));
        JButton editButton = createIconButton("Edit", UIManager.getIcon("FileView.directoryIcon"));

        rightButtonPanel.add(editButton);
        rightButtonPanel.add(deleteButton);
        rightButtonPanel.add(addButton);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTasks();

        addButton.addActionListener(e -> showAddTaskDialog());
        deleteButton.addActionListener(e -> deleteTask());
        editButton.addActionListener(e -> editTask());

        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = taskList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    taskList.setSelectedIndex(index);
                }
            }
        });

        setVisible(true);
    }

    private void markTaskAsDone() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Select a task first!", 
                "Oopsie!", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task;
        if (viewComboBox.getSelectedIndex() == 0) {
            task = tasks.get(selectedIndex);
            tasks.remove(selectedIndex);
            completedTasks.add(task);
        } else {
            task = completedTasks.get(selectedIndex);
            completedTasks.remove(selectedIndex);
            tasks.add(task);
        }
        
        updateList();
        saveTasks();
        updateCompletedCount();
        
        if (viewComboBox.getSelectedIndex() == 1 && completedTasks.isEmpty()) {
            viewComboBox.setSelectedIndex(0);
        }
    }

    private void updateCompletedCount() {
        viewComboBoxModel.removeElementAt(1);
        viewComboBoxModel.insertElementAt("Completed", 1);
    }

    class ReorderListTransferHandler extends TransferHandler {
        private int fromIndex;

        public ReorderListTransferHandler(DefaultListModel<Task> model) {
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            fromIndex = taskList.getSelectedIndex();
            if (fromIndex < 0) return null;
            return new StringSelection(taskList.getSelectedValue().toString());
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            try {
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int toIndex = dl.getIndex();
                
                if (toIndex < 0 || toIndex > tasks.size()) {
                    toIndex = tasks.size();
                }
                
                if (fromIndex == toIndex) {
                    return true;
                }
                
                Task movedTask = tasks.get(fromIndex);
                tasks.remove(fromIndex);
                tasks.add(toIndex > fromIndex ? toIndex - 1 : toIndex, movedTask);
                
                updateList();
                taskList.setSelectedIndex(toIndex > fromIndex ? toIndex - 1 : toIndex);
                sortComboBox.setSelectedIndex(0);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void sortTasks() {
        String sortBy = (String) sortComboBox.getSelectedItem();
        if (sortBy == null) return;
        
        switch (sortBy) {
            case "Due Date":
                tasks.sort((t1, t2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Date d1 = t1.getDueDate().isEmpty() ? new Date(Long.MAX_VALUE) : sdf.parse(t1.getDueDate());
                        Date d2 = t2.getDueDate().isEmpty() ? new Date(Long.MAX_VALUE) : sdf.parse(t2.getDueDate());
                        return d1.compareTo(d2);
                    } catch (Exception e) {
                        return 0;
                    }
                });
                break;
            case "Title":
                tasks.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                break;
            case "Custom":
                break;
        }
        updateList();
    }

    private JButton createFloatingActionButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DARK_RED);
        button.setForeground(WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createIconButton(String text, Icon icon) {
        JButton button = new JButton(text, icon);
        button.setBackground(WHITE);
        button.setForeground(Color.DARK_GRAY);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private class TaskListRenderer extends JPanel implements ListCellRenderer<Task> {
        private JLabel titleLabel = new JLabel();
        private JLabel descLabel = new JLabel();
        private JLabel dateLabel = new JLabel();
        private JPanel contentPanel = new JPanel(new BorderLayout());
        private JPanel textPanel = new JPanel();
        private JButton markAsDone;
        private boolean isMarkHovered = false; 
        private int currentIndex = -1;

        public TaskListRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(10, 10, 10, 20));
            
            markAsDone = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int diameter = 20;
                    int x = (getWidth() - diameter) / 2;
                    int y = (getHeight() - diameter) / 2;
                    
                    if (isMarkHovered) {
                        g2d.setColor(new Color(200, 200, 200));
                        g2d.fillOval(x, y, diameter, diameter);
                    }
                    
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawOval(x, y, diameter, diameter);
                    
                    g2d.dispose();
                }
                
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(30, 30);
                }
                
                @Override
                public boolean contains(int x, int y) {
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int radius = 10;
                    return (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) <= radius * radius;
                }
            };

            markAsDone.setContentAreaFilled(false);
            markAsDone.setBorderPainted(false);
            markAsDone.setFocusPainted(false);
            markAsDone.setOpaque(false);
            markAsDone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            markAsDone.addMouseListener(new MouseAdapter() {
            @Override
                public void mouseEntered(MouseEvent e) {
                    isMarkHovered = true;
                    markAsDone.repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isMarkHovered = false;
                    markAsDone.repaint();
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentIndex >= 0 && currentIndex < listModel.size()) {
                        Task task = listModel.getElementAt(currentIndex);
                        if (tasks.contains(task)) {
                            tasks.remove(task);
                            completedTasks.add(task);
                        } else if (completedTasks.contains(task)) {
                            completedTasks.remove(task);
                            tasks.add(task);
                        }
                        updateList();
                        saveTasks();
                        updateCompletedCount();
                    }
                }
            });
            
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            descLabel.setForeground(Color.GRAY);
            
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
            textPanel.add(titleLabel);
            textPanel.add(descLabel);
            
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            dateLabel.setForeground(Color.GRAY);
            
            contentPanel.setOpaque(false);
            contentPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PAPER_LINE_COLOR));
            contentPanel.add(textPanel, BorderLayout.CENTER);
            contentPanel.add(dateLabel, BorderLayout.EAST);
            
            add(markAsDone, BorderLayout.WEST);
            add(contentPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task task, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            currentIndex = index;
            titleLabel.setText(task.getTitle());
            descLabel.setText(task.getDescription());
            descLabel.setToolTipText(task.getDescription());
            
            String dueDate = task.getDueDate();
            if (!dueDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse(dueDate);
                    SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d");
                    
                    Date today = new Date();
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(date);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(today);
                    
                    if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && 
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                        dateLabel.setText("Today");
                    } else if (date.before(today)) {
                        dateLabel.setText("Past");
                    } else {
                        dateLabel.setText(displayFormat.format(date));
                    }
                } catch (Exception e) {
                    dateLabel.setText(dueDate);
                }
            } else {
                dateLabel.setText("No Date");
            }
            
            if (isSelected) {
                setBackground(new Color(220, 220, 220));
            } else {
                setBackground(LIGHT_GRAY);
                titleLabel.setForeground(Color.BLACK);
            }

            markAsDone.setEnabled(list.isEnabled());
            
            return this;
        }

        private void updateCompletedCount() {
            viewComboBoxModel.removeElementAt(1);
            viewComboBoxModel.insertElementAt("Completed (" + completedTasks.size() + ")", 1);
            
            if (viewComboBox.getSelectedIndex() == 1) {
                showCompletedTasks();
            }
        }
    }

    private void showCompletedTasks() {
        listModel.clear();
        for (Task task : completedTasks) {
            listModel.addElement(task);
        }
    }

    private void centerFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    private void showAddTaskDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel titleLabel = new JLabel("Name:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField titleField = new JTextField(20);
        titleField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextArea descArea = new JTextArea(5, 20);
        descArea.setFont(new Font("Arial", Font.PLAIN, 16));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        
        JLabel dateLabel = new JLabel("Due Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField dateField = new JTextField(15);
        dateField.setFont(new Font("Arial", Font.PLAIN, 14));
        dateField.setToolTipText("Format: DD/MM/YYYY");
        
        dateField.setText("DD/MM/YYYY");
        dateField.setForeground(Color.GRAY);
        dateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (dateField.getText().equals("DD/MM/YYYY")) {
                    dateField.setText("");
                    dateField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (dateField.getText().isEmpty()) {
                    dateField.setForeground(Color.GRAY);
                    dateField.setText("DD/MM/YYYY");
                }
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(titleField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(descLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panel.add(descScrollPane, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        panel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dateField, gbc);
        
        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(this, "Add New Task");
        dialog.setPreferredSize(new Dimension(500, 350));
        dialog.pack();

        pane.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("value")) {
                if (pane.getValue() != null && pane.getValue().equals(JOptionPane.OK_OPTION)) {

                    // String Handling

                    String title = titleField.getText().trim();
                    String dateText = dateField.getText().trim();
                    
                    titleLabel.setForeground(Color.BLACK);
                    titleLabel.setText("Title:");
                    dateLabel.setForeground(Color.BLACK);
                    dateLabel.setText("Due Date:");
                    
                    boolean isValid = true;
                    StringBuilder errorMessage = new StringBuilder("Please finish:\n");
                    
                    if (title.isEmpty()) {
                        titleLabel.setForeground(Color.RED);
                        titleLabel.setText("Title: (required)");
                        errorMessage.append("- Title is required\n");
                        isValid = false;
                    }
                    
                    if (dateText.isEmpty() || dateText.equals("DD/MM/YYYY")) {
                        dateLabel.setForeground(Color.RED);
                        dateLabel.setText("Due Date: (required)");
                        errorMessage.append("- Due Date is required\n");
                        isValid = false;
                    } else {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            sdf.setLenient(false);
                            sdf.parse(dateText);
                        } catch (Exception ex) {
                            dateLabel.setForeground(Color.RED);
                            dateLabel.setText("Due Date: (invalid format)");
                            errorMessage.append("- Due Date must be in DD/MM/YYYY format\n");
                            isValid = false;
                        }
                    }
                    
                    if (!isValid) {
                        JOptionPane.showMessageDialog(
                            dialog,
                            errorMessage.toString(),
                            "Incomplete Information",
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    } else {
                        if (dateText.equals("DD/MM/YYYY")) {
                            dateText = "";
                        }
                        
                        Task task = new Task(
                            title,
                            descArea.getText(),
                            dateText
                        );
                        tasks.add(task);
                        sortTasks();
                        saveTasks();
                    }
                }
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Select a task first!", "Oopsie!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tasks.get(selectedIndex);
        
        JTextField titleField = new JTextField(task.getTitle(), 20);
        titleField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JTextArea descArea = new JTextArea(task.getDescription(), 5, 20);
        descArea.setFont(new Font("Arial", Font.PLAIN, 16));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        
        JTextField dateField = new JTextField(task.getDueDate(), 15);
        dateField.setFont(new Font("Arial", Font.PLAIN, 14));
        dateField.setToolTipText("Format: DD/MM/YYYY");
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(titleField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panel.add(descScrollPane, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        panel.add(new JLabel("Due Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dateField, gbc);
        
        JOptionPane pane = new JOptionPane(panel, 
            JOptionPane.PLAIN_MESSAGE, 
            JOptionPane.OK_CANCEL_OPTION);
        
        JDialog dialog = pane.createDialog(this, "Edit Task");
        dialog.setPreferredSize(new Dimension(500, 350));
        dialog.pack();

        pane.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("value")) {
                if (pane.getValue() != null && pane.getValue().equals(JOptionPane.OK_OPTION)) {
                    String title = titleField.getText().trim();
                    String dateText = dateField.getText().trim();
                    
                    boolean isValid = true;
                    StringBuilder errorMessage = new StringBuilder("Please fix:\n");
                    
                    if (title.isEmpty()) {
                        errorMessage.append("- Title is required\n");
                        isValid = false;
                    }
                    
                    if (!dateText.isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            sdf.setLenient(false);
                            sdf.parse(dateText);
                        } catch (Exception ex) {
                            errorMessage.append("- Due Date must be in DD/MM/YYYY format (or empty)\n");
                            isValid = false;
                        }
                    }
                    
                    if (!isValid) {
                        JOptionPane.showMessageDialog(
                            dialog,
                            errorMessage.toString(),
                            "Invalid Information",
                            JOptionPane.WARNING_MESSAGE
                        );
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    } else {
                        task.setTitle(title);
                        task.setDescription(descArea.getText());
                        task.setDueDate(dateText);
                        sortTasks();
                        saveTasks();
                    }
                }
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Select a task first!", 
                "Oopsie!", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task taskToDelete = tasks.get(selectedIndex);
        
        int response = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the task:\n" +
            "\"" + taskToDelete.getTitle() + "\"?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            tasks.remove(selectedIndex);
            updateList();
            saveTasks();
        }
    }

    private void updateList() {
        listModel.clear();
        for (Task task : tasks) {
            listModel.addElement(task);
        }
    }

    private void saveTasks() {

        // File Handling (File Input / Output)

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tasks_data.dat"))) {
            oos.writeObject(tasks);
            oos.writeObject(completedTasks);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTasks() {
        File file = new File("tasks_data.dat");
        if (!file.exists()) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            tasks = (List<Task>) ois.readObject();
            completedTasks = (List<Task>) ois.readObject();
            viewComboBoxModel.removeElementAt(1);
            viewComboBoxModel.insertElementAt("Completed (" + completedTasks.size() + ")", 1);
            updateList();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.setProperty("jdk.gtk.verbose", "false");
        System.setProperty("jdk.gtk.version", "2");

        new SplashScreen(2000);

        SwingUtilities.invokeLater(() -> {
            ThinkFast manager = new ThinkFast();
            manager.setVisible(true);
        });
    }
}

class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;
    private String dueDate;

    public Task(String title, String description, String dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    @Override
    public String toString() {
        return title;
    }
}

class JCalendar extends JPanel {
    private Calendar calendar = Calendar.getInstance();
    private JLabel monthLabel;
    private JButton[][] dayButtons = new JButton[6][7];

    public JCalendar() {
        setLayout(new BorderLayout());
        
        JPanel navPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("<");
        prevButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        
        JButton nextButton = new JButton(">");
        nextButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });
        
        monthLabel = new JLabel("", SwingConstants.CENTER);
        navPanel.add(prevButton, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        navPanel.add(nextButton, BorderLayout.EAST);
        add(navPanel, BorderLayout.NORTH);
        
        JPanel dayPanel = new JPanel(new GridLayout(6, 7));
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            dayPanel.add(new JLabel(day, SwingConstants.CENTER));
        }
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                dayButtons[i][j] = new JButton();
                dayButtons[i][j].setMargin(new Insets(0, 0, 0, 0));
                final int row = i;
                final int col = j;
                dayButtons[i][j].addActionListener(e -> {
                    String text = dayButtons[row][col].getText();
                    if (!text.isEmpty()) {
                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
                        firePropertyChange("calendar", null, calendar);
                    }
                });
                dayPanel.add(dayButtons[i][j]);
            }
        }
        add(dayPanel, BorderLayout.CENTER);
        
        updateCalendar();
    }
    
    private void updateCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        monthLabel.setText(sdf.format(calendar.getTime()));
        
        Calendar temp = (Calendar) calendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDay = temp.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        int day = 1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < firstDay - 1) {
                    dayButtons[i][j].setText("");
                } else if (day > daysInMonth) {
                    dayButtons[i][j].setText("");
                } else {
                    dayButtons[i][j].setText(String.valueOf(day));
                    day++;
                }
            }
        }
    }
    
    public Date getDate() {
        return calendar.getTime();
    }
}