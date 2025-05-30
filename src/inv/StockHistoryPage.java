package inv;

import database.DatabaseHelper;
import models.SystemLog;
import models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
        
        JLabel titleLabel = new JLabel("Stock Activity History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create table with simplified columns
        tableModel = new DefaultTableModel(new Object[]{"Log ID", "User", "Action", "Activity Details", "Date & Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setFillsViewportHeight(true);
        historyTable.setRowHeight(35);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(240, 240, 240));
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Log ID
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(120); // User
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Action
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(400); // Details
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Date
        
        // Custom renderer for action column
        historyTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected && value != null) {
                    String action = value.toString();
                    switch (action.toLowerCase()) {
                        case "update" -> {
                            c.setBackground(new Color(212, 237, 218));
                            c.setForeground(new Color(40, 167, 69));
                        }
                        case "alert" -> {
                            c.setBackground(new Color(255, 243, 205));
                            c.setForeground(new Color(255, 145, 0));
                        }
                        case "quality" -> {
                            c.setBackground(new Color(217, 237, 247));
                            c.setForeground(new Color(58, 135, 173));
                        }
                        default -> {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    }
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        searchField = new JTextField(25);
        searchField.addActionListener(e -> searchHistory());
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchHistory());
        
        JButton clearButton = new JButton("Show All");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadStockHistory();
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
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
            
            if (inventoryLogs != null) {
                for (SystemLog log : inventoryLogs) {
                    if (log != null) {
                        try {
                            String logId = log.getLogId() != null ? log.getLogId() : "N/A";
                            String user = log.getUsername() != null ? log.getUsername() : "Unknown";
                            String action = log.getAction() != null ? log.getAction() : "N/A";
                            String details = log.getDetails() != null ? log.getDetails() : "No details available";
                            String timestamp = log.getTimestamp() != null ? 
                                log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A";

                            Object[] rowData = {logId, user, action, details, timestamp};
                            tableModel.addRow(rowData);
                            
                        } catch (Exception e) {
                            // Skip malformed entries
                            System.out.println("Skipping malformed log entry: " + e.getMessage());
                        }
                    }
                }
            }
            
            // Show count
            JOptionPane.showMessageDialog(this, 
                "Loaded " + tableModel.getRowCount() + " stock history records", 
                "History Loaded", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading stock history: " + ex.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchHistory() {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadStockHistory();
            return;
        }

        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            List<SystemLog> inventoryLogs = dbHelper.getInventoryLogs();

            tableModel.setRowCount(0);
            int matchCount = 0;
            
            if (inventoryLogs != null) {
                for (SystemLog log : inventoryLogs) {
                    if (log != null) {
                        try {
                            String logId = log.getLogId() != null ? log.getLogId() : "";
                            String user = log.getUsername() != null ? log.getUsername() : "";
                            String action = log.getAction() != null ? log.getAction() : "";
                            String details = log.getDetails() != null ? log.getDetails() : "";
                            String timestamp = log.getTimestamp() != null ? 
                                log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";

                            // Check if any field contains the search text
                            if (logId.toLowerCase().contains(searchText) ||
                                user.toLowerCase().contains(searchText) ||
                                action.toLowerCase().contains(searchText) ||
                                details.toLowerCase().contains(searchText) ||
                                timestamp.toLowerCase().contains(searchText)) {
                                
                                Object[] rowData = {logId, user, action, details, timestamp};
                                tableModel.addRow(rowData);
                                matchCount++;
                            }
                            
                        } catch (Exception e) {
                            // Skip malformed entries
                        }
                    }
                }
            }
            
            // Show search results count
            if (matchCount == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No records found matching: " + searchText, 
                    "Search Results", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error searching stock history: " + ex.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User currentUser = new User("John Doe", "johndoe", "password", "inventory");
            StockHistoryPage stockHistoryPage = new StockHistoryPage(currentUser);
            stockHistoryPage.setVisible(true);
        });
    }
}