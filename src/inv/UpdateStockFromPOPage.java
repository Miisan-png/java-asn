package inv;

import database.DatabaseHelper;
import models.PurchaseOrder;
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

public class UpdateStockFromPOPage extends admin.UIBase {
    private final User currentUser;
    private JTable poTable;
    private DefaultTableModel tableModel;

    public UpdateStockFromPOPage(User user) {
        super("Update Stock From Approved PO");
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

        JPanel updateStockItem = createMenuItem("Update Approved Stock", true);
        JPanel dashboardItem = createMenuItem("Dashboard", false);
        dashboardItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        menuPanel.add(updateStockItem);
        menuPanel.add(dashboardItem);
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
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Log out?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });
        logoutPanel.add(logoutBtn);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

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

        JLabel title = new JLabel("Update Stock From Approved PO");
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

        tableModel = new DefaultTableModel(new Object[]{"PO ID", "Item Code", "Item Name", "Ordered Qty", "Received Qty"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 4; 
            }
        };

        poTable = new JTable(tableModel);
        poTable.setRowHeight(28);
        JScrollPane scrollPane = new JScrollPane(poTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setBackground(Color.WHITE);

        JButton confirmBtn = new JButton("Confirm Receipt");
        JButton updateQtyBtn = new JButton("Update Quantity");

        styleButton(confirmBtn);
        styleButton(updateQtyBtn);

        confirmBtn.addActionListener(e -> confirmReceipt());
        updateQtyBtn.addActionListener(e -> loadApprovedPO());

        buttons.add(confirmBtn);
        buttons.add(updateQtyBtn);

        content.add(scrollPane, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        loadApprovedPO();
        return content;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(96, 96, 96));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));
    }

    private void loadApprovedPO() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            List<PurchaseOrder> approvedOrders = db.getApprovedPurchaseOrders();
            tableModel.setRowCount(0);

            for (PurchaseOrder po : approvedOrders) {
                tableModel.addRow(new Object[]{
                        po.getOrderId(),
                        po.getItemCode(),
                        po.getItemName(),
                        po.getQuantity(),
                        po.getQuantity() 
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading PO data: " + e.getMessage());
        }
    }

    private void confirmReceipt() {
        try {
            DatabaseHelper db = new DatabaseHelper();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String itemCode = (String) tableModel.getValueAt(i, 1);
                int receivedQty = Integer.parseInt(tableModel.getValueAt(i, 4).toString());

                db.updateStockQuantity(itemCode, receivedQty, true);

                SystemLog log = new SystemLog(
                        "LOG" + System.currentTimeMillis(),
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        SystemLog.ACTION_UPDATE,
                        "Confirmed stock receipt for " + itemCode + " with qty " + receivedQty,
                        LocalDateTime.now(),
                        currentUser.getRole()
                );
                db.addInventoryLog(log);
            }

            JOptionPane.showMessageDialog(this, "Stock updated from POs successfully.");
            loadApprovedPO();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error confirming receipt: " + e.getMessage());
        }
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }
}