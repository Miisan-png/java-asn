package inv;

import admin.UIBase;
import database.DatabaseHelper;
import models.SystemLog;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ViewInventoryLogsPage extends UIBase {
    private final User currentUser;
    private JTable logTable;
    private DefaultTableModel tableModel;

    public ViewInventoryLogsPage(User user) {
        super("Inventory Logs");
        this.currentUser = user;
    }

    @Override
    protected void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.addActionListener(e -> {
            dispose();
            new InventoryDashboardPage(currentUser).setVisible(true);
        });

        JLabel title = new JLabel("🧾 Inventory Logs", SwingConstants.CENTER);
        title.setFont(headerFont);
        title.setForeground(primaryColor);

        topPanel.add(backBtn, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);

        
        tableModel = new DefaultTableModel(new Object[]{"Log ID", "Username", "Action", "Details", "Timestamp"}, 0);
        logTable = new JTable(tableModel);
        logTable.setFillsViewportHeight(true);
        logTable.setRowHeight(24);
        logTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(logTable);
        root.add(scrollPane, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        loadInventoryLogs();
    }

    private void loadInventoryLogs() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            List<SystemLog> logs = db.getInventoryLogs();

            tableModel.setRowCount(0);
            for (SystemLog log : logs) {
                if (log.getUserRole().equalsIgnoreCase("inventory")) {
                    tableModel.addRow(new Object[]{
                        log.getLogId(),
                        log.getUsername(),
                        log.getAction(),
                        log.getDetails(),
                        log.getTimestamp().toString()
                    });
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading logs: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
