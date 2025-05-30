package purchase;

import admin.UIBase;
import database.DatabaseHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.PurchaseRequisition;
import models.SystemLog;
import models.User;

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
        String[] columnNames = {"PR ID", "Item Code", "Item Name", "Quantity", "Required Date", "Sales Manager", "Supplier", "Status"};
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
            initTableModel();
        }

        tableModel.setRowCount(0);

        if (requisitionsList == null) {
            return;
        }

        System.out.println("Loading all requisitions - list size: " + requisitionsList.size());

        for (PurchaseRequisition requisition : requisitionsList) {
            if (requisition != null) {
                try {
                    
                    String itemName = requisition.getItemName();
                    if (itemName == null || itemName.isEmpty()) {
                        try {
                            DatabaseHelper db = new DatabaseHelper();
                            models.Item item = db.getItemByCode(requisition.getItemCode());
                            if (item != null) {
                                itemName = item.getItemName();
                            } else {
                                itemName = "Unknown Item"; 
                            }
                        } catch (Exception e) {
                            itemName = "Unknown Item"; 
                        }
                    }

                    
                    String salesManagerName = requisition.getSalesManagerId();
                    try {
                        DatabaseHelper db = new DatabaseHelper();
                        models.User salesManager = db.getUserById(requisition.getSalesManagerId());
                        if (salesManager != null) {
                            salesManagerName = salesManager.getUsername();
                        }
                    } catch (Exception e) {
                        
                    }

                    
                    String supplierInfo = "N/A";
                    try {
                        DatabaseHelper db = new DatabaseHelper();
                        models.Item item = db.getItemByCode(requisition.getItemCode());
                        if (item != null && item.getSupplierId() != null) {
                            models.Supplier supplier = db.getSupplierById(item.getSupplierId());
                            if (supplier != null) {
                                supplierInfo = supplier.getSupplierName();
                            } else {
                                supplierInfo = item.getSupplierId(); 
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching supplier info for requisition " + requisition.getRequisitionId() + ": " + e.getMessage());
                    }

                    
                    String dateStr = "";
                    LocalDate requiredDate = requisition.getRequiredDate();
                    if (requiredDate != null) {
                        try {
                            dateStr = requiredDate.format(displayDateFormatter);
                        } catch (Exception e) {
                            dateStr = requiredDate.toString();
                        }
                    }

                    Object[] rowData = {
                            requisition.getRequisitionId(),
                            requisition.getItemCode(),
                            itemName,
                            requisition.getQuantity(),
                            dateStr,
                            salesManagerName,
                            supplierInfo,
                            requisition.getStatus()
                    };
                    tableModel.addRow(rowData);

                    System.out.println("Added requisition: " + requisition.getRequisitionId() + " with supplier: " + supplierInfo);
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

        JLabel userLabel = new JLabel("User ▾");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.add(userLabel);

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

        if (tableModel == null) {
            initTableModel();
        }

        requisitionsTable = new JTable(tableModel);
        requisitionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requisitionsTable.getTableHeader().setBackground(new Color(240, 240, 240));
        requisitionsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        requisitionsTable.setRowHeight(30);
        requisitionsTable.setGridColor(Color.LIGHT_GRAY);

        
        requisitionsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  
        requisitionsTable.getColumnModel().getColumn(1).setPreferredWidth(90);  
        requisitionsTable.getColumnModel().getColumn(2).setPreferredWidth(150); 
        requisitionsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  
        requisitionsTable.getColumnModel().getColumn(4).setPreferredWidth(120); 
        requisitionsTable.getColumnModel().getColumn(5).setPreferredWidth(120); 
        requisitionsTable.getColumnModel().getColumn(6).setPreferredWidth(120); 
        requisitionsTable.getColumnModel().getColumn(7).setPreferredWidth(90);  

        
        requisitionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    try {
                        if (column == 7) { 
                            String status = value != null ? value.toString() : "";
                            if ("Approved".equals(status)) {
                                c.setBackground(new Color(230, 255, 230)); 
                                c.setForeground(new Color(0, 100, 0));     
                            } else if ("Rejected".equals(status)) {
                                c.setBackground(new Color(255, 230, 230)); 
                                c.setForeground(new Color(180, 0, 0));     
                            } else { 
                                c.setBackground(new Color(255, 255, 230)); 
                                c.setForeground(new Color(180, 100, 0));   
                            }
                        } else if (column == 6) { 
                            c.setBackground(new Color(240, 248, 255)); 
                            c.setForeground(new Color(25, 25, 112));   
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

        JScrollPane scrollPane = new JScrollPane(requisitionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.setPreferredSize(new Dimension(700, 400));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        
        JButton approveButton = new JButton("Approve PR");
        approveButton.setBackground(new Color(40, 167, 69)); 
        approveButton.setForeground(Color.WHITE);
        approveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        approveButton.setFocusPainted(false);
        approveButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        approveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        approveButton.setPreferredSize(new Dimension(150, 40));
        approveButton.addActionListener(e -> handleApproveRequisition());
        buttonsPanel.add(approveButton);

        
        JButton rejectButton = new JButton("Reject PR");
        rejectButton.setBackground(new Color(220, 53, 69)); 
        rejectButton.setForeground(Color.WHITE);
        rejectButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        rejectButton.setFocusPainted(false);
        rejectButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        rejectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rejectButton.setPreferredSize(new Dimension(150, 40));
        rejectButton.addActionListener(e -> handleRejectRequisition());
        buttonsPanel.add(rejectButton);

        
        JButton viewButton = new JButton("View Details");
        styleButton(viewButton);
        viewButton.addActionListener(e -> handleViewDetails());
        buttonsPanel.add(viewButton);

        
        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadRequisitions());
        buttonsPanel.add(refreshButton);

        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return contentPanel;
    }

    private void handleApproveRequisition() {
        int selectedRow = requisitionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a requisition to approve.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String requisitionId = (String) requisitionsTable.getValueAt(selectedRow, 0);
            String itemName = (String) requisitionsTable.getValueAt(selectedRow, 2);
            String currentStatus = (String) requisitionsTable.getValueAt(selectedRow, 7); 

            
            if (!"Pending".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "This requisition has already been " + currentStatus.toLowerCase() + ".",
                        "Cannot Approve",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to APPROVE this requisition?\n\n" +
                    "Requisition ID: " + requisitionId + "\n" +
                    "Item: " + itemName + "\n\n" +
                    "This will allow the requisition to proceed to purchase order creation.",
                    "Confirm Approval",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                updateRequisitionStatus(requisitionId, PurchaseRequisition.STATUS_APPROVED);
                
                
                requisitionsTable.setValueAt("Approved", selectedRow, 7);
                requisitionsTable.repaint();
                
                JOptionPane.showMessageDialog(this,
                        "Requisition approved successfully!",
                        "Approval Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                logSystemAction(SystemLog.ACTION_UPDATE, "Approved purchase requisition: " + requisitionId);
                
                
                loadRequisitions();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error approving requisition: " + ex.getMessage(),
                    "Approval Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleRejectRequisition() {
        int selectedRow = requisitionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a requisition to reject.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String requisitionId = (String) requisitionsTable.getValueAt(selectedRow, 0);
            String itemName = (String) requisitionsTable.getValueAt(selectedRow, 2);
            String currentStatus = (String) requisitionsTable.getValueAt(selectedRow, 7); 

            
            if (!"Pending".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "This requisition has already been " + currentStatus.toLowerCase() + ".",
                        "Cannot Reject",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            
            String reason = JOptionPane.showInputDialog(this,
                    "Please provide a reason for rejecting this requisition:\n\n" +
                    "Requisition ID: " + requisitionId + "\n" +
                    "Item: " + itemName,
                    "Rejection Reason",
                    JOptionPane.QUESTION_MESSAGE);

            if (reason != null && !reason.trim().isEmpty()) {
                updateRequisitionStatus(requisitionId, PurchaseRequisition.STATUS_REJECTED);
                
                
                requisitionsTable.setValueAt("Rejected", selectedRow, 7);
                requisitionsTable.repaint();
                
                JOptionPane.showMessageDialog(this,
                        "Requisition rejected successfully!\n\nReason: " + reason,
                        "Rejection Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                logSystemAction(SystemLog.ACTION_UPDATE, "Rejected purchase requisition: " + requisitionId + " - Reason: " + reason);
                
                
                loadRequisitions();
            } else if (reason != null) {
                JOptionPane.showMessageDialog(this,
                        "Rejection reason cannot be empty.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error rejecting requisition: " + ex.getMessage(),
                    "Rejection Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleViewDetails() {
        int selectedRow = requisitionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a requisition to view details.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String requisitionId = (String) requisitionsTable.getValueAt(selectedRow, 0);
            String itemCode = (String) requisitionsTable.getValueAt(selectedRow, 1);
            String itemName = (String) requisitionsTable.getValueAt(selectedRow, 2);
            int quantity = (Integer) requisitionsTable.getValueAt(selectedRow, 3);
            String requiredDate = (String) requisitionsTable.getValueAt(selectedRow, 4);
            String salesManager = (String) requisitionsTable.getValueAt(selectedRow, 5);
            String supplier = (String) requisitionsTable.getValueAt(selectedRow, 6);
            String status = (String) requisitionsTable.getValueAt(selectedRow, 7); 

            
            DatabaseHelper dbHelper = new DatabaseHelper();
            PurchaseRequisition pr = dbHelper.getPurchaseRequisitionById(requisitionId);
            
            String details = String.format(
                    "Purchase Requisition Details\n\n" +
                    "Requisition ID: %s\n" +
                    "Item Code: %s\n" +
                    "Item Name: %s\n" +
                    "Quantity Required: %d\n" +
                    "Required Date: %s\n" +
                    "Requested by: %s\n" +
                    "Supplier: %s\n" +
                    "Current Status: %s",
                    requisitionId, itemCode, itemName, quantity, 
                    requiredDate, salesManager, supplier, status
            );

            JOptionPane.showMessageDialog(this,
                    details,
                    "Requisition Details",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading requisition details: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRequisitionStatus(String requisitionId, String newStatus) throws IOException {
        DatabaseHelper dbHelper = new DatabaseHelper();
        PurchaseRequisition requisition = dbHelper.getPurchaseRequisitionById(requisitionId);
        
        if (requisition != null) {
            requisition.setStatus(newStatus);
            dbHelper.updatePurchaseRequisition(requisition);
        } else {
            throw new IOException("Requisition not found: " + requisitionId);
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(120, 120, 120));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));
    }

    private void goBackToDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            PurchaseDashboardPage dashboard = new PurchaseDashboardPage(currentUser);
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
}