package inv;

import database.DatabaseHelper;
import models.Stock;
import models.User;

import javax.swing.*;
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
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JButton back = new JButton("← Back");
        back.addActionListener(e -> goBackToDashboard());
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("View Current Stock", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setForeground(new Color(0, 102, 204));
        header.add(title, BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Quantity"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        stockTable = new JTable(tableModel);
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
        JScrollPane scroll = new JScrollPane(stockTable);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        main.add(scroll, BorderLayout.CENTER);

        // Filter bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Filter By");
        filterLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton lowStock = createFilterButton("Low Stock", this::filterLowStock);
        JButton byName = createFilterButton("Item Name", this::sortByName);
        JButton byQty = createFilterButton("Quantity", this::sortByQuantity);

        filterPanel.add(filterLabel);
        filterPanel.add(lowStock);
        filterPanel.add(byName);
        filterPanel.add(byQty);

        main.add(filterPanel, BorderLayout.SOUTH);

        setContentPane(main);
        pack();
        setLocationRelativeTo(null);

        loadStockData();
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
        stockTable.getRowSorter().toggleSortOrder(1); // Column 1 = Item Name
    }

    private void sortByQuantity() {
        stockTable.getRowSorter().toggleSortOrder(2); // Column 2 = Quantity
    }

    private void goBackToDashboard() {
        dispose();
        new InventoryDashboardPage(currentUser).setVisible(true);
    }
}
