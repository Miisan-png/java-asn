import admin.DashboardPage;
import database.DatabaseHelper;
import finance.FinanceDashboardPage;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import models.User;
import purchase.PurchaseDashboardPage;
import sales.SalesDashboardPage;

public class LoginPage extends UIBase {
    public static final String APP_TITLE = "Automated Purchase Order Management System";
    private static final String USERNAME_PLACEHOLDER = "Username";
    private static final String PASSWORD_PLACEHOLDER = "Password";

    public LoginPage() {
        super(APP_TITLE);
    }

    private static void addPlaceholderStyle(JTextComponent textComponent, String placeholder, Color placeholderColor, Color defaultForegroundColor) {
        textComponent.setText(placeholder);
        textComponent.setForeground(placeholderColor);

        char defaultEchoChar = 0;
        if (textComponent instanceof JPasswordField) {
            defaultEchoChar = ((JPasswordField) textComponent).getEchoChar();
            ((JPasswordField) textComponent).setEchoChar((char) 0);
        }
        final char finalDefaultEchoChar = defaultEchoChar;

        textComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textComponent.getText().equals(placeholder) && textComponent.getForeground().equals(placeholderColor)) {
                    textComponent.setText("");
                    textComponent.setForeground(defaultForegroundColor);
                    if (textComponent instanceof JPasswordField) {
                        ((JPasswordField) textComponent).setEchoChar(finalDefaultEchoChar);
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textComponent.getText().isEmpty()) {
                    textComponent.setText(placeholder);
                    textComponent.setForeground(placeholderColor);
                    if (textComponent instanceof JPasswordField) {
                        ((JPasswordField) textComponent).setEchoChar((char) 0);
                    }
                }
            }
        });
    }

    @Override
protected void initUI() {
    JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
    rootPanel.setBackground(Color.WHITE);
    rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(Color.WHITE);

    JLabel titleLabel = new JLabel(APP_TITLE, SwingConstants.CENTER);
    titleLabel.setFont(headerFont);
    titleLabel.setForeground(primaryColor);
    headerPanel.add(titleLabel, BorderLayout.NORTH);

    JLabel subtitleLabel = new JLabel("Login to your Account to get started or manage your inventory", SwingConstants.CENTER);
    subtitleLabel.setFont(subtitleFont);
    subtitleLabel.setForeground(Color.DARK_GRAY);
    headerPanel.add(subtitleLabel, BorderLayout.CENTER);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

    rootPanel.add(headerPanel, BorderLayout.NORTH);

    JPanel formContainerPanel = new JPanel(new GridBagLayout());
    formContainerPanel.setBackground(Color.WHITE);
    rootPanel.add(formContainerPanel, BorderLayout.CENTER);

    JPanel actualFormPanel = new JPanel(new GridBagLayout());
    actualFormPanel.setBackground(panelColor);
    actualFormPanel.setBorder(formPanelBorder);

    GridBagConstraints gbcForm = new GridBagConstraints();
    gbcForm.fill = GridBagConstraints.HORIZONTAL;
    gbcForm.weightx = 1.0;
    gbcForm.gridx = 0;
    gbcForm.gridwidth = 1;
    gbcForm.anchor = GridBagConstraints.CENTER;

    gbcForm.gridy = 0;
    gbcForm.insets = new Insets(15, 5, 10, 5);

    JTextField userField = new JTextField();
    userField.setFont(inputFont);
    userField.setBorder(INPUT_FIELD_BORDER);
    addPlaceholderStyle(userField, USERNAME_PLACEHOLDER, placeholderColor, UIManager.getColor("TextField.foreground"));
    actualFormPanel.add(userField, gbcForm);

    gbcForm.gridy++;
    gbcForm.insets = new Insets(10, 5, 10, 5);

    JPasswordField passField = new JPasswordField();
    passField.setFont(inputFont);
    passField.setBorder(INPUT_FIELD_BORDER);
    addPlaceholderStyle(passField, PASSWORD_PLACEHOLDER, placeholderColor, UIManager.getColor("PasswordField.foreground"));
    actualFormPanel.add(passField, gbcForm);

    gbcForm.gridy++;
    gbcForm.insets = new Insets(15, 5, 10, 5);

    JButton loginBtn = new JButton("Login");
    loginBtn.setFont(buttonTextFont);
    loginBtn.setBackground(primaryColor);
    loginBtn.setForeground(Color.WHITE);
    actualFormPanel.add(loginBtn, gbcForm);

    GridBagConstraints formContainerGBC = new GridBagConstraints();
    formContainerGBC.anchor = GridBagConstraints.CENTER;
    formContainerGBC.weighty = 0.0;
    formContainerGBC.fill = GridBagConstraints.NONE;
    formContainerPanel.add(actualFormPanel, formContainerGBC);

    JPanel linksPanel = new JPanel(new GridBagLayout());
    linksPanel.setBackground(Color.WHITE);
    linksPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

    GridBagConstraints gbcLinks = new GridBagConstraints();
    gbcLinks.gridx = 0;
    gbcLinks.anchor = GridBagConstraints.CENTER;

    JLabel forgotLabel = new JLabel("Forgot Username or Password?");
    forgotLabel.setFont(smallLinkFont);
    forgotLabel.setForeground(primaryColor);
    forgotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    // Add forgot password functionality
    forgotLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            showForgotPasswordDialog();
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
            forgotLabel.setText("<html><u>Forgot Username or Password?</u></html>");
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            forgotLabel.setText("Forgot Username or Password?");
        }
    });

    JLabel createAccountLabel = new JLabel("<html>Create an Account</html>");
    createAccountLabel.setFont(smallLinkFont);
    createAccountLabel.setForeground(primaryColor);
    createAccountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    createAccountLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    createAccountLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            dispose();
            SwingUtilities.invokeLater(RegisterPage::new);
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            createAccountLabel.setText("<html><u>Create an Account</u></html>");
        }
        @Override
        public void mouseExited(MouseEvent e) {
            createAccountLabel.setText("<html>Create an Account</html>");
        }
    });

    gbcLinks.gridy = 0;
    linksPanel.add(forgotLabel, gbcLinks);

    gbcLinks.gridy++;
    linksPanel.add(Box.createRigidArea(new Dimension(0, 8)), gbcLinks);

    gbcLinks.gridy++;
    linksPanel.add(createAccountLabel, gbcLinks);

    rootPanel.add(linksPanel, BorderLayout.SOUTH);
    setContentPane(rootPanel);

    loginBtn.addActionListener(e -> {
        String username = userField.getText();
        if (username.equals(USERNAME_PLACEHOLDER) && userField.getForeground().equals(placeholderColor)) {
            username = "";
        }

        String password = new String(passField.getPassword());
        if (passField.getEchoChar() == (char)0 && password.equals(PASSWORD_PLACEHOLDER)) {
            password = "";
        }

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and Password cannot be empty.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DatabaseHelper dbHelper = new DatabaseHelper();
            User user = dbHelper.validateUser(username, password);
            if (user != null) {
                if (User.ROLE_ADMINISTRATOR.equals(user.getRole())) {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        DashboardPage dashboard = new DashboardPage(user);
                        dashboard.setVisible(true);
                    });
                } else if (User.ROLE_INVENTORY_MANAGER.equals(user.getRole())) {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        new inv.InventoryDashboardPage(user).setVisible(true);
                    });
                } else if (User.ROLE_PURCHASE_MANAGER.equals(user.getRole())) {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        new PurchaseDashboardPage(user).setVisible(true);
                    });
                } else if (User.ROLE_FINANCE_MANAGER.equals(user.getRole())) {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        new FinanceDashboardPage(user).setVisible(true);
                    });
                } else if (User.ROLE_SALES_MANAGER.equals(user.getRole())) {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        new SalesDashboardPage(user).setVisible(true);
                    });
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Welcome " + user.getUsername() + "!\nRole: " + user.getRole() + "\nThis role does not have a dashboard yet.",
                            "Login Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                addPlaceholderStyle(passField, PASSWORD_PLACEHOLDER, placeholderColor, UIManager.getColor("PasswordField.foreground"));
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error accessing user database: " + ex.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    });
}


