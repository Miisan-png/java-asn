// sales/SalesDataEntryPage.java
package sales;

import admin.UIBase;
import database.DatabaseHelper;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import models.SalesEntry;
import models.SystemLog;
import models.User;

public class SalesDataEntryPage extends UIBase {

    private final User currentUser;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextField dateField, itemCodeField, quantityField;

    public SalesDataEntryPage(User user) {
        super("Sales Data Entry");
        this.currentUser = user;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            loadSalesEntries();
        }
        super.setVisible(visible);
    }

    // Replace the loadSalesEntries() method in SalesDataEntryPage.java with this fixed version:

public void loadSalesEntries() {
    try {
        DatabaseHelper dbHelper = new DatabaseHelper();
        List<SalesEntry> allEntries = dbHelper.getAllSalesEntries();
        
        if (allEntries == null) {
            allEntries = new ArrayList<>();
        }
        
        // Filter entries for current user if user is not null
        List<SalesEntry> userEntries = new ArrayList<>();
        if (currentUser != null) {
            for (SalesEntry entry : allEntries) {
                if (entry != null && entry.getSalesManagerId() != null && 
                    entry.getSalesManagerId().equals(currentUser.getUserId())) {
                    userEntries.add(entry);
                }
            }
        } else {
            // If currentUser is null, show all entries (for debugging)
            System.out.println("Warning: Current user is null, showing all entries");
            userEntries = allEntries;
        }
        
        updateTable(userEntries);
        System.out.println("Loaded " + userEntries.size() + " sales entries for user: " + 
                          (currentUser != null ? currentUser.getUsername() : "null"));

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error loading sales entries: " + ex.getMessage(),
                "Data Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

// Also update the updateTable method to handle null currentUser better:
private void updateTable(List<SalesEntry> entries) {
    if (tableModel == null) {
        // Initialize the table model if it hasn't been created yet
        String[] columnNames = {"Date", "Item ID", "Item Name", "Quantity", "Category", "Price per unit", "Total price"};
        tableModel = new DefaultTableModel(columnNames, 0);
    }

    tableModel.setRowCount(0);

    if (entries == null) {
        return; // Don't try to update the table if entries are null
    }

    for (SalesEntry entry : entries) {
        if (entry != null) {
            Object[] rowData = {
                    entry.getDate(),
                    entry.getItemId(),
                    entry.getItemName() != null ? entry.getItemName() : "",
                    entry.getQuantity(),
                    entry.getCategory() != null ? entry.getCategory() : "",
                    String.format("%.2f", entry.getPricePerUnit()),
                    String.format("%.2f", entry.getTotalPrice())
            };
            tableModel.addRow(rowData);
        }
    }
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

        JLabel placeholder = new JLabel("OWSB", SwingConstants.CENTER);
        placeholder.setFont(new Font("Serif", Font.BOLD, 16));
        placeholder.setForeground(new Color(11, 61, 145));
        placeholder.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        logoPanel.add(placeholder, BorderLayout.CENTER);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JPanel dashboardItem = createMenuItem("Dashboard", false);
        dashboardItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                goBackToDashboard();
            }
        });

        JPanel salesEntryItem = createMenuItem("Sales Data Entry", true);

        menuPanel.add(dashboardItem);
        menuPanel.add(salesEntryItem);
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
                    SalesDataEntryPage.this,
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

        String displayName = (currentUser != null && currentUser.getUsername() != null && !currentUser.getUsername().isEmpty())
                ? currentUser.getUsername()
                : "User";

        JLabel userLabel = new JLabel(displayName + " ▾");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.add(userLabel);
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

        JLabel title = new JLabel("Sales Data Entry");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(11, 61, 145));

        headerPanel.add(title);

        topContainer.add(headerPanel, BorderLayout.SOUTH);

        return topContainer;
    }

    private JPanel createMenuItem(String text, boolean isSelected) {
        JPanel menuItem = new JPanel(new BorderLayout());
        menuItem.setBackground(isSelected ? new Color(230, 230, 230) : Color.WHITE);
        menuItem.setMaximumSize(new Dimension(200, 50));
        menuItem.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel menuLabel = new JLabel(text);
        menuLabel.setFont(new Font("Serif", Font.BOLD, 16));
        menuItem.add(menuLabel, BorderLayout.CENTER);

        return menuItem;
    }

    private JPanel createContentPanel() {
    JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
    contentPanel.setBackground(Color.WHITE);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Use GridLayout for better alignment of form fields
    JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    formPanel.setBackground(Color.WHITE);
    formPanel.setBorder(BorderFactory.createTitledBorder("Enter Sales Data"));

    dateField = new JTextField(10);
    itemCodeField = new JTextField(10);
    quantityField = new JTextField(10);

    formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
    formPanel.add(dateField);
    formPanel.add(new JLabel("Item Code:"));
    formPanel.add(itemCodeField);
    formPanel.add(new JLabel("Sales Quantity:"));
    formPanel.add(quantityField);

    // Add some padding around the form panel
    JPanel formContainer = new JPanel(new BorderLayout());
    formContainer.setBackground(Color.WHITE);
    formContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    formContainer.add(formPanel, BorderLayout.CENTER);

    contentPanel.add(formContainer, BorderLayout.NORTH);

    // Initialize table model BEFORE creating the table
    String[] columnNames = {"Date", "Item ID", "Item Name", "Quantity", "Category", "Price per unit", "Total price"};
    tableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make read-only for better data integrity
        }
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 3 || column == 5 || column == 6) {
                return Object.class;
            }
            return String.class;
        }
    };

    salesTable = new JTable(tableModel);
    salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    salesTable.getTableHeader().setBackground(new Color(240, 240, 240));
    salesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    salesTable.setRowHeight(30);
    salesTable.setGridColor(Color.LIGHT_GRAY);

    JScrollPane scrollPane = new JScrollPane(salesTable);
    scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    contentPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
    buttonsPanel.setBackground(Color.WHITE);
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

    JButton createButton = new JButton("Create");
    styleButton(createButton);
    createButton.addActionListener(e -> handleCreateEntry());
    buttonsPanel.add(createButton);

    JButton editButton = new JButton("Edit Entry");
    styleButton(editButton);
    editButton.addActionListener(e -> handleEditEntry());
    buttonsPanel.add(editButton);

    JButton deleteButton = new JButton("Delete");
    styleButton(deleteButton);
    deleteButton.addActionListener(e -> handleDeleteEntry());
    buttonsPanel.add(deleteButton);

    JButton saveButton = new JButton("Save");
    styleButton(saveButton);
    saveButton.addActionListener(e -> handleSaveEntries());
    buttonsPanel.add(saveButton);

    JButton resetButton = new JButton("Reset");
    styleButton(resetButton);
    resetButton.addActionListener(e -> handleResetForm());
    buttonsPanel.add(resetButton);

    contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

    return contentPanel;
}

    private void styleButton(JButton button) {
        button.setBackground(new Color(120, 120, 120));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40)); // Adjusted size for more buttons
    }

    private void handleDeleteEntry() {
    int selectedRow = salesTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select an entry to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Get the item ID from the selected row to identify the entry
    String itemId = (String) tableModel.getValueAt(selectedRow, 1); // Item ID column
    String itemName = (String) tableModel.getValueAt(selectedRow, 2); // Item Name column
    
    int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this sales entry?\n\n" +
            "Item: " + itemName + " (" + itemId + ")",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            List<SalesEntry> allEntries = dbHelper.getAllSalesEntries();
            
            // Find and remove the entry that matches the selected row
            boolean entryFound = false;
            for (int i = 0; i < allEntries.size(); i++) {
                SalesEntry entry = allEntries.get(i);
                if (entry != null && 
                    entry.getItemId().equals(itemId) && 
                    entry.getSalesManagerId().equals(currentUser.getUserId())) {
                    
                    // Found the matching entry, now delete it from database
                    dbHelper.deleteSalesEntry(entry.getEntryId());
                    entryFound = true;
                    break;
                }
            }
            
            if (entryFound) {
                // Remove from table display
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, 
                    "Sales entry deleted successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                logSystemAction(SystemLog.ACTION_DELETE, "Deleted sales entry for item: " + itemId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Could not find the sales entry to delete.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error deleting sales entry: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

private void handleCreateEntry() {
    String dateStr = dateField.getText().trim();
    String itemCode = itemCodeField.getText().trim();
    String quantityStr = quantityField.getText().trim();

    if (dateStr.isEmpty() || itemCode.isEmpty() || quantityStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Date, Item Code, and Quantity are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        LocalDate date = LocalDate.parse(dateStr);
        int quantity = Integer.parseInt(quantityStr);
        if (quantity <= 0) {
            throw new NumberFormatException("Quantity must be positive.");
        }

        DatabaseHelper dbHelper = new DatabaseHelper();
        String itemName = "";
        String category = "";
        double pricePerUnit = 0.0;

        try {
            models.Item item = dbHelper.getItemByCode(itemCode);
            if (item != null) {
                itemName = item.getItemName();
                pricePerUnit = item.getPricePerUnit(); // Use actual price from item
                
                // Determine category based on item name (simple categorization)
                if (itemName.toLowerCase().contains("milk") || itemName.toLowerCase().contains("cheese") || 
                    itemName.toLowerCase().contains("yogurt") || itemName.toLowerCase().contains("eggs")) {
                    category = "Dairy";
                } else if (itemName.toLowerCase().contains("bread") || itemName.toLowerCase().contains("rice") || 
                          itemName.toLowerCase().contains("pasta") || itemName.toLowerCase().contains("oats")) {
                    category = "Grains";
                } else if (itemName.toLowerCase().contains("banana") || itemName.toLowerCase().contains("apple") || 
                          itemName.toLowerCase().contains("orange")) {
                    category = "Fruits";
                } else if (itemName.toLowerCase().contains("tomato") || itemName.toLowerCase().contains("carrot") || 
                          itemName.toLowerCase().contains("potato") || itemName.toLowerCase().contains("spinach")) {
                    category = "Vegetables";
                } else if (itemName.toLowerCase().contains("chicken") || itemName.toLowerCase().contains("beef") || 
                          itemName.toLowerCase().contains("salmon")) {
                    category = "Meat & Seafood";
                } else {
                    category = "General";
                }
            } else {
                JOptionPane.showMessageDialog(this, "Item Code not found.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching item details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double totalPrice = quantity * pricePerUnit;

        // Generate a unique entry ID
        String entryId = "SALE" + System.currentTimeMillis();

        SalesEntry newEntry = new SalesEntry(entryId, date, itemCode, itemName, quantity, category, pricePerUnit, totalPrice, currentUser.getUserId());

        // Save to database immediately
        try {
            dbHelper.addSalesEntry(newEntry);
            
            // Add to the table model for immediate display
            tableModel.addRow(new Object[]{
                    newEntry.getDate(),
                    newEntry.getItemId(),
                    newEntry.getItemName(),
                    newEntry.getQuantity(),
                    newEntry.getCategory(),
                    String.format("%.2f", newEntry.getPricePerUnit()),
                    String.format("%.2f", newEntry.getTotalPrice())
            });

            JOptionPane.showMessageDialog(this, "Sales entry created and saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            logSystemAction(SystemLog.ACTION_CREATE, "Created new sales entry for item: " + itemCode);
            handleResetForm(); // Clear form after successful creation
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving sales entry: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

    } catch (DateTimeParseException ex) {
        JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

// Replace the handleEditEntry() method in SalesDataEntryPage.java with this:

private void handleEditEntry() {
    int selectedRow = salesTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select an entry to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Get data from the selected row
    Object dateObj = tableModel.getValueAt(selectedRow, 0);
    Object itemCodeObj = tableModel.getValueAt(selectedRow, 1);
    Object itemNameObj = tableModel.getValueAt(selectedRow, 2);
    Object quantityObj = tableModel.getValueAt(selectedRow, 3);
    Object categoryObj = tableModel.getValueAt(selectedRow, 4);
    Object pricePerUnitObj = tableModel.getValueAt(selectedRow, 5);

    // Create edit form
    JTextField editDateField = new JTextField(dateObj != null ? dateObj.toString() : "");
    JTextField editItemCodeField = new JTextField(itemCodeObj != null ? itemCodeObj.toString() : "");
    JTextField editItemNameField = new JTextField(itemNameObj != null ? itemNameObj.toString() : "");
    editItemNameField.setEditable(false); // Item name should be auto-populated from item code
    JTextField editQuantityField = new JTextField(quantityObj != null ? quantityObj.toString() : "");
    JTextField editCategoryField = new JTextField(categoryObj != null ? categoryObj.toString() : "");
    editCategoryField.setEditable(false); // Category should be auto-determined
    JTextField editPricePerUnitField = new JTextField(pricePerUnitObj != null ? pricePerUnitObj.toString() : "");
    editPricePerUnitField.setEditable(false); // Price should come from item master data

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Date (YYYY-MM-DD):"));
    panel.add(editDateField);
    panel.add(new JLabel("Item Code:"));
    panel.add(editItemCodeField);
    panel.add(new JLabel("Item Name: (Auto-filled)"));
    panel.add(editItemNameField);
    panel.add(new JLabel("Sales Quantity:"));
    panel.add(editQuantityField);
    panel.add(new JLabel("Category: (Auto-determined)"));
    panel.add(editCategoryField);
    panel.add(new JLabel("Price per unit: (From Item Master)"));
    panel.add(editPricePerUnitField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Edit Sales Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
        try {
            // Get updated values from fields
            String newDateStr = editDateField.getText().trim();
            String newItemCode = editItemCodeField.getText().trim();
            String newQuantityStr = editQuantityField.getText().trim();

            if (newDateStr.isEmpty() || newItemCode.isEmpty() || newQuantityStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Date, Item Code, and Quantity are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate newDate = LocalDate.parse(newDateStr);
            int newQuantity = Integer.parseInt(newQuantityStr);
            if (newQuantity <= 0) {
                throw new NumberFormatException("Quantity must be positive.");
            }

            // Get updated item information from database
            DatabaseHelper dbHelper = new DatabaseHelper();
            String newItemName = "";
            String newCategory = "";
            double newPricePerUnit = 0.0;

            try {
                models.Item item = dbHelper.getItemByCode(newItemCode);
                if (item != null) {
                    newItemName = item.getItemName();
                    newPricePerUnit = item.getPricePerUnit();
                    
                    // Determine category based on item name
                    if (newItemName.toLowerCase().contains("milk") || newItemName.toLowerCase().contains("cheese") || 
                        newItemName.toLowerCase().contains("yogurt") || newItemName.toLowerCase().contains("eggs")) {
                        newCategory = "Dairy";
                    } else if (newItemName.toLowerCase().contains("bread") || newItemName.toLowerCase().contains("rice") || 
                              newItemName.toLowerCase().contains("pasta") || newItemName.toLowerCase().contains("oats")) {
                        newCategory = "Grains";
                    } else if (newItemName.toLowerCase().contains("banana") || newItemName.toLowerCase().contains("apple") || 
                              newItemName.toLowerCase().contains("orange")) {
                        newCategory = "Fruits";
                    } else if (newItemName.toLowerCase().contains("tomato") || newItemName.toLowerCase().contains("carrot") || 
                              newItemName.toLowerCase().contains("potato") || newItemName.toLowerCase().contains("spinach")) {
                        newCategory = "Vegetables";
                    } else if (newItemName.toLowerCase().contains("chicken") || newItemName.toLowerCase().contains("beef") || 
                              newItemName.toLowerCase().contains("salmon")) {
                        newCategory = "Meat & Seafood";
                    } else {
                        newCategory = "General";
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Item Code not found.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error fetching item details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double newTotalPrice = newQuantity * newPricePerUnit;

            // Find the original entry to update
            try {
                List<SalesEntry> allEntries = dbHelper.getAllSalesEntries();
                String originalItemCode = (String) itemCodeObj;
                
                // Find the matching entry
                SalesEntry entryToUpdate = null;
                for (SalesEntry entry : allEntries) {
                    if (entry != null && 
                        entry.getItemId().equals(originalItemCode) && 
                        entry.getSalesManagerId().equals(currentUser.getUserId()) &&
                        entry.getDate().toString().equals(dateObj.toString())) {
                        entryToUpdate = entry;
                        break;
                    }
                }

                if (entryToUpdate != null) {
                    // Update the entry
                    entryToUpdate.setDate(newDate);
                    entryToUpdate.setItemId(newItemCode);
                    entryToUpdate.setItemName(newItemName);
                    entryToUpdate.setQuantity(newQuantity);
                    entryToUpdate.setCategory(newCategory);
                    entryToUpdate.setPricePerUnit(newPricePerUnit);
                    entryToUpdate.setTotalPrice(newTotalPrice);
                    
                    // Save to database
                    dbHelper.updateSalesEntry(entryToUpdate);

                    // Update the table model
                    tableModel.setValueAt(newDate, selectedRow, 0);
                    tableModel.setValueAt(newItemCode, selectedRow, 1);
                    tableModel.setValueAt(newItemName, selectedRow, 2);
                    tableModel.setValueAt(newQuantity, selectedRow, 3);
                    tableModel.setValueAt(newCategory, selectedRow, 4);
                    tableModel.setValueAt(String.format("%.2f", newPricePerUnit), selectedRow, 5);
                    tableModel.setValueAt(String.format("%.2f", newTotalPrice), selectedRow, 6);

                    JOptionPane.showMessageDialog(this, "Sales entry updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    logSystemAction(SystemLog.ACTION_UPDATE, "Edited sales entry for item: " + newItemCode);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not find the original entry to update.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error updating sales entry: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}



    

    private void handleSaveEntries() {
    try {
        loadSalesEntries();
        
        JOptionPane.showMessageDialog(this, 
            "All sales entries are saved!\nData refreshed from database.", 
            "Save Complete", 
            JOptionPane.INFORMATION_MESSAGE);
        logSystemAction(SystemLog.ACTION_UPDATE, "Refreshed sales entries data");
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Error during save operation: " + ex.getMessage(),
            "Save Error",
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}


    private void handleResetForm() {
        dateField.setText("");
        itemCodeField.setText("");
        quantityField.setText("");
    }

    private void goBackToDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            SalesDashboardPage dashboard = new SalesDashboardPage(currentUser);
            dashboard.setVisible(true);
        });
    }
    private void logSystemAction(String action, String details) {
        try {
            DatabaseHelper db = new DatabaseHelper();
            SystemLog log = new SystemLog(
                    "LOG" + System.currentTimeMillis(),
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    action,
                    details,
                    LocalDateTime.now(),
                    currentUser.getRole()
            );
            db.addSystemLog(log);
        } catch (IOException e) {
            System.out.println("Failed to log system action: " + e.getMessage());
        }
    }
}