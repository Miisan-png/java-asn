package purchase;

import database.DatabaseHelper;
import models.PurchaseOrder;
import models.User;
import models.SystemLog;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;

public class ManagePurchaseOrdersPage extends JFrame {
    private final User currentUser;
    private JTable poTable;
    private DefaultTableModel tableModel;
    private List<PurchaseOrder> orders;
    private final Color primaryColor = new Color(60, 141, 188);
    private final Font headerFont = new Font("SansSerif", Font.BOLD, 24);
    private final int APP_WINDOW_HEIGHT = 700;

    public ManagePurchaseOrdersPage(User user) {
        super("Manage Purchase Orders");
        this.currentUser = user;
        setSize(1200, APP_WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadPendingOrders();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel sidebar = createSidebar();
        JPanel topBar = createTopBar();
        JPanel content = createMainContent();

        root.add(sidebar, BorderLayout.WEST);
        root.add(topBar, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, APP_WINDOW_HEIGHT));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        // Logo Panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel logo = new JLabel("Purchase Dept", SwingConstants.CENTER);
        logo.setFont(new Font("Serif", Font.BOLD, 16));
        logo.setForeground(primaryColor);
        logoPanel.add(logo, BorderLayout.CENTER);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Menu Items
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        // Dashboard Item
        JPanel dashboardItem = new JPanel(new BorderLayout());
        dashboardItem.setBackground(new Color(230, 230, 230));
        dashboardItem.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        dashboardItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel dashboardLabel = new JLabel("Dashboard");
        dashboardLabel.setFont(new Font("Serif", Font.BOLD, 16));
        dashboardItem.add(dashboardLabel, BorderLayout.CENTER);
        
        dashboardItem.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                new PurchaseDashboardPage(currentUser).setVisible(true);
            }
        });

        // Purchase Orders Item
        JPanel ordersItem = new JPanel(new BorderLayout());
        ordersItem.setBackground(Color.WHITE);
        ordersItem.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        ordersItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel ordersLabel = new JLabel("Purchase Orders");
        ordersLabel.setFont(new Font("Serif", Font.BOLD, 16));
        ordersItem.add(ordersLabel, BorderLayout.CENTER);

        menuPanel.add(dashboardItem);
        menuPanel.add(ordersItem);
        menuPanel.add(Box.createVerticalGlue());

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Logout Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(120, 120, 120));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(120, 35));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setFocusPainted(false);

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Log out?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        bottomPanel.add(logoutBtn);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        // User Panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        userPanel.setBackground(new Color(180, 180, 180));
        userPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 20));

        JLabel bell = new JLabel("🔔");
        bell.setFont(new Font("SansSerif", Font.PLAIN, 16));
        userPanel.add(bell);

        String displayName = currentUser.getUsername() != null && !currentUser.getUsername().isEmpty()
                ? currentUser.getUsername()
                : "User";

        JLabel userLabel = new JLabel(displayName + " ▾");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.add(userLabel);
        
        userLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new admin.MyProfilePage(currentUser).setVisible(true);
            }
        });

        top.add(userPanel, BorderLayout.NORTH);

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Manage Purchase Orders");
        title.setFont(headerFont);
        title.setForeground(primaryColor);
        titlePanel.add(title);

        top.add(titlePanel, BorderLayout.SOUTH);
        return top;
    }

    private JPanel createMainContent() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);
        
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(primaryColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> searchOrders(searchField.getText()));
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        JButton addButton = new JButton("Add New PO");
        addButton.setBackground(primaryColor);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> showAddPODialog());
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(addButton);
        
        panel.add(searchPanel, BorderLayout.NORTH);

        // Table Setup
        String[] cols = {"Order ID", "Requisition ID", "Item Code", "Item Name",
                "Quantity", "Unit Price", "Total", "Status", "Order Date", "Supplier ID"};

        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        poTable = new JTable(tableModel);
        poTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String status = (String) table.getValueAt(row, 7);

                if (isSelected) {
                    c.setBackground(new Color(173, 216, 230));
                } else {
                    switch (status) {
                        case PurchaseOrder.STATUS_PENDING:
                            c.setBackground(new Color(255, 250, 205));
                            break;
                        case PurchaseOrder.STATUS_COMPLETED:
                            c.setBackground(new Color(224, 255, 224));
                            break;
                        case PurchaseOrder.STATUS_CANCELLED:
                            c.setBackground(new Color(255, 224, 224));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                            break;
                    }
                }

                return c;
            }
        });
        poTable.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(poTable);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Action Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton approve = createActionButton("Approve", () -> updateStatus(PurchaseOrder.STATUS_COMPLETED));
        JButton reject = createActionButton("Reject", () -> updateStatus(PurchaseOrder.STATUS_CANCELLED));
        JButton edit = createActionButton("Edit", this::editSelectedPO);
        JButton delete = createActionButton("Delete", this::deleteSelectedPO);
        JButton refresh = createActionButton("Refresh", this::loadPendingOrders);

        buttonPanel.add(approve);
        buttonPanel.add(reject);
        buttonPanel.add(edit);
        buttonPanel.add(delete);
        buttonPanel.add(refresh);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Double-click to edit
        poTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedPO();
                }
            }
        });
        
        return panel;
    }

    private JButton createActionButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(primaryColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 40));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void loadPendingOrders() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            orders = db.getAllPurchaseOrders();
            tableModel.setRowCount(0);
            for (PurchaseOrder po : orders) {
                tableModel.addRow(new Object[]{
                    po.getOrderId(),
                    po.getRequisitionId(),
                    po.getItemCode(),
                    po.getItemName(),
                    po.getQuantity(),
                    String.format("$%.2f", po.getUnitPrice()),
                    String.format("$%.2f", po.getTotalAmount()),
                    po.getStatus(),
                    po.getOrderDate(),
                    po.getSupplierId()
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void searchOrders(String searchText) {
        if (searchText.isEmpty()) {
            loadPendingOrders();
            return;
        }
        
        String searchLower = searchText.toLowerCase();
        tableModel.setRowCount(0);
        
        for (PurchaseOrder po : orders) {
            if (po.getOrderId().toLowerCase().contains(searchLower) ||
                po.getRequisitionId().toLowerCase().contains(searchLower) ||
                po.getItemCode().toLowerCase().contains(searchLower) ||
                po.getItemName().toLowerCase().contains(searchLower) ||
                po.getSupplierId().toLowerCase().contains(searchLower)) {
                
                tableModel.addRow(new Object[]{
                    po.getOrderId(),
                    po.getRequisitionId(),
                    po.getItemCode(),
                    po.getItemName(),
                    po.getQuantity(),
                    String.format("$%.2f", po.getUnitPrice()),
                    String.format("$%.2f", po.getTotalAmount()),
                    po.getStatus(),
                    po.getOrderDate(),
                    po.getSupplierId()
                });
            }
        }
    }

    private void updateStatus(String status) {
        int row = poTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a PO.");
            return;
        }

        String id = (String) tableModel.getValueAt(row, 0);

        try {
            DatabaseHelper db = new DatabaseHelper();
            PurchaseOrder po = db.getPurchaseOrderById(id);
            if (po != null) {
                po.setStatus(status);
                db.updatePurchaseOrder(po);
                
                SystemLog log = new SystemLog(
                    "LOG" + System.currentTimeMillis(),
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    SystemLog.ACTION_UPDATE,
                    "Changed status of " + po.getOrderId() + " to " + status,
                    LocalDateTime.now(),
                    currentUser.getRole()
                );
                db.addSystemLog(log);
                
                JOptionPane.showMessageDialog(this, "Status updated.");
                loadPendingOrders();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage());
        }
    }

    private void showAddPODialog() {
        JDialog addDialog = new JDialog(this, "Add New Purchase Order", true);
        addDialog.setSize(500, 400);
        addDialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField reqIdField = new JTextField();
        JTextField itemCodeField = new JTextField();
        JTextField itemNameField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField unitPriceField = new JTextField();
        JTextField supplierIdField = new JTextField();
        
        formPanel.add(new JLabel("Requisition ID*:"));
        formPanel.add(reqIdField);
        formPanel.add(new JLabel("Item Code*:"));
        formPanel.add(itemCodeField);
        formPanel.add(new JLabel("Item Name*:"));
        formPanel.add(itemNameField);
        formPanel.add(new JLabel("Quantity*:"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Unit Price*:"));
        formPanel.add(unitPriceField);
        formPanel.add(new JLabel("Supplier ID*:"));
        formPanel.add(supplierIdField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.setBackground(primaryColor);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(120, 120, 120));
        cancelButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            try {
                // Validate required fields
                if (reqIdField.getText().trim().isEmpty() || 
                    itemCodeField.getText().trim().isEmpty() ||
                    itemNameField.getText().trim().isEmpty() ||
                    quantityField.getText().trim().isEmpty() ||
                    unitPriceField.getText().trim().isEmpty() ||
                    supplierIdField.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("All fields marked with * are required");
                }
                
                // Validate numbers
                int quantity = Integer.parseInt(quantityField.getText());
                double unitPrice = Double.parseDouble(unitPriceField.getText());
                
                if (quantity <= 0 || unitPrice <= 0) {
                    throw new NumberFormatException("Quantity and price must be positive numbers");
                }
                
                // Create PO object
                PurchaseOrder newPO = new PurchaseOrder(
                    "PO" + System.currentTimeMillis(),
                    reqIdField.getText().trim(),
                    itemCodeField.getText().trim(),
                    itemNameField.getText().trim(),
                    quantity,
                    unitPrice,
                    quantity * unitPrice,
                    PurchaseOrder.STATUS_PENDING,
                    LocalDateTime.now().toString(),
                    supplierIdField.getText().trim()
                );
                
                // Save to database
                DatabaseHelper db = new DatabaseHelper();
                db.addPurchaseOrder(newPO);
                
                // Log the action
                SystemLog log = new SystemLog(
                    "LOG" + System.currentTimeMillis(),
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    SystemLog.ACTION_CREATE,
                    "Created new PO: " + newPO.getOrderId(),
                    LocalDateTime.now(),
                    currentUser.getRole()
                );
                db.addSystemLog(log);
                
                addDialog.dispose();
                loadPendingOrders();
                JOptionPane.showMessageDialog(this, "Purchase order added successfully!");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, 
                    "Invalid number format: " + ex.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(addDialog, 
                    ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(addDialog, 
                    "Database error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        addDialog.add(formPanel, BorderLayout.CENTER);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }

    private void editSelectedPO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to edit.");
            return;
        }
        
        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        
        try {
            DatabaseHelper db = new DatabaseHelper();
            PurchaseOrder po = db.getPurchaseOrderById(orderId);
            
            if (po == null) {
                JOptionPane.showMessageDialog(this, "Selected purchase order not found.");
                return;
            }
            
            JDialog editDialog = new JDialog(this, "Edit Purchase Order", true);
            editDialog.setSize(500, 450);
            editDialog.setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JTextField reqIdField = new JTextField(po.getRequisitionId());
            JTextField itemCodeField = new JTextField(po.getItemCode());
            JTextField itemNameField = new JTextField(po.getItemName());
            JTextField quantityField = new JTextField(String.valueOf(po.getQuantity()));
            JTextField unitPriceField = new JTextField(String.valueOf(po.getUnitPrice()));
            JTextField supplierIdField = new JTextField(po.getSupplierId());
            
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{
                PurchaseOrder.STATUS_PENDING,
                PurchaseOrder.STATUS_COMPLETED,
                PurchaseOrder.STATUS_CANCELLED
            });
            statusCombo.setSelectedItem(po.getStatus());
            
            formPanel.add(new JLabel("Order ID:"));
            formPanel.add(new JLabel(po.getOrderId()));
            formPanel.add(new JLabel("Requisition ID*:"));
            formPanel.add(reqIdField);
            formPanel.add(new JLabel("Item Code*:"));
            formPanel.add(itemCodeField);
            formPanel.add(new JLabel("Item Name*:"));
            formPanel.add(itemNameField);
            formPanel.add(new JLabel("Quantity*:"));
            formPanel.add(quantityField);
            formPanel.add(new JLabel("Unit Price*:"));
            formPanel.add(unitPriceField);
            formPanel.add(new JLabel("Supplier ID*:"));
            formPanel.add(supplierIdField);
            formPanel.add(new JLabel("Status:"));
            formPanel.add(statusCombo);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save Changes");
            JButton cancelButton = new JButton("Cancel");
            
            saveButton.setBackground(primaryColor);
            saveButton.setForeground(Color.WHITE);
            cancelButton.setBackground(new Color(120, 120, 120));
            cancelButton.setForeground(Color.WHITE);
            
            saveButton.addActionListener(e -> {
                try {
                    // to validate
                    if (reqIdField.getText().trim().isEmpty() || 
                        itemCodeField.getText().trim().isEmpty() ||
                        itemNameField.getText().trim().isEmpty() ||
                        quantityField.getText().trim().isEmpty() ||
                        unitPriceField.getText().trim().isEmpty() ||
                        supplierIdField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("All fields marked with * are required");
                    }
                    
                    // Validate numbers
                    int quantity = Integer.parseInt(quantityField.getText());
                    double unitPrice = Double.parseDouble(unitPriceField.getText());
                    
                    if (quantity <= 0 || unitPrice <= 0) {
                        throw new NumberFormatException("Quantity and price must be positive numbers");
                    }
                    
                    // Update PO
                    po.setRequisitionId(reqIdField.getText().trim());
                    po.setItemCode(itemCodeField.getText().trim());
                    po.setItemName(itemNameField.getText().trim());
                    po.setQuantity(quantity);
                    po.setUnitPrice(unitPrice);
                    po.setTotalAmount(quantity * unitPrice);
                    po.setSupplierId(supplierIdField.getText().trim());
                    po.setStatus((String) statusCombo.getSelectedItem());
                                  
                    db.updatePurchaseOrder(po);
                    
                    // Log the action
                    SystemLog log = new SystemLog(
                        "LOG" + System.currentTimeMillis(),
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        SystemLog.ACTION_UPDATE,
                        "Updated PO: " + po.getOrderId(),
                        LocalDateTime.now(),
                        currentUser.getRole()
                    );
                    db.addSystemLog(log);
                    
                    editDialog.dispose();
                    loadPendingOrders();
                    JOptionPane.showMessageDialog(this, "Purchase order updated successfully!");
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "Invalid number format: " + ex.getMessage(),
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(editDialog, 
                        ex.getMessage(),
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "Database error: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> editDialog.dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            editDialog.add(formPanel, BorderLayout.CENTER);
            editDialog.add(buttonPanel, BorderLayout.SOUTH);
            editDialog.setLocationRelativeTo(this);
            editDialog.setVisible(true);
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading purchase order: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedPO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to delete.");
            return;
        }
        
        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to delete purchase order " + orderId + "?", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseHelper db = new DatabaseHelper();
                db.deletePurchaseOrder(orderId);
                
                SystemLog log = new SystemLog(
                    "LOG" + System.currentTimeMillis(),
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    SystemLog.ACTION_DELETE,
                    "Deleted PO: " + orderId,
                    LocalDateTime.now(),
                    currentUser.getRole()
                );
                db.addSystemLog(log);
                
                loadPendingOrders();
                JOptionPane.showMessageDialog(this, "Purchase order deleted successfully!");
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting purchase order: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        User testUser = new User("testuser", "Test User", "password", "purchasing");
        SwingUtilities.invokeLater(() -> {
            ManagePurchaseOrdersPage page = new ManagePurchaseOrdersPage(testUser);
            page.setVisible(true);
        });
    }
}