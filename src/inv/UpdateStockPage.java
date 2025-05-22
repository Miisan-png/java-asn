package inv;

import database.DatabaseHelper;
import models.Stock;
import models.User;
import javax.swing.*;
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
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create header panel with back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JButton backButton = new JButton("← Back to Dashboard");
        backButton.addActionListener(e -> goBackToDashboard());
        headerPanel.add(backButton, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create table to display current stock levels
        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Quantity"}, 0);
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.getTableHeader().setReorderingAllowed(false);
        stockTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create form panel for updating stock quantity
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel quantityLabel = new JLabel("New Quantity:");
        formPanel.add(quantityLabel, gbc);

        gbc.gridx = 1;
        quantityField = new JTextField(10);
        formPanel.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton updateButton = new JButton("Update Stock");
        updateButton.addActionListener(e -> updateStock());
        formPanel.add(updateButton, gbc);

        mainPanel.add(formPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);

        // Load current stock data
        loadStockData();
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

            // Refresh the stock data
            loadStockData();

            // Clear the input field after successful update
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
        InventoryDashboardPage inventoryDashboardPage = new InventoryDashboardPage(currentUser);
        inventoryDashboardPage.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User currentUser = new User("John Doe", "johndoe", "password", "inventory");
            UpdateStockPage updateStockPage = new UpdateStockPage(currentUser);
            updateStockPage.setVisible(true);
        });
    }
}