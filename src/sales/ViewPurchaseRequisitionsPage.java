package sales;

import admin.UIBase;
import database.DatabaseHelper;
import models.PurchaseRequisition;
import models.User;
import models.SystemLog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ViewPurchaseRequisitionsPage extends UIBase {

    private final User currentUser;
    private JTable requisitionsTable;
    private DefaultTableModel tableModel;
    private List<PurchaseRequisition> requisitionsList;
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public ViewPurchaseRequisitionsPage(User user) {
        super("View Purchase Requisitions");
        this.currentUser = user;
        requisitionsList = new ArrayList<>();
        initTableModel();
    }

    private void initTableModel() {
        String[] columnNames = {"PR ID", "Item Code", "Item Name", "Quantity", "Required Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3) {
                    return Integer.class;
                }
                return String.class;
            }
        };
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            loadRequisitions();
        }
        super.setVisible(visible);
    }


    public void loadRequisitions() {
    try {
        DatabaseHelper dbHelper = new DatabaseHelper();
        requisitionsList = dbHelper.getAllPurchaseRequisitions();

        if (requisitionsList == null) {
            requisitionsList = new ArrayList<>();
        }

        System.out.println("Loaded " + requisitionsList.size() + " requisitions");

        if (currentUser != null) {
            System.out.println("Current user: " + currentUser.getUsername() + ", ID: " + currentUser.getUserId() + ", Role: " + currentUser.getRole());
        } else {
            System.out.println("Current user is null");
        }

        filterRequisitions(null);
        
        // Force refresh the UI to update username display
        SwingUtilities.invokeLater(() -> {
            repaint();
            revalidate();
        });

    } catch (IOException ex) {
        ex.printStackTrace();
        requisitionsList = new ArrayList<>();
        JOptionPane.showMessageDialog(this,
                "Error loading requisitions: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        ex.printStackTrace();
        requisitionsList = new ArrayList<>();
        JOptionPane.showMessageDialog(this,
                "Unexpected error: " + ex.getMessage(),
                "System Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    

    private void filterRequisitions(String status) {
    if (tableModel == null) {
        String[] columnNames = {"PR ID", "Item Code", "Item Name", "Quantity", "Required Date", "Supplier Code", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3) {
                    return Integer.class;
                }
                return String.class;
            }
        };
    }

    tableModel.setRowCount(0);

    if (requisitionsList == null) {
        return;
    }

    System.out.println("Loading all requisitions with supplier codes - list size: " + requisitionsList.size());

    for (PurchaseRequisition requisition : requisitionsList) {
        if (requisition != null) {
            try {
                // Get item name if not set
                String itemName = requisition.getItemName();
                if (itemName == null || itemName.isEmpty()) {
                    try {
                        DatabaseHelper db = new DatabaseHelper();
                        models.Item item = db.getItemByCode(requisition.getItemCode());
                        if (item != null) {
                            itemName = item.getItemName();
                        } else {
                            itemName = "";
                        }
                    } catch (Exception e) {
                        itemName = "";
                    }
                }

                // FIXED: Get supplier code by looking up the item
                String supplierCode = "";
                try {
                    DatabaseHelper db = new DatabaseHelper();
                    models.Item item = db.getItemByCode(requisition.getItemCode());
                    if (item != null) {
                        supplierCode = item.getSupplierId();
                    } else {
                        supplierCode = "N/A";
                    }
                } catch (Exception e) {
                    supplierCode = "N/A";
                    System.err.println("Error fetching supplier code for requisition " + requisition.getRequisitionId() + ": " + e.getMessage());
                }

                // Format date safely
                String dateStr = "";
                LocalDate requiredDate = requisition.getRequiredDate();
                if (requiredDate != null) {
                    try {
                        dateStr = requiredDate.format(displayDateFormatter);
                    } catch (Exception e) {
                        dateStr = requiredDate.toString();
                    }
                }

                // FIXED: Added supplier code to the row data
                Object[] rowData = {
                        requisition.getRequisitionId(),
                        requisition.getItemCode(),
                        itemName,
                        requisition.getQuantity(),
                        dateStr,
                        supplierCode, // This was missing!
                        requisition.getStatus()
                };
                tableModel.addRow(rowData);

                System.out.println("Added requisition: " + requisition.getRequisitionId() + " with supplier: " + supplierCode);
            } catch (Exception e) {
                System.out.println("Error adding row for requisition: " +
                        (requisition.getRequisitionId() != null ? requisition.getRequisitionId() : "unknown") +
                        " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    System.out.println("Table now has " + tableModel.getRowCount() + " rows");

    if (requisitionsTable != null) {
        requisitionsTable.revalidate();
        requisitionsTable.repaint();
    }
}

    

    @Override
    protected void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        JPanel topBar = createTopBar();
        root.add(topBar, BorderLayout.NORTH);

        JPanel contentPanel = createContentPanel();
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, APP_WINDOW_HEIGHT));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel placeholder = new JLabel("OWSB", SwingConstants.CENTER);
        placeholder.setFont(new Font("Serif", Font.BOLD, 16));
        placeholder.setForeground(new Color(11, 61, 145));
        placeholder.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        logoPanel.add(placeholder, BorderLayout.CENTER);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JPanel dashboardItem = createMenuItem("Dashboard", false);
        dashboardItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        JPanel requisitionsItem = createMenuItem("View Requisitions", true);

        menuPanel.add(dashboardItem);
        menuPanel.add(requisitionsItem);
        menuPanel.add(Box.createVerticalGlue());

        sidebar.add(menuPanel, BorderLayout.CENTER);

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(120, 120, 120));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        logoutBtn.setPreferredSize(new Dimension(120, 35));
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(120, 120, 120), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutBtn.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                    ViewPurchaseRequisitionsPage.this,
                    "Are you sure you want to log out?",
                    "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (response == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        logoutPanel.add(logoutBtn);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private String getDisplayName() {
    System.out.println("=== getDisplayName Debug ===");
    System.out.println("currentUser: " + currentUser);
    
    if (currentUser != null) {
        String username = currentUser.getUsername();
        System.out.println("Username: '" + username + "'");
        
        if (username != null && !username.trim().isEmpty()) {
            System.out.println("Returning username: " + username.trim());
            return username.trim();
        }
        
        String userId = currentUser.getUserId();
        System.out.println("Username empty, trying userId: " + userId);
        if (userId != null && !userId.trim().isEmpty()) {
            return userId.trim();
        }
    }
    
    System.out.println("Returning default: User");
    return "User";
}



    

    // Replace your createTopBar method with this:

private JPanel createTopBar() {
    JPanel topContainer = new JPanel(new BorderLayout());
    topContainer.setBackground(Color.WHITE);

    JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    userPanel.setBackground(new Color(180, 180, 180));
    userPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 20));

    JLabel bell = new JLabel("🔔");
    bell.setFont(new Font("SansSerif", Font.PLAIN, 16));
    bell.setCursor(new Cursor(Cursor.HAND_CURSOR));
    userPanel.add(bell);

    // Create the user label but set text later
    JLabel userLabel = new JLabel("User ▾");
    userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    userPanel.add(userLabel);

    // Update the username after a short delay to ensure currentUser is set
    SwingUtilities.invokeLater(() -> {
        if (currentUser != null && currentUser.getUsername() != null) {
            userLabel.setText(currentUser.getUsername().trim() + " ▾");
        }
    });

    userLabel.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            dispose();
            new admin.MyProfilePage(currentUser).setVisible(true);
        }
    });

    topContainer.add(userPanel, BorderLayout.NORTH);

    JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    headerPanel.setBackground(Color.WHITE);
    headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
    ));

    JLabel title = new JLabel("View Purchase Requisitions");
    title.setFont(new Font("Serif", Font.BOLD, 28));
    title.setForeground(new Color(11, 61, 145));

    headerPanel.add(title);

    topContainer.add(headerPanel, BorderLayout.SOUTH);

    return topContainer;
}

    

    private JPanel createMenuItem(String text, boolean isSelected) {
        JPanel menuItem = new JPanel(new BorderLayout());
        menuItem.setBackground(isSelected ? new Color(230, 230, 230) : Color.WHITE);
        menuItem.setMaximumSize(new Dimension(200, 50));
        menuItem.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel menuLabel = new JLabel(text);
        menuLabel.setFont(new Font("Serif", Font.BOLD, 16));
        menuItem.add(menuLabel, BorderLayout.CENTER);

        return menuItem;
    }

    private JPanel createContentPanel() {
    JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
    contentPanel.setBackground(Color.WHITE);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Make sure we have a table model
    if (tableModel == null) {
        String[] columnNames = {"PR ID", "Item Code", "Item Name", "Quantity", "Required Date", "Supplier Code", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3) { // Quantity column
                    return Integer.class;
                }
                return String.class;
            }
        };
    }

    // Create table with the model
    requisitionsTable = new JTable(tableModel);
    requisitionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    requisitionsTable.getTableHeader().setBackground(new Color(240, 240, 240));
    requisitionsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    requisitionsTable.setRowHeight(30);
    requisitionsTable.setGridColor(Color.LIGHT_GRAY);

    // Set column widths to ensure visibility
    requisitionsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PR ID
    requisitionsTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Item Code
    requisitionsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Item Name
    requisitionsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Quantity
    requisitionsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Required Date
    requisitionsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Supplier Code
    requisitionsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status

    // Add color coding for different status values
    requisitionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                try {
                    if (column == 6) { // Status column
                        String status = value != null ? value.toString() : "";
                        if ("Approved".equals(status)) {
                            c.setBackground(new Color(230, 255, 230)); // Light green
                            c.setForeground(new Color(0, 100, 0));     // Dark green
                        } else if ("Rejected".equals(status)) {
                            c.setBackground(new Color(255, 230, 230)); // Light red
                            c.setForeground(new Color(180, 0, 0));     // Dark red
                        } else { // Pending
                            c.setBackground(new Color(255, 255, 230)); // Light yellow
                            c.setForeground(new Color(180, 100, 0));   // Orange/brown
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } catch (Exception e) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            }
            return c;
        }
    });

    // Put the table in a scroll pane
    JScrollPane scrollPane = new JScrollPane(requisitionsTable);
    scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    scrollPane.setPreferredSize(new Dimension(700, 400));
    contentPanel.add(scrollPane, BorderLayout.CENTER);

    // FIXED: Consistent button colors (grey tone like other pages)
    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
    buttonsPanel.setBackground(Color.WHITE);
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

    // Edit Button - using consistent grey color
    JButton editButton = new JButton("Edit Requisition");
    styleButton(editButton);
    // Remove the custom color to use the default grey from styleButton
    editButton.addActionListener(e -> handleEditRequisition());
    buttonsPanel.add(editButton);

    // Delete Button - using consistent grey color
    JButton deleteButton = new JButton("Delete Requisition");
    styleButton(deleteButton);
    // Remove the custom color to use the default grey from styleButton
    deleteButton.addActionListener(e -> handleDeleteRequisition());
    buttonsPanel.add(deleteButton);

    contentPanel.add(buttonsPanel, BorderLayout.SOUTH);
    return contentPanel;
}

  


  


    private void styleButton(JButton button) {
        button.setBackground(new Color(120, 120, 120));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));
    }

    private void goBackToDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            SalesDashboardPage dashboard = new SalesDashboardPage(currentUser);
            dashboard.setVisible(true);
        });
    }

    private void logSystemAction(String action, String details) {
        try {
            if (currentUser == null) return;

            DatabaseHelper db = new DatabaseHelper();
            SystemLog log = new SystemLog(
                    "LOG" + System.currentTimeMillis(),
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    action,
                    details,
                    LocalDateTime.now(),
                    currentUser.getRole()
            );
            db.addSystemLog(log);
        } catch (IOException e) {
            System.out.println("Failed to log system action: " + e.getMessage());
        }
    }

    private void handleEditRequisition() {
    int selectedRow = requisitionsTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this,
                "Please select a requisition to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        String requisitionId = (String) requisitionsTable.getValueAt(selectedRow, 0);
        String itemCode = (String) requisitionsTable.getValueAt(selectedRow, 1);
        String itemName = (String) requisitionsTable.getValueAt(selectedRow, 2);
        int currentQuantity = (Integer) requisitionsTable.getValueAt(selectedRow, 3);
        String requiredDate = (String) requisitionsTable.getValueAt(selectedRow, 4);
        String status = (String) requisitionsTable.getValueAt(selectedRow, 6);

        // Check if requisition can be edited (only pending ones)
        if (!"Pending".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Only pending requisitions can be edited.\nThis requisition is: " + status,
                    "Cannot Edit",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create edit form
        JPanel editPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        editPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField itemCodeField = new JTextField(itemCode);
        JTextField quantityField = new JTextField(String.valueOf(currentQuantity));
        JTextField dateField = new JTextField(requiredDate);

        editPanel.add(new JLabel("Requisition ID:"));
        editPanel.add(new JLabel(requisitionId)); // Read-only
        editPanel.add(new JLabel("Item Code:"));
        editPanel.add(itemCodeField);
        editPanel.add(new JLabel("Item Name:"));
        editPanel.add(new JLabel(itemName)); // Read-only
        editPanel.add(new JLabel("Quantity:"));
        editPanel.add(quantityField);
        editPanel.add(new JLabel("Required Date (dd-MM-yyyy):"));
        editPanel.add(dateField);

        int result = JOptionPane.showConfirmDialog(this, editPanel, 
                "Edit Purchase Requisition", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String newItemCode = itemCodeField.getText().trim();
                int newQuantity = Integer.parseInt(quantityField.getText().trim());
                String newDateStr = dateField.getText().trim();

                if (newItemCode.isEmpty() || newQuantity <= 0) {
                    JOptionPane.showMessageDialog(this, 
                            "Item code cannot be empty and quantity must be positive.", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Parse and validate date
                LocalDate newRequiredDate;
                try {
                    newRequiredDate = LocalDate.parse(newDateStr, displayDateFormatter);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Invalid date format. Please use dd-MM-yyyy format.", 
                            "Date Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update the requisition
                DatabaseHelper dbHelper = new DatabaseHelper();
                PurchaseRequisition requisition = dbHelper.getPurchaseRequisitionById(requisitionId);
                
                if (requisition != null) {
                    requisition.setItemCode(newItemCode);
                    requisition.setQuantity(newQuantity);
                    requisition.setRequiredDate(newRequiredDate);
                    
                    // Update item name if item code changed
                    try {
                        models.Item item = dbHelper.getItemByCode(newItemCode);
                        if (item != null) {
                            requisition.setItemName(item.getItemName());
                        }
                    } catch (Exception ex) {
                        // Keep old item name if lookup fails
                    }

                    dbHelper.updatePurchaseRequisition(requisition);
                    
                    JOptionPane.showMessageDialog(this,
                            "Requisition updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    logSystemAction(SystemLog.ACTION_UPDATE, "Updated purchase requisition: " + requisitionId);
                    loadRequisitions(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Requisition not found in database.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number for quantity.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error updating requisition: " + ex.getMessage(),
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Error editing requisition: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

private void updateUserDisplay() {
    if (currentUser != null) {
        // Find the user label and update it
        Container parent = getContentPane();
        updateUserLabelRecursive(parent);
        repaint();
    }
}

private void updateUserLabelRecursive(Container container) {
    for (Component comp : container.getComponents()) {
        if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            String text = label.getText();
            if (text != null && text.contains("▾")) {
                // This is likely the user label
                String newDisplayName = getDisplayName();
                label.setText(newDisplayName + " ▾");
                System.out.println("Updated user label to: " + newDisplayName);
            }
        } else if (comp instanceof Container) {
            updateUserLabelRecursive((Container) comp);
        }
    }
}



// FIXED: Added Delete Requisition functionality
private void handleDeleteRequisition() {
    int selectedRow = requisitionsTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this,
                "Please select a requisition to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        String requisitionId = (String) requisitionsTable.getValueAt(selectedRow, 0);
        String itemName = (String) requisitionsTable.getValueAt(selectedRow, 2);
        String status = (String) requisitionsTable.getValueAt(selectedRow, 6);

        // Check if requisition can be deleted (only pending ones)
        if (!"Pending".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Only pending requisitions can be deleted.\nThis requisition is: " + status,
                    "Cannot Delete",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this requisition?\n\n" +
                "Requisition ID: " + requisitionId + "\n" +
                "Item: " + itemName + "\n\n" +
                "This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper();
                dbHelper.deletePurchaseRequisition(requisitionId);

                JOptionPane.showMessageDialog(this,
                        "Requisition deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                logSystemAction(SystemLog.ACTION_DELETE, "Deleted purchase requisition: " + requisitionId);
                loadRequisitions(); // Refresh the table

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting requisition: " + ex.getMessage(),
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Error deleting requisition: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
}

