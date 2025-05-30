package inv;

import database.DatabaseHelper;
import models.PurchaseOrder;
import models.Stock;
import models.SystemLog;
import models.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateStockFromPOPage extends admin.UIBase {
    private final User currentUser;
    private JTable poTable;
    private DefaultTableModel tableModel;
    private List<PurchaseOrder> allPurchaseOrders;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> supplierFilter;
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public UpdateStockFromPOPage(User user) {
        super("Update Stock From Purchase Orders");
        this.currentUser = user;
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
        
        // Load initial data
        loadPurchaseOrders();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, APP_WINDOW_HEIGHT));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel logo = new JLabel("Inventory", SwingConstants.CENTER);
        logo.setFont(new Font("Serif", Font.BOLD, 16));
        logo.setForeground(primaryColor);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        logoPanel.add(logo, BorderLayout.CENTER);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JPanel updateStockItem = createMenuItem("Update Stock from PO", true);
        JPanel dashboardItem = createMenuItem("Dashboard", false);
        dashboardItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        menuPanel.add(updateStockItem);
        menuPanel.add(dashboardItem);
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
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to log out?", 
                "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    try {
                        Class<?> loginClass = Class.forName("LoginPage");
                        java.lang.reflect.Method mainMethod = loginClass.getMethod("main", String[].class);
                        mainMethod.invoke(null, (Object) new String[0]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(0);
                    }
                });
            }
        });
        logoutPanel.add(logoutBtn);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("Update Stock From Purchase Orders");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(primaryColor);

        headerPanel.add(title);
        top.add(headerPanel, BorderLayout.SOUTH);

        return top;
    }

    private JPanel createMenuItem(String text, boolean selected) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(selected ? new Color(230, 230, 230) : Color.WHITE);
        item.setMaximumSize(new Dimension(200, 50));
        item.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Serif", Font.BOLD, 16));
        item.add(label, BorderLayout.CENTER);

        return item;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create filter panel at the top
        JPanel filterPanel = createFilterPanel();
        content.add(filterPanel, BorderLayout.NORTH);

        // Create table
        tableModel = new DefaultTableModel(new Object[]{
            "PO ID", "Item Code", "Item Name", "Supplier", "Ordered Qty", "Received Qty", "Order Date", "Status"
        }, 0) {
            public boolean isCellEditable(int r, int c) {
                // Only "Received Qty" column (index 5) is editable, and only for approved orders
                if (c == 5) {
                    try {
                        String status = (String) getValueAt(r, 7);
                        return PurchaseOrder.STATUS_COMPLETED.equals(status);
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4 || column == 5) { // Quantity columns
                    return Integer.class;
                }
                return String.class;
            }
        };

        poTable = new JTable(tableModel);
        poTable.setRowHeight(30);
        poTable.getTableHeader().setBackground(new Color(240, 240, 240));
        poTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        poTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        poTable.setSelectionBackground(new Color(232, 242, 254));
        poTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        poTable.setShowGrid(true);
        poTable.setGridColor(new Color(230, 230, 230));

        // Set column widths
        poTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PO ID
        poTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Item Code
        poTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Item Name
        poTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Supplier
        poTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Ordered Qty
        poTable.getColumnModel().getColumn(5).setPreferredWidth(90);  // Received Qty
        poTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Order Date
        poTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Status

        // Custom renderer for status and editable cells
        poTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (column == 7) { // Status column
                        String status = value != null ? value.toString() : "";
                        switch (status) {
                            case PurchaseOrder.STATUS_COMPLETED -> {
                                c.setBackground(new Color(212, 237, 218));
                                c.setForeground(new Color(40, 167, 69));
                            }
                            case PurchaseOrder.STATUS_CANCELLED -> {
                                c.setBackground(new Color(248, 215, 218));
                                c.setForeground(new Color(220, 53, 69));
                            }
                            default -> {
                                c.setBackground(new Color(255, 243, 205));
                                c.setForeground(new Color(255, 145, 0));
                            }
                        }
                    } else if (column == 5) { // Received Qty column - highlight editable cells
                        try {
                            String status = (String) table.getValueAt(row, 7);
                            if (PurchaseOrder.STATUS_COMPLETED.equals(status)) {
                                c.setBackground(new Color(230, 247, 255));
                                c.setForeground(Color.BLACK);
                            } else {
                                c.setBackground(new Color(245, 245, 245));
                                c.setForeground(Color.GRAY);
                            }
                        } catch (Exception e) {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(poTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        content.add(scrollPane, BorderLayout.CENTER);

        // Create action buttons panel
        JPanel actionsPanel = createActionsPanel();
        content.add(actionsPanel, BorderLayout.SOUTH);

        return content;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        "Purchase Order Filters",
                        0,
                        0,
                        new Font("SansSerif", Font.BOLD, 14)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Left side - Search and filters
        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftFilters.setBackground(Color.WHITE);

        // Search field
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchField.addActionListener(e -> applyFilters());

        // Status filter dropdown
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        String[] statusOptions = {"All Status", PurchaseOrder.STATUS_PENDING, PurchaseOrder.STATUS_COMPLETED, PurchaseOrder.STATUS_CANCELLED};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusFilter.addActionListener(e -> applyFilters());

        // Supplier filter dropdown
        JLabel supplierLabel = new JLabel("Supplier:");
        supplierLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        supplierFilter = new JComboBox<>();
        supplierFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        supplierFilter.addActionListener(e -> applyFilters());

        leftFilters.add(searchLabel);
        leftFilters.add(searchField);
        leftFilters.add(Box.createRigidArea(new Dimension(15, 0)));
        leftFilters.add(statusLabel);
        leftFilters.add(statusFilter);
        leftFilters.add(Box.createRigidArea(new Dimension(15, 0)));
        leftFilters.add(supplierLabel);
        leftFilters.add(supplierFilter);

        // Right side - Just some spacing or info
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightActions.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("Use dropdown filters to refine results");
        infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        rightActions.add(infoLabel);

        filterPanel.add(leftFilters, BorderLayout.WEST);
        filterPanel.add(rightActions, BorderLayout.EAST);

        return filterPanel;
    }
    private JPanel createActionsPanel() {
    JPanel actionsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
    actionsPanel.setBackground(Color.WHITE);
    actionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Stock Update Actions",
                    0,
                    0,
                    new Font("SansSerif", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(15, 15, 20, 15)
    ));

    JButton confirmReceiptBtn = createActionButton("Confirm Receipt", this::confirmReceipt);
    JButton updateQuantityBtn = createActionButton("Update Received Qty", this::updateReceivedQuantity);
    JButton viewDetailsBtn = createActionButton("View Details", this::viewOrderDetails);

    actionsPanel.add(confirmReceiptBtn);
    actionsPanel.add(updateQuantityBtn);
    actionsPanel.add(viewDetailsBtn);

    return actionsPanel;
}
    private JButton createActionButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setBackground(new Color(96, 96, 96));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton createMainActionButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setBackground(new Color(96, 96, 96));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void loadPurchaseOrders() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            allPurchaseOrders = db.getAllPurchaseOrders();
            if (allPurchaseOrders == null) {
                allPurchaseOrders = List.of();
            }
            
            // Populate supplier filter first, before applying filters
            SwingUtilities.invokeLater(() -> {
                updateSupplierFilter();
                applyFilters();
            });
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading purchase orders: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            allPurchaseOrders = List.of();
        }
    }

    private void updateSupplierFilter() {
        try {
            // Temporarily remove the action listener to prevent firing during updates
            java.awt.event.ActionListener[] listeners = supplierFilter.getActionListeners();
            for (java.awt.event.ActionListener listener : listeners) {
                supplierFilter.removeActionListener(listener);
            }
            
            supplierFilter.removeAllItems();
            supplierFilter.addItem("All Suppliers");
            
            if (allPurchaseOrders != null) {
                allPurchaseOrders.stream()
                        .filter(po -> po != null && po.getSupplierId() != null)
                        .map(PurchaseOrder::getSupplierId)
                        .distinct()
                        .sorted()
                        .forEach(supplier -> supplierFilter.addItem(supplier));
            }
            
            // Re-add the action listeners
            for (java.awt.event.ActionListener listener : listeners) {
                supplierFilter.addActionListener(listener);
            }
            
        } catch (Exception e) {
            System.out.println("Error updating supplier filter: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (allPurchaseOrders == null) {
            return;
        }

        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedSupplier = (String) supplierFilter.getSelectedItem();

        List<PurchaseOrder> filteredList = allPurchaseOrders.stream()
                .filter(po -> po != null)
                .filter(po -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        return po.getOrderId().toLowerCase().contains(searchText) ||
                               po.getItemCode().toLowerCase().contains(searchText) ||
                               (po.getItemName() != null && po.getItemName().toLowerCase().contains(searchText)) ||
                               po.getSupplierId().toLowerCase().contains(searchText);
                    }
                    return true;
                })
                .filter(po -> {
                    // Status filter
                    if (!"All Status".equals(selectedStatus)) {
                        return po.getStatus().equals(selectedStatus);
                    }
                    return true;
                })
                .filter(po -> {
                    // Supplier filter
                    if (!"All Suppliers".equals(selectedSupplier)) {
                        return po.getSupplierId().equals(selectedSupplier);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        updateTable(filteredList);
    }

    private void updateTable(List<PurchaseOrder> poList) {
        tableModel.setRowCount(0);
        for (PurchaseOrder po : poList) {
            String orderDate = "N/A";
            try {
                if (po.getOrderDate() != null) {
                    orderDate = po.getOrderDate().format(displayDateFormatter);
                }
            } catch (Exception e) {
                orderDate = po.getOrderDate() != null ? po.getOrderDate().toString() : "N/A";
            }
                
            tableModel.addRow(new Object[]{
                po.getOrderId(),
                po.getItemCode(),
                po.getItemName() != null ? po.getItemName() : "N/A",
                po.getSupplierId(),
                po.getQuantity(),
                po.getQuantity(), // Default received quantity to ordered quantity
                orderDate,
                po.getStatus()
            });
        }
    }

    private void clearFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        if (supplierFilter.getItemCount() > 0) {
            supplierFilter.setSelectedIndex(0);
        }
        applyFilters();
    }

    private void confirmReceipt() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a purchase order to confirm receipt.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String poId = (String) tableModel.getValueAt(selectedRow, 0);
            String itemCode = (String) tableModel.getValueAt(selectedRow, 1);
            String status = (String) tableModel.getValueAt(selectedRow, 7);
            
            if (!PurchaseOrder.STATUS_COMPLETED.equals(status)) {
                JOptionPane.showMessageDialog(this,
                    "Only approved/completed purchase orders can have stock confirmed.\nCurrent status: " + status,
                    "Invalid Status",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int receivedQty = Integer.parseInt(tableModel.getValueAt(selectedRow, 5).toString());

            int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm receipt of " + receivedQty + " units for item " + itemCode + "?\n" +
                "This will update the stock quantity.",
                "Confirm Stock Receipt",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseHelper db = new DatabaseHelper();
                
                // Update stock quantity (add to existing stock)
                db.updateStockQuantity(itemCode, receivedQty, true);

                // Log the action
                SystemLog log = new SystemLog(
                        "LOG" + System.currentTimeMillis(),
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        SystemLog.ACTION_UPDATE,
                        "Confirmed stock receipt for PO " + poId + " - Item: " + itemCode + ", Qty: " + receivedQty,
                        LocalDateTime.now(),
                        currentUser.getRole()
                );
                db.addInventoryLog(log);

                JOptionPane.showMessageDialog(this,
                    "Stock receipt confirmed successfully!\nItem: " + itemCode + "\nQuantity added: " + receivedQty,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                loadPurchaseOrders(); // Refresh data
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid quantity format. Please check the received quantity.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error confirming receipt: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReceivedQuantity() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a purchase order to update received quantity.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String status = (String) tableModel.getValueAt(selectedRow, 7);
            if (!PurchaseOrder.STATUS_COMPLETED.equals(status)) {
                JOptionPane.showMessageDialog(this,
                    "Only approved/completed purchase orders can be updated.\nCurrent status: " + status,
                    "Invalid Status",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String currentQtyStr = tableModel.getValueAt(selectedRow, 5).toString();
            String orderedQtyStr = tableModel.getValueAt(selectedRow, 4).toString();

            String newQtyStr = JOptionPane.showInputDialog(this,
                "Enter received quantity:\n(Ordered: " + orderedQtyStr + ", Current: " + currentQtyStr + ")",
                "Update Received Quantity",
                JOptionPane.QUESTION_MESSAGE);

            if (newQtyStr != null && !newQtyStr.trim().isEmpty()) {
                int newQty = Integer.parseInt(newQtyStr.trim());
                if (newQty < 0) {
                    JOptionPane.showMessageDialog(this,
                        "Received quantity cannot be negative.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                tableModel.setValueAt(newQty, selectedRow, 5);
                JOptionPane.showMessageDialog(this,
                    "Received quantity updated to " + newQty + ".\nUse 'Confirm Receipt' to update stock.",
                    "Quantity Updated",
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid number.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewOrderDetails() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a purchase order to view details.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String poId = (String) tableModel.getValueAt(selectedRow, 0);
            String itemCode = (String) tableModel.getValueAt(selectedRow, 1);
            String itemName = (String) tableModel.getValueAt(selectedRow, 2);
            String supplier = (String) tableModel.getValueAt(selectedRow, 3);
            String orderedQty = tableModel.getValueAt(selectedRow, 4).toString();
            String receivedQty = tableModel.getValueAt(selectedRow, 5).toString();
            String orderDate = (String) tableModel.getValueAt(selectedRow, 6);
            String status = (String) tableModel.getValueAt(selectedRow, 7);

            StringBuilder details = new StringBuilder();
            details.append("Purchase Order Details\n");
            details.append("========================\n\n");
            details.append("PO ID: ").append(poId).append("\n");
            details.append("Item Code: ").append(itemCode).append("\n");
            details.append("Item Name: ").append(itemName).append("\n");
            details.append("Supplier: ").append(supplier).append("\n");
            details.append("Order Date: ").append(orderDate).append("\n");
            details.append("Status: ").append(status).append("\n\n");
            details.append("Quantities:\n");
            details.append("- Ordered: ").append(orderedQty).append("\n");
            details.append("- Received: ").append(receivedQty).append("\n");

            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
            textArea.setBackground(UIManager.getColor("Panel.background"));
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this,
                scrollPane,
                "Purchase Order Details",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error displaying order details: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }
}