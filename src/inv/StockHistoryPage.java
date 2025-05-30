package inv;

import database.DatabaseHelper;
import models.SystemLog;
import models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StockHistoryPage extends admin.UIBase {
    private final User currentUser;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public StockHistoryPage(User user) {
        super("Stock History");
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

        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Action", "Quantity", "User", "Timestamp"}, 0);
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        searchField.addActionListener(e -> searchHistory());
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchHistory());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);

        loadStockHistory();
    }

    private void loadStockHistory() {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            List<SystemLog> inventoryLogs = dbHelper.getInventoryLogs();

            tableModel.setRowCount(0);
            for (SystemLog log : inventoryLogs) {
                String itemCode = extractItemCodeFromDetails(log.getDetails());
                String action = log.getAction();
                int quantity = extractQuantityFromDetails(log.getDetails());
                String user = log.getUsername();
                String timestamp = log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Object[] rowData = {itemCode, action, quantity, user, timestamp};
                tableModel.addRow(rowData);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading stock history: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String extractItemCodeFromDetails(String details) {
        
        
        return details.split(" ")[2];
    }

    private int extractQuantityFromDetails(String details) {
        
        
        String[] parts = details.split(" ");
        return Integer.parseInt(parts[parts.length - 2]);
    }

    private void searchHistory() {
        String searchText = searchField.getText().trim().toLowerCase();

        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            List<SystemLog> inventoryLogs = dbHelper.getInventoryLogs();

            tableModel.setRowCount(0);
            for (SystemLog log : inventoryLogs) {
                String itemCode = extractItemCodeFromDetails(log.getDetails());
                String action = log.getAction();
                int quantity = extractQuantityFromDetails(log.getDetails());
                String user = log.getUsername();
                String timestamp = log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                if (itemCode.toLowerCase().contains(searchText) ||
                        action.toLowerCase().contains(searchText) ||
                        user.toLowerCase().contains(searchText) ||
                        timestamp.toLowerCase().contains(searchText)) {
                    Object[] rowData = {itemCode, action, quantity, user, timestamp};
                    tableModel.addRow(rowData);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error searching stock history: " + ex.getMessage(),
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
            StockHistoryPage stockHistoryPage = new StockHistoryPage(currentUser);
            stockHistoryPage.setVisible(true);
        });
    }
}