private void showForgotPasswordDialog() {
    JDialog forgotDialog = new JDialog(this, "Forgot Password", true);
    forgotDialog.setLayout(new BorderLayout());
    forgotDialog.setSize(400, 200);
    forgotDialog.setLocationRelativeTo(this);
    
    JPanel contentPanel = new JPanel(new GridBagLayout());
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;
    
    JLabel instructionLabel = new JLabel("Enter your email or username:");
    instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.gridwidth = 2;
    contentPanel.add(instructionLabel, gbc);
    
    JTextField emailField = new JTextField(20);
    emailField.setFont(inputFont);
    emailField.setBorder(INPUT_FIELD_BORDER);
    gbc.gridx = 0; gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    contentPanel.add(emailField, gbc);
    
    JButton sendButton = new JButton("Send Request");
    sendButton.setBackground(primaryColor);
    sendButton.setForeground(Color.WHITE);
    sendButton.setFont(buttonTextFont);
    gbc.gridx = 0; gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(15, 5, 5, 5);
    contentPanel.add(sendButton, gbc);
    
    JButton cancelButton = new JButton("Cancel");
    cancelButton.setFont(buttonTextFont);
    gbc.gridx = 1; gbc.gridy = 2;
    contentPanel.add(cancelButton, gbc);
    
    forgotDialog.add(contentPanel, BorderLayout.CENTER);
    
    sendButton.addActionListener(e -> {
        String emailOrUsername = emailField.getText().trim();
        if (emailOrUsername.isEmpty()) {
            JOptionPane.showMessageDialog(forgotDialog,
                    "Please enter your email or username.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(forgotDialog,
                "Password reset request sent successfully!\nPlease check your email for further instructions.",
                "Request Sent",
                JOptionPane.INFORMATION_MESSAGE);
        forgotDialog.dispose();
    });
    
    cancelButton.addActionListener(e -> forgotDialog.dispose());
    
    emailField.addActionListener(e -> sendButton.doClick());
    
    forgotDialog.setVisible(true);
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}