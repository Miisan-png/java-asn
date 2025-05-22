package inv;

import admin.UIBase;
import database.DatabaseHelper;
import models.Stock;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class StockReportPage extends UIBase {
    private final User currentUser;
    private JTable stockTable;
    private DefaultTableModel tableModel;

    public StockReportPage(User user) {
        super("Stock Report");
        this.currentUser = user;
    }

    @Override
    protected void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.addActionListener(e -> {
            dispose();
            new InventoryDashboardPage(currentUser).setVisible(true);
        });

        JLabel title = new JLabel("📦 Stock Report", SwingConstants.CENTER);
        title.setFont(headerFont);
        title.setForeground(primaryColor);

        topPanel.add(backBtn, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Quantity", "Location", "Last Updated", "Status"}, 0);
        stockTable = new JTable(tableModel);
        stockTable.setFillsViewportHeight(true);
        stockTable.setRowHeight(24);
        stockTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        root.add(scrollPane, BorderLayout.CENTER);

        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        JButton generateBtn = new JButton("📝 Generate Full Report");
        generateBtn.setPreferredSize(new Dimension(200, 35));
        generateBtn.addActionListener(e -> showReportPopup());
        actionPanel.add(generateBtn);
        root.add(actionPanel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        loadStockData();
    }

    private void loadStockData() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            List<Stock> stockList = db.getAllStock();
            tableModel.setRowCount(0);
            for (Stock s : stockList) {
                tableModel.addRow(new Object[]{
                    s.getItemCode(), s.getItemName(), s.getQuantity(),
                    s.getLocation(), s.getLastUpdated(), s.getStatus()
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading stock data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReportPopup() {
        JDialog reportDialog = new JDialog(this, "Inventory Report", true);
        reportDialog.setSize(800, 500);
        reportDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel heading = new JLabel("📋 Full Inventory Report", SwingConstants.CENTER);
        heading.setFont(new Font("Serif", Font.BOLD, 20));
        heading.setForeground(primaryColor);
        heading.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(heading, BorderLayout.NORTH);

        JTable reportTable = new JTable(tableModel);
        reportTable.setEnabled(false);
        reportTable.setRowHeight(25);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> reportDialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(closeBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        reportDialog.setContentPane(panel);
        reportDialog.setVisible(true);
    }
}
