package inv;

import database.DatabaseHelper;
import models.Stock;
import models.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ViewCurrentStockPage extends admin.UIBase {
    private final User currentUser;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private List<Stock> allStockList;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> quantityFilter;

    public ViewCurrentStockPage(User user) {
        super("View Current Stock");
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
        loadStockData();
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

        JPanel stockItem = createMenuItem("View Stock", true);
        stockItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel backItem = createMenuItem("Dashboard", false);
        backItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        menuPanel.add(stockItem);
        menuPanel.add(backItem);
        menuPanel.add(Box.createVerticalGlue());

        sidebar.add(menuPanel, BorderLayout.CENTER);
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

        JLabel title = new JLabel("View Current Stock");
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
        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Quantity", "Location", "Last Updated", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };

        stockTable = new JTable(tableModel);
        stockTable.setAutoCreateRowSorter(true);
        stockTable.setRowHeight(28);
        stockTable.getTableHeader().setBackground(new Color(240, 240, 240));
        stockTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        stockTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stockTable.setSelectionBackground(new Color(232, 242, 254));
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setShowGrid(true);
        stockTable.setGridColor(new Color(230, 230, 230));

        // Custom renderer for status and quantity columns
        stockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (column == 5) { // Status column
                        String status = value != null ? value.toString() : "";
                        switch (status) {
                            case Stock.STATUS_OUT_OF_STOCK -> {
                                c.setBackground(new Color(255, 230, 230));
                                c.setForeground(new Color(220, 53, 69));
                            }
                            case Stock.STATUS_LOW_STOCK -> {
                                c.setBackground(new Color(255, 243, 205));
                                c.setForeground(new Color(255, 145, 0));
                            }
                            default -> {
                                c.setBackground(new Color(212, 237, 218));
                                c.setForeground(new Color(40, 167, 69));
                            }
                        }
                    } else if (column == 2) { // Quantity column
                        try {
                            int quantity = Integer.parseInt(value.toString());
                            if (quantity == 0) {
                                c.setBackground(new Color(255, 230, 230));
                            } else if (quantity < 10) {
                                c.setBackground(new Color(255, 243, 205));
                            } else {
                                c.setBackground(Color.WHITE);
                            }
                            c.setForeground(Color.BLACK);
                        } catch (NumberFormatException e) {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(new Color(173, 216, 230));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockTable);
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
                        "Filters",
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
        String[] statusOptions = {"All Status", Stock.STATUS_IN_STOCK, Stock.STATUS_LOW_STOCK, Stock.STATUS_OUT_OF_STOCK};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusFilter.addActionListener(e -> applyFilters());

        // Quantity filter dropdown
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        String[] quantityOptions = {"All Quantities", "Out of Stock (0)", "Low Stock (<10)", "Normal Stock (>=10)"};
        quantityFilter = new JComboBox<>(quantityOptions);
        quantityFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        quantityFilter.addActionListener(e -> applyFilters());

        leftFilters.add(searchLabel);
        leftFilters.add(searchField);
        leftFilters.add(Box.createRigidArea(new Dimension(15, 0)));
        leftFilters.add(statusLabel);
        leftFilters.add(statusFilter);
        leftFilters.add(Box.createRigidArea(new Dimension(15, 0)));
        leftFilters.add(quantityLabel);
        leftFilters.add(quantityFilter);

        // Right side - Action buttons
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightActions.setBackground(Color.WHITE);


        filterPanel.add(leftFilters, BorderLayout.WEST);
        filterPanel.add(rightActions, BorderLayout.EAST);

        return filterPanel;
    }

    private JPanel createActionsPanel() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionsPanel.setBackground(Color.WHITE);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton exportButton = createFilterButton("Export to TXT", this::exportData);
        JButton printButton = createFilterButton("Print Report", this::printReport);

        actionsPanel.add(exportButton);
        actionsPanel.add(printButton);

        return actionsPanel;
    }

    private JButton createFilterButton(String text, Runnable action) {
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

    private void loadStockData() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            allStockList = db.getAllStock();
            if (allStockList == null) {
                allStockList = List.of();
            }
            applyFilters();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading stock data: " + ex.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            allStockList = List.of();
        }
    }

    private void applyFilters() {
        if (allStockList == null) {
            return;
        }

        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedQuantity = (String) quantityFilter.getSelectedItem();

        List<Stock> filteredList = allStockList.stream()
                .filter(stock -> stock != null)
                .filter(stock -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        return stock.getItemCode().toLowerCase().contains(searchText) ||
                               stock.getItemName().toLowerCase().contains(searchText) ||
                               stock.getLocation().toLowerCase().contains(searchText);
                    }
                    return true;
                })
                .filter(stock -> {
                    // Status filter
                    if (!"All Status".equals(selectedStatus)) {
                        return stock.getStatus().equals(selectedStatus);
                    }
                    return true;
                })
                .filter(stock -> {
                    // Quantity filter
                    if (!"All Quantities".equals(selectedQuantity)) {
                        int quantity = stock.getQuantity();
                        switch (selectedQuantity) {
                            case "Out of Stock (0)":
                                return quantity == 0;
                            case "Low Stock (<10)":
                                return quantity > 0 && quantity < 10;
                            case "Normal Stock (>=10)":
                                return quantity >= 10;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        updateTable(filteredList);
    }

    private void updateTable(List<Stock> stockList) {
        tableModel.setRowCount(0);
        for (Stock stock : stockList) {
            tableModel.addRow(new Object[]{
                stock.getItemCode(),
                stock.getItemName(),
                stock.getQuantity(),
                stock.getLocation(),
                stock.getLastUpdated(),
                stock.getStatus()
            });
        }
    }

    private void clearFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        quantityFilter.setSelectedIndex(0);
        applyFilters();
    }

    private void exportData() {
        JOptionPane.showMessageDialog(this, 
            "Export data to Txt", 
            "Coming Soon", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void printReport() {
        JOptionPane.showMessageDialog(this, 
            "Printing to your printer", 
            "Coming Soon", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }
}