package inv;

import database.DatabaseHelper;
import models.Stock;
import models.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ViewCurrentStockPage extends admin.UIBase {
    private final User currentUser;
    private JTable stockTable;
    private DefaultTableModel tableModel;

    public ViewCurrentStockPage(User user) {
        super("View Current Stock");
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

        JPanel stockItem = createMenuItem("View Stock", true);
        stockItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel backItem = createMenuItem("Dashboard", false);
        backItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        menuPanel.add(stockItem);
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

        JLabel title = new JLabel("View Current Stock");
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
        stockTable.setAutoCreateRowSorter(true);
        stockTable.setRowHeight(28);
        stockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int quantity = (int) tableModel.getValueAt(row, 2);
                if (!isSelected) {
                    c.setBackground(quantity < 10 ? new Color(255, 224, 224) : Color.WHITE);
                } else {
                    c.setBackground(new Color(173, 216, 230));
                }
                return c;
            }
        });
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        filters.setBackground(Color.WHITE);

        JButton lowStock = createFilterButton("Low Stock", this::filterLowStock);
        JButton byName = createFilterButton("Item Name", this::sortByName);
        JButton byQty = createFilterButton("Quantity", this::sortByQuantity);

        filters.add(new JLabel("Filter By"));
        filters.add(lowStock);
        filters.add(byName);
        filters.add(byQty);

        content.add(scrollPane, BorderLayout.CENTER);
        content.add(filters, BorderLayout.SOUTH);

        loadStockData();
        return content;
    }

    private JButton createFilterButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setBackground(new Color(96, 96, 96));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
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
            JOptionPane.showMessageDialog(this, "Error loading stock data: " + ex.getMessage());
        }
    }

    private void filterLowStock() {
        try {
            DatabaseHelper db = new DatabaseHelper();
            List<Stock> stocks = db.getAllStock();
            tableModel.setRowCount(0);
            for (Stock s : stocks) {
                if (s.getQuantity() < 10) {
                    tableModel.addRow(new Object[]{s.getItemCode(), s.getItemName(), s.getQuantity()});
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error filtering stock: " + ex.getMessage());
        }
    }

    private void sortByName() {
        stockTable.getRowSorter().toggleSortOrder(1);
    }

    private void sortByQuantity() {
        stockTable.getRowSorter().toggleSortOrder(2);
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }
}