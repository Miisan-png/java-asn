package admin;

import database.DatabaseHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import models.User;

public class MyProfilePage extends UIBase {

    private final User currentUser;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public MyProfilePage(User user) {
    super("My Profile");
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


        setVisible(true);
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

        
        JLabel profileTitle = new JLabel("My Profile", SwingConstants.LEFT);
        profileTitle.setFont(new Font("Serif", Font.BOLD, 22));
        profileTitle.setForeground(new Color(11, 61, 145));
        profileTitle.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 0));
        sidebar.add(profileTitle, BorderLayout.CENTER);

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JPanel dashboardItem = createMenuItem("Dashboard", false);
        dashboardItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goBackToDashboard();
            }
        });

        JPanel profileItem = createMenuItem("My Profile", true);

        menuPanel.add(dashboardItem);
        menuPanel.add(profileItem);
        menuPanel.add(Box.createVerticalGlue());

        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.setBackground(Color.WHITE);
        sidebarContent.add(menuPanel, BorderLayout.NORTH);
        sidebar.add(sidebarContent, BorderLayout.CENTER);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setBackground(Color.WHITE);
        backPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setBackground(new Color(120, 120, 120));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backBtn.setPreferredSize(new Dimension(160, 35));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(120, 120, 120), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backBtn.addActionListener(e -> goBackToDashboard());

        backPanel.add(backBtn);
        sidebar.add(backPanel, BorderLayout.SOUTH);

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

    
        topContainer.add(userPanel, BorderLayout.NORTH);

        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

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

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(230, 240, 250)); 
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15); 
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.WEST; 

        String username = "";
        String email = "";

        if (currentUser != null) {
            username = currentUser.getUsername() != null ? currentUser.getUsername() : "";
            email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
        }

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = new JTextField(username);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 30)); 
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        emailField = new JTextField(email);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(200, 30)); 
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel currentPasswordLabel = new JLabel("Current Password:");
        currentPasswordLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        formPanel.add(currentPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        currentPasswordField.setPreferredSize(new Dimension(200, 30)); 
        currentPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(currentPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        formPanel.add(newPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        newPasswordField.setPreferredSize(new Dimension(200, 30)); 
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        confirmPasswordField.setPreferredSize(new Dimension(200, 30)); 
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(confirmPasswordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(new Color(11, 61, 145)); 
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> saveProfileChanges());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(150, 150, 150));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelButton.addActionListener(e -> goBackToDashboard());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        
        JPanel centeredContent = new JPanel(new BorderLayout());
        centeredContent.setBackground(Color.WHITE);

        
        JPanel paddedContainer = new JPanel(new BorderLayout());
        paddedContainer.setBackground(Color.WHITE);
        paddedContainer.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        paddedContainer.add(wrapper, BorderLayout.CENTER);

        centeredContent.add(paddedContainer, BorderLayout.CENTER);
        contentPanel.add(centeredContent, BorderLayout.CENTER);

        
        contentPanel.setBackground(new Color(240, 248, 255));  

        return contentPanel;
    }

    private void saveProfileChanges() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                    "Error: User data not available.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and email cannot be empty.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!currentPassword.equals(currentUser.getPassword())) {
            JOptionPane.showMessageDialog(this,
                    "Current password is incorrect.",
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean changePassword = !newPassword.isEmpty() || !confirmPassword.isEmpty();

        if (changePassword) {
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "New password and confirmation do not match.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            User updatedUser = new User(
                    currentUser.getUserId(),
                    username,
                    changePassword ? newPassword : currentUser.getPassword(),
                    email,
                    currentUser.getRole()
            );

            DatabaseHelper dbHelper = new DatabaseHelper();
            dbHelper.updateUser(updatedUser);

            JOptionPane.showMessageDialog(this,
                    "Profile updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            goBackToDashboard();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating profile: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void goBackToDashboard() {
    dispose();
        SwingUtilities.invokeLater(() -> {
            switch (currentUser.getRole()) {
                case User.ROLE_ADMINISTRATOR:
                    new admin.DashboardPage(currentUser).setVisible(true);
                    break;
                case User.ROLE_INVENTORY_MANAGER:
                    new inv.InventoryDashboardPage(currentUser).setVisible(true);
                    break;
                case User.ROLE_PURCHASE_MANAGER:
                    new purchase.PurchaseDashboardPage(currentUser).setVisible(true);
                    break;
                case User.ROLE_FINANCE_MANAGER:
                    new finance.FinanceDashboardPage(currentUser).setVisible(true);
                    break;
                case User.ROLE_SALES_MANAGER:
                    new sales.SalesDashboardPage(currentUser).setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Unrecognized role: " + currentUser.getRole());
                    System.exit(1);
            }
        });
    }
}