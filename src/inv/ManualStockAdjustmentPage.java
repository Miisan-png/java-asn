package inv;

import database.DatabaseHelper;
import models.Stock;
import models.SystemLog;
import models.User;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ManualStockAdjustmentPage extends admin.UIBase {
    private final User currentUser;
    private JTable stockTable;
    private DefaultTableModel tableModel;

    public ManualStockAdjustmentPage(User user) {
        super("Manual Stock Adjustment");
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

        JPanel backItem = createMenuItem("Dashboard", false);
        backItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        menuPanel.add(backItem);
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
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(120, 120, 120), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutBtn.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                    ManualStockAdjustmentPage.this,
                    "Are you sure you want to log out?",
                    "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (response == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        logoutPanel.add(logoutBtn);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createTopBar() {
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        userPanel.setBackground(new Color(180, 180, 180));
        userPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 20));

        JLabel bell = new JLabel("🔔");
        bell.setFont(new Font("SansSerif", Font.PLAIN, 16));
        bell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.add(bell);

        JLabel userLabel = new JLabel("User ▾");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.add(userLabel);

        SwingUtilities.invokeLater(() -> {
            if (currentUser != null && currentUser.getUsername() != null) {
                userLabel.setText(currentUser.getUsername().trim() + " ▾");
            }
        });

        userLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new admin.MyProfilePage(currentUser).setVisible(true);
            }
        });

        topContainer.add(userPanel, BorderLayout.NORTH);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("Manual Stock Adjustment");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(primaryColor);
        headerPanel.add(title);

        topContainer.add(headerPanel, BorderLayout.SOUTH);

        return topContainer;
    }

    private JPanel createMenuItem(String text, boolean selected) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(selected ? new Color(230, 230, 230) : Color.WHITE);
        item.setMaximumSize(new Dimension(200, 50));
        item.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
            public boolean isCellEditable(int row, int col) { return false; }
        };

        stockTable = new JTable(tableModel);
        stockTable.setRowHeight(28);
        stockTable.getTableHeader().setBackground(new Color(240, 240, 240));
        stockTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        stockTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stockTable.setSelectionBackground(new Color(232, 242, 254));
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setShowGrid(true);
        stockTable.setGridColor(new Color(230, 230, 230));
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton adjustBtn = new JButton("Adjust Stock");
        adjustBtn.setPreferredSize(new Dimension(150, 40));
        adjustBtn.setBackground(primaryColor);
        adjustBtn.setForeground(Color.WHITE);
        adjustBtn.setFocusPainted(false);
        adjustBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        adjustBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        adjustBtn.addActionListener(e -> openAdjustDialog());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        bottom.add(adjustBtn);

        content.add(scrollPane, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        loadStockData();
        return content;
    }

    private void openAdjustDialog() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to adjust.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
        int currentQty = (int) tableModel.getValueAt(selectedRow, 2);

        JTextField adjustmentField = new JTextField();
        JTextField reasonField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Current Quantity: " + currentQty));
        panel.add(new JLabel("Adjustment Amount:"));
        panel.add(adjustmentField);
        panel.add(new JLabel("Reason:"));
        panel.add(reasonField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Adjust Stock - " + itemCode, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int adjust = Integer.parseInt(adjustmentField.getText().trim());
                String reason = reasonField.getText().trim();
                if (reason.isEmpty()) throw new IllegalArgumentException("Reason is required");
                int newQty = currentQty + adjust;

                DatabaseHelper db = new DatabaseHelper();
                db.updateStockQuantity(itemCode, newQty);
                db.addInventoryLog(new SystemLog("LOG" + System.currentTimeMillis(), currentUser.getUserId(), currentUser.getUsername(), SystemLog.ACTION_UPDATE,
                        "Manual adjustment on " + itemCode + " by " + adjust + ". Reason: " + reason, LocalDateTime.now(), currentUser.getRole()));

                JOptionPane.showMessageDialog(this, "Stock adjusted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadStockData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Adjustment must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadStockData() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            List<Stock> stocks = db.getAllStock();
            tableModel.setRowCount(0);
            for (Stock s : stocks) {
                tableModel.addRow(new Object[]{s.getItemCode(), s.getItemName(), s.getQuantity()});
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading stock: " + ex.getMessage());
        }
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }
}