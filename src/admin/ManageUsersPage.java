package admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.User;
import models.SystemLog;
import database.DatabaseHelper;

public class ManageUsersPage extends UIBase {
    
    private final User currentUser;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private List<User> usersList;
    
    public ManageUsersPage(User user) {
        super("Manage User");
        this.currentUser = user;
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            loadUsers();
        }
        super.setVisible(visible);
    }

    public void loadUsers() {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            usersList = dbHelper.getAllUsers();
            
            if (usersList == null) {
                usersList = new ArrayList<>();
            } else {
                filterUsers(null);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            usersList = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                    "Error loading users: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            usersList = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                    "Unexpected error: " + ex.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterUsers(String role) {
        tableModel.setRowCount(0);
        for (User user : usersList) {
            if (user != null && (role == null || user.getRole().equals(role))) {
                if (currentUser != null &&
                    currentUser.getUserId().equals(user.getUserId()) &&
                    currentUser.getRole().equals(User.ROLE_ADMINISTRATOR)) {
                    continue;
                }
                Object[] rowData = {
                    user.getUserId(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getRole()
                };
                tableModel.addRow(rowData);
            }
        }
    }

    @Override
    protected void initUI() {
        usersList = new ArrayList<>();
        
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
        
        JPanel manageUserItem = createMenuItem("Manage User", true);
        
        menuPanel.add(dashboardItem);
        menuPanel.add(manageUserItem);
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
                ManageUsersPage.this,
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
                           : "Admin";
        
        JLabel userLabel = new JLabel(displayName + " ▾");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.add(userLabel);
        
        topContainer.add(userPanel, BorderLayout.NORTH);
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel title = new JLabel("Manage User");
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
    
        String[] columnNames = {"User ID", "Username", "Password", "Email", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getTableHeader().setBackground(new Color(240, 240, 240));
        usersTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        usersTable.setRowHeight(30);
        usersTable.setGridColor(Color.LIGHT_GRAY);
    
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
    
        JButton filterBtn = new JButton("Filter Users ▼");
        styleButton(filterBtn);
        createFilterPopup(filterBtn);
        buttonsPanel.add(filterBtn);
    
        JButton addButton = new JButton("Add User");
        styleButton(addButton);
        addButton.addActionListener(e -> handleAddUser());
        buttonsPanel.add(addButton);
    
        JButton editButton = new JButton("Edit User");
        styleButton(editButton);
        editButton.addActionListener(e -> handleEditUser());
        buttonsPanel.add(editButton);
    
        JButton deleteButton = new JButton("Delete User");
        styleButton(deleteButton);
        deleteButton.addActionListener(e -> handleDeleteUser());
        buttonsPanel.add(deleteButton);
    
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);
    
        return contentPanel;
    }
    
    private void createFilterPopup(JButton filterBtn) {
        JPopupMenu filterMenu = new JPopupMenu();
        filterMenu.setBackground(Color.WHITE);
    
        String[] roles = {
            "All Users",
            User.ROLE_ADMINISTRATOR,
            User.ROLE_INVENTORY_MANAGER,
            User.ROLE_PURCHASE_MANAGER,
            User.ROLE_FINANCE_MANAGER,
            User.ROLE_SALES_MANAGER
        };
    
        for (String role : roles) {
            JMenuItem item = new JMenuItem(role);
            item.setFont(new Font("SansSerif", Font.PLAIN, 14));
            item.setBackground(Color.WHITE);
            item.addActionListener(e -> {
                filterUsers(role.equals("All Users") ? null : role);
                filterBtn.setText("Filter by: " + role + " ▼");
            });
            filterMenu.add(item);
        }
    
        filterBtn.addActionListener(e -> {
            filterMenu.show(filterBtn, 0, filterBtn.getHeight());
        });
    }

    private void handleAddUser() {
    JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JTextField usernameField = new JTextField();
    JTextField emailField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JPasswordField confirmPasswordField = new JPasswordField();

    String[] roles = {
        User.ROLE_ADMINISTRATOR,
        User.ROLE_INVENTORY_MANAGER,
        User.ROLE_PURCHASE_MANAGER,
        User.ROLE_FINANCE_MANAGER,
        User.ROLE_SALES_MANAGER
    };

    JComboBox<String> roleComboBox = new JComboBox<>(roles);
    roleComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
    roleComboBox.setBackground(Color.WHITE);

    formPanel.add(new JLabel("Username:"));
    formPanel.add(usernameField);
    formPanel.add(new JLabel("Email:"));
    formPanel.add(emailField);
    formPanel.add(new JLabel("Password:"));
    formPanel.add(passwordField);
    formPanel.add(new JLabel("Confirm Password:"));
    formPanel.add(confirmPasswordField);
    formPanel.add(new JLabel("Role:"));
    formPanel.add(roleComboBox);

    int result = JOptionPane.showConfirmDialog(
        this,
        formPanel,
        "Add New User",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this,
                "All fields are required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            User newUser = new User("testuser", "Test User", "password", "purchasing");
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setConfirmPassword(confirmPassword);
            newUser.setEmail(email);
            newUser.setRole(role);
            dbHelper.registerUser(newUser);

            JOptionPane.showMessageDialog(this,
                "User registered successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            logSystemAction(SystemLog.ACTION_CREATE, "Added new user: " + username);
            loadUsers();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Registration Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void handleEditUser() {
    int selectedRow = usersTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this,
            "Please select a user to edit.",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    String userId = (String) usersTable.getValueAt(selectedRow, 0);
    String username = (String) usersTable.getValueAt(selectedRow, 1);
    String password = (String) usersTable.getValueAt(selectedRow, 2);
    String email = (String) usersTable.getValueAt(selectedRow, 3);
    String role = (String) usersTable.getValueAt(selectedRow, 4);

    JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JTextField usernameField = new JTextField(username);
    JTextField emailField = new JTextField(email);
    JPasswordField passwordField = new JPasswordField(password);

    String[] roles = {
        User.ROLE_ADMINISTRATOR,
        User.ROLE_INVENTORY_MANAGER,
        User.ROLE_PURCHASE_MANAGER,
        User.ROLE_FINANCE_MANAGER,
        User.ROLE_SALES_MANAGER
    };

    JComboBox<String> roleComboBox = new JComboBox<>(roles);
    roleComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
    roleComboBox.setBackground(Color.WHITE);
    roleComboBox.setSelectedItem(role);

    formPanel.add(new JLabel("User ID: " + userId));
    formPanel.add(new JLabel("")); // Empty cell
    formPanel.add(new JLabel("Username:"));
    formPanel.add(usernameField);
    formPanel.add(new JLabel("Email:"));
    formPanel.add(emailField);
    formPanel.add(new JLabel("Password:"));
    formPanel.add(passwordField);
    formPanel.add(new JLabel("Role:"));
    formPanel.add(roleComboBox);

    int result = JOptionPane.showConfirmDialog(
        this,
        formPanel,
        "Edit User",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPassword = new String(passwordField.getPassword());
        String newRole = (String) roleComboBox.getSelectedItem();

        if (newUsername.isEmpty() || newEmail.isEmpty() || newPassword.isEmpty() || newRole == null) {
            JOptionPane.showMessageDialog(this,
                "All fields are required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            User updatedUser = new User(userId, newUsername, newPassword, newEmail, newRole);
            dbHelper.updateUser(updatedUser);

            JOptionPane.showMessageDialog(this,
                "User updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            logSystemAction(SystemLog.ACTION_UPDATE, "Updated user: " + newUsername);
            loadUsers();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Update Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void handleDeleteUser() {
    int selectedRow = usersTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this,
            "Please select a user to delete.",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    String userId = (String) usersTable.getValueAt(selectedRow, 0);
    String username = (String) usersTable.getValueAt(selectedRow, 1);

    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to delete user '" + username + "'?",
        "Confirm Delete",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            dbHelper.deleteUser(userId);

            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this,
                "User deleted successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            logSystemAction(SystemLog.ACTION_DELETE, "Deleted user: " + username);
            loadUsers();

        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Error deleting user: " + ex.getMessage(),
                "Delete Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

    

    private void styleButton(JButton button) {
        button.setBackground(new Color(120, 120, 120));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 40));
    }

    private void goBackToDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            DashboardPage dashboard = new DashboardPage(currentUser);
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