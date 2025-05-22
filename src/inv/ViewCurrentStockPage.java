package inv;

import database.DatabaseHelper;
import models.Stock;
import models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ViewCurrentStockPage extends admin.UIBase {
    private final User currentUser;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ViewCurrentStockPage(User user) {
        super("View Current Stock");
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

        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Quantity", "Location", "Status"}, 0);
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.getTableHeader().setReorderingAllowed(false);
        stockTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        searchField.addActionListener(e -> searchStock());
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchStock());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel, BorderLayout.SOUTH);

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
                Object[] rowData = {
                        stock.getItemCode(),
                        stock.getItemName(),
                        stock.getQuantity(),
                        stock.getLocation(),
                        stock.getStatus()
                };
                tableModel.addRow(rowData);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading stock data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStock() {
        String searchText = searchField.getText().trim().toLowerCase();

        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            List<Stock> stockList = dbHelper.getAllStock();

            tableModel.setRowCount(0);
            for (Stock stock : stockList) {
                if (stock.getItemCode().toLowerCase().contains(searchText) ||
                        stock.getItemName().toLowerCase().contains(searchText) ||
                        stock.getLocation().toLowerCase().contains(searchText) ||
                        stock.getStatus().toLowerCase().contains(searchText)) {
                    Object[] rowData = {
                            stock.getItemCode(),
                            stock.getItemName(),
                            stock.getQuantity(),
                            stock.getLocation(),
                            stock.getStatus()
                    };
                    tableModel.addRow(rowData);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error searching stock: " + ex.getMessage(),
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
            ViewCurrentStockPage viewStockPage = new ViewCurrentStockPage(currentUser);
            viewStockPage.setVisible(true);
        });
    }
}