import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class TK extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private JTextField loginPeselField = new JTextField(15);
    private JPasswordField loginPasswordField = new JPasswordField(15);
    private JTextField signupNameField = new JTextField(15);
    private JTextField signupSurnameField = new JTextField(15);
    private JTextField signupPeselField = new JTextField(15);
    private JPasswordField signupPasswordField = new JPasswordField(15);

    private JTextArea infoArea = new JTextArea();
    private JTextField kwotaField = new JTextField(10);
    private JTextField kontoDoceloweField = new JTextField(10);

    private PrintWriter out;
    private BufferedReader in;
    private String numerKonta;

    public TK() {
        setTitle("Terminal Klienta");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Nie udało się połączyć z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Ekran logowania
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; loginPanel.add(new JLabel("PESEL:"), gbc);
        gbc.gridx = 1; loginPanel.add(loginPeselField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; loginPanel.add(new JLabel("Hasło:"), gbc);
        gbc.gridx = 1; loginPanel.add(loginPasswordField, gbc);

        JButton loginBtn = new JButton("Zaloguj się");
        JButton goToSignupBtn = new JButton("Rejestracja");
        gbc.gridx = 0; gbc.gridy = 2; loginPanel.add(loginBtn, gbc);
        gbc.gridx = 1; loginPanel.add(goToSignupBtn, gbc);

        loginBtn.addActionListener(e -> login());
        goToSignupBtn.addActionListener(e -> cardLayout.show(mainPanel, "signup"));

        // Ekran rejestracji
        JPanel signupPanel = new JPanel(new GridBagLayout());
        signupPanel.setBackground(new Color(255, 248, 230));
        gbc.gridx = 0; gbc.gridy = 0; signupPanel.add(new JLabel("Imię:"), gbc);
        gbc.gridx = 1; signupPanel.add(signupNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; signupPanel.add(new JLabel("Nazwisko:"), gbc);
        gbc.gridx = 1; signupPanel.add(signupSurnameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; signupPanel.add(new JLabel("PESEL:"), gbc);
        gbc.gridx = 1; signupPanel.add(signupPeselField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; signupPanel.add(new JLabel("Hasło:"), gbc);
        gbc.gridx = 1; signupPanel.add(signupPasswordField, gbc);

        JButton signupBtn = new JButton("Zarejestruj się");
        JButton backToLoginBtn = new JButton("Powrót");
        gbc.gridx = 0; gbc.gridy = 4; signupPanel.add(signupBtn, gbc);
        gbc.gridx = 1; signupPanel.add(backToLoginBtn, gbc);

        signupBtn.addActionListener(e -> signup());
        backToLoginBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        // Panel główny użytkownika
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(245, 255, 245));
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(infoArea);
        userPanel.add(scroll, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        JButton saldoBtn = new JButton("Sprawdź saldo");
        JButton wplataBtn = new JButton("Wpłać");
        JButton wyplataBtn = new JButton("Wypłać");
        JButton przelewBtn = new JButton("Przelej");

        actionPanel.add(saldoBtn);
        actionPanel.add(wplataBtn);
        actionPanel.add(wyplataBtn);
        actionPanel.add(przelewBtn);
        userPanel.add(actionPanel, BorderLayout.NORTH);

        JPanel przelewPanel = new JPanel();
        przelewPanel.add(new JLabel("Kwota:"));
        przelewPanel.add(kwotaField);
        przelewPanel.add(new JLabel("Do konta:"));
        przelewPanel.add(kontoDoceloweField);
        userPanel.add(przelewPanel, BorderLayout.SOUTH);

        saldoBtn.addActionListener(e -> send("BALANCE"));
        wplataBtn.addActionListener(e -> sendWithAmount("DEPOSIT"));
        wyplataBtn.addActionListener(e -> sendWithAmount("WITHDRAW"));
        przelewBtn.addActionListener(e -> sendTransfer());

        mainPanel.add(loginPanel, "login");
        mainPanel.add(signupPanel, "signup");
        mainPanel.add(userPanel, "user");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
        setVisible(true);
    }

    private void login() {
        String pesel = loginPeselField.getText();
        String haslo = new String(loginPasswordField.getPassword());
        out.println("LOGIN;" + pesel + ";" + haslo);
        try {
            String response = in.readLine();
            if (response.startsWith("ERROR")) {
                JOptionPane.showMessageDialog(this, response, "Błąd logowania", JOptionPane.ERROR_MESSAGE);
            } else {
                infoArea.setText("Witaj, " + response + "\n\n");
                cardLayout.show(mainPanel, "user");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void signup() {
        String imie = signupNameField.getText();
        String nazwisko = signupSurnameField.getText();
        String pesel = signupPeselField.getText();
        String haslo = new String(signupPasswordField.getPassword());

        out.println("SIGNUP;" + imie + ";" + nazwisko + ";" + pesel + ";" + haslo);
        try {
            String response = in.readLine();
            JOptionPane.showMessageDialog(this, response);
            if (!response.startsWith("ERROR")) {
                cardLayout.show(mainPanel, "login");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void send(String komenda) {
        out.println(komenda);
        try {
            infoArea.append("\n" + in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWithAmount(String komenda) {
        try {
            double kwota = Double.parseDouble(kwotaField.getText());
            if (kwota <= 0) {
                JOptionPane.showMessageDialog(this, "Kwota musi być większa od zera.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }
            out.println(komenda + ";" + kwota);
            infoArea.append("\n" + in.readLine());
        } catch (NumberFormatException | IOException e) {
            infoArea.append("\nBłąd kwoty");
        }
    }

    private void sendTransfer() {
        try {
            double kwota = Double.parseDouble(kwotaField.getText());
            String konto = kontoDoceloweField.getText();
            if (kwota <= 0 || konto.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Wprowadź poprawną kwotę i numer konta.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }
            out.println("TRANSFER;" + konto + ";" + kwota);
            infoArea.append("\n" + in.readLine());
        } catch (NumberFormatException | IOException e) {
            infoArea.append("\nBłąd przelewu");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TK::new);
    }
}
