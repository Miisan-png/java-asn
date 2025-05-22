package inv;

import database.DatabaseHelper;
import models.Stock;
import models.SystemLog;
import models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ManualStockAdjustmentPage extends admin.UIBase {
    private final User currentUser;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JTextField adjustmentField;
    private JTextField reasonField;

    public ManualStockAdjustmentPage(User user) {
        super("Manual Stock Adjustment");
        this.currentUser = user;
    }

    @Override
    protected void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JButton backButton = new JButton("← Back to Dashboard");
        backButton.addActionListener(e -> goBackToDashboard());
        headerPanel.add(backButton, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Quantity"}, 0);
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.getTableHeader().setReorderingAllowed(false);
        stockTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel adjustmentLabel = new JLabel("Adjustment Quantity:");
        formPanel.add(adjustmentLabel, gbc);

        gbc.gridx = 1;
        adjustmentField = new JTextField(10);
        formPanel.add(adjustmentField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel reasonLabel = new JLabel("Reason:");
        formPanel.add(reasonLabel, gbc);

        gbc.gridx = 1;
        reasonField = new JTextField(20);
        formPanel.add(reasonField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton adjustButton = new JButton("Adjust Stock");
        adjustButton.addActionListener(e -> adjustStock());
        formPanel.add(adjustButton, gbc);

        mainPanel.add(formPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);

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

    private void adjustStock() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to adjust.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
        String adjustmentStr = adjustmentField.getText().trim();
        String reason = reasonField.getText().trim();

        if (adjustmentStr.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the adjustment quantity and reason.",
                    "Incomplete Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int adjustment = Integer.parseInt(adjustmentStr);
            int currentQuantity = (int) tableModel.getValueAt(selectedRow, 2);
            int newQuantity = currentQuantity + adjustment;

            DatabaseHelper dbHelper = new DatabaseHelper();
            dbHelper.updateStockQuantity(itemCode, newQuantity);

            SystemLog log = new SystemLog(
                    "LOG" + System.currentTimeMillis(),
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    SystemLog.ACTION_UPDATE,
                    "Manual stock adjustment for item " + itemCode + ". Quantity adjusted by " + adjustment + ". Reason: " + reason,
                    LocalDateTime.now(),
                    currentUser.getRole()
            );
            dbHelper.addInventoryLog(log);

            JOptionPane.showMessageDialog(this, "Stock adjusted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadStockData();
            adjustmentField.setText("");
            reasonField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid adjustment quantity. Please enter a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error adjusting stock: " + ex.getMessage(),
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
            ManualStockAdjustmentPage adjustmentPage = new ManualStockAdjustmentPage(currentUser);
            adjustmentPage.setVisible(true);
        });
    }
}