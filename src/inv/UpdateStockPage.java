package inv;

import database.DatabaseHelper;
import models.Stock;
import models.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class UpdateStockPage extends admin.UIBase {
    private final User currentUser;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JTextField quantityField;

    public UpdateStockPage(User user) {
        super("Update Stock");
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

        JPanel viewStockItem = createMenuItem("Update Stock", true);
        JPanel backItem = createMenuItem("Dashboard", false);
        backItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        menuPanel.add(viewStockItem);
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

        JLabel title = new JLabel("Update Stock");
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

        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Quantity"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setRowHeight(28);

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        formPanel.setBackground(Color.WHITE);

        quantityField = new JTextField(10);
        JButton updateButton = new JButton("Update Stock");
        styleButton(updateButton);
        updateButton.addActionListener(e -> updateStock());

        formPanel.add(new JLabel("New Quantity:"));
        formPanel.add(quantityField);
        formPanel.add(updateButton);

        content.add(formPanel, BorderLayout.SOUTH);

        loadStockData();
        return content;
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

    private void loadStockData() {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            List<Stock> stockList = dbHelper.getAllStock();

            tableModel.setRowCount(0);
            for (Stock stock : stockList) {
                Object[] rowData = {stock.getItemCode(), stock.getItemName(), stock.getQuantity()};
                tableModel.addRow(rowData);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading stock data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStock() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
        String quantityStr = quantityField.getText().trim();

        if (quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the new quantity.",
                    "Incomplete Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int newQuantity = Integer.parseInt(quantityStr);

            DatabaseHelper dbHelper = new DatabaseHelper();
            dbHelper.updateStockQuantity(itemCode, newQuantity);

            JOptionPane.showMessageDialog(this, "Stock updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadStockData();
            quantityField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error updating stock: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }
}