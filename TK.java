// TK.java – Terminal Klienta z GUI (Swing) + logowanie hasłem, rejestracja i walidacja kwot
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class TK {
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static JFrame frame;
    private static String saldo;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createWelcomeWindow());
    }

    private static void createWelcomeWindow() {
        frame = new JFrame("Terminal Klienta - Start");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new GridLayout(3, 1));

        JButton loginBtn = new JButton("Zaloguj się");
        JButton signupBtn = new JButton("Zarejestruj się");
        JButton exitBtn = new JButton("Wyjście");

        panel.add(loginBtn);
        panel.add(signupBtn);
        panel.add(exitBtn);

        loginBtn.addActionListener(e -> {
            frame.dispose();
            createLoginWindow();
        });

        signupBtn.addActionListener(e -> {
            frame.dispose();
            createSignupWindow();
        });

        exitBtn.addActionListener(e -> System.exit(0));

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void createLoginWindow() {
        frame = new JFrame("Terminal Klienta - Logowanie");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);

        JLabel peselLabel = new JLabel("PESEL:");
        peselLabel.setBounds(10, 20, 80, 25);
        panel.add(peselLabel);

        JTextField peselText = new JTextField(20);
        peselText.setBounds(100, 20, 165, 25);
        panel.add(peselText);

        JLabel passLabel = new JLabel("Hasło:");
        passLabel.setBounds(10, 60, 80, 25);
        panel.add(passLabel);

        JPasswordField passText = new JPasswordField();
        passText.setBounds(100, 60, 165, 25);
        panel.add(passText);

        JButton loginButton = new JButton("Zaloguj");
        loginButton.setBounds(100, 100, 100, 25);
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            try {
                socket = new Socket("localhost", 12345);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("LOGIN;" + peselText.getText() + ";" + new String(passText.getPassword()));
                String response = in.readLine();
                if (response.startsWith("ERROR")) {
                    JOptionPane.showMessageDialog(frame, "Błąd logowania: " + response);
                } else {
                    frame.dispose();
                    createMainWindow(response);
                }
            } catch (IOException ex) {
                showError(ex);
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void createSignupWindow() {
        frame = new JFrame("Rejestracja klienta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));
        frame.add(panel);

        JTextField imie = new JTextField();
        JTextField nazwisko = new JTextField();
        JTextField pesel = new JTextField();
        JPasswordField haslo = new JPasswordField();

        panel.add(new JLabel("Imię:"));
        panel.add(imie);
        panel.add(new JLabel("Nazwisko:"));
        panel.add(nazwisko);
        panel.add(new JLabel("PESEL:"));
        panel.add(pesel);
        panel.add(new JLabel("Hasło:"));
        panel.add(haslo);

        JButton register = new JButton("Zarejestruj");
        panel.add(new JLabel());
        panel.add(register);

        register.addActionListener(e -> {
            try {
                socket = new Socket("localhost", 12345);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("SIGNUP;" + imie.getText() + ";" + nazwisko.getText() + ";" + pesel.getText() + ";" + new String(haslo.getPassword()));
                String response = in.readLine();
                JOptionPane.showMessageDialog(frame, response);
                frame.dispose();
                createLoginWindow();
            } catch (IOException ex) {
                showError(ex);
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void createMainWindow(String userInfo) {
        frame = new JFrame("Terminal Klienta - Konto");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(6, 1));

        JLabel infoLabel = new JLabel("Zalogowano: " + userInfo);
        frame.add(infoLabel);

        JButton balanceButton = new JButton("Sprawdź saldo");
        JButton depositButton = new JButton("Wpłata");
        JButton withdrawButton = new JButton("Wypłata");
        JButton transferButton = new JButton("Przelew");
        JButton exitButton = new JButton("Wyloguj");

        frame.add(balanceButton);
        frame.add(depositButton);
        frame.add(withdrawButton);
        frame.add(transferButton);
        frame.add(exitButton);

        balanceButton.addActionListener(e -> {
            out.println("BALANCE");
            try {
                saldo = in.readLine();
                JOptionPane.showMessageDialog(frame, "Saldo: " + saldo);
            } catch (IOException ex) {
                showError(ex);
            }
        });

        depositButton.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(frame, "Kwota wpłaty:");
            if (amount != null && isPositiveNumber(amount)) {
                out.println("DEPOSIT;" + amount);
                try {
                    JOptionPane.showMessageDialog(frame, in.readLine());
                } catch (IOException ex) {
                    showError(ex);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Nieprawidłowa kwota.");
            }
        });

        withdrawButton.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(frame, "Kwota wypłaty:");
            if (amount != null && isPositiveNumber(amount)) {
                out.println("WITHDRAW;" + amount);
                try {
                    JOptionPane.showMessageDialog(frame, in.readLine());
                } catch (IOException ex) {
                    showError(ex);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Nieprawidłowa kwota.");
            }
        });

        transferButton.addActionListener(e -> {
            JTextField kontoField = new JTextField();
            JTextField kwotaField = new JTextField();
            Object[] msg = {"Numer konta odbiorcy:", kontoField, "Kwota:", kwotaField};

            int ok = JOptionPane.showConfirmDialog(frame, msg, "Przelew", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION && isPositiveNumber(kwotaField.getText())) {
                out.println("TRANSFER;" + kontoField.getText() + ";" + kwotaField.getText());
                try {
                    JOptionPane.showMessageDialog(frame, in.readLine());
                } catch (IOException ex) {
                    showError(ex);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Nieprawidłowa kwota.");
            }
        });

        exitButton.addActionListener(e -> {
            try {
                socket.close();
            } catch (IOException ignored) {}
            frame.dispose();
            createWelcomeWindow();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static boolean isPositiveNumber(String input) {
        try {
            return Double.parseDouble(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void showError(Exception ex) {
        JOptionPane.showMessageDialog(frame, "Błąd: " + ex.getMessage());
    }
}
