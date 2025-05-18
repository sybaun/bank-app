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
        SwingUtilities.invokeLater(() -> createLoginWindow());
    }

    private static void createLoginWindow() {
        frame = new JFrame("Terminal Klienta - Logowanie");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeLoginComponents(panel);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void placeLoginComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel peselLabel = new JLabel("PESEL:");
        peselLabel.setBounds(10, 20, 80, 25);
        panel.add(peselLabel);

        JTextField peselText = new JTextField(20);
        peselText.setBounds(100, 20, 165, 25);
        panel.add(peselText);

        JButton loginButton = new JButton("Zaloguj");
        loginButton.setBounds(100, 60, 100, 25);
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            try {
                socket = new Socket("localhost", 12345);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println(peselText.getText());
                String response = in.readLine();
                if (response.startsWith("ERROR")) {
                    JOptionPane.showMessageDialog(frame, "Błąd logowania: " + response);
                } else {
                    frame.dispose();
                    createMainWindow(response);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Błąd połączenia z serwerem");
            }
        });
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
        JButton exitButton = new JButton("Wyjście");

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
            if (amount != null) {
                out.println("DEPOSIT;" + amount);
                try {
                    JOptionPane.showMessageDialog(frame, in.readLine());
                } catch (IOException ex) {
                    showError(ex);
                }
            }
        });

        withdrawButton.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(frame, "Kwota wypłaty:");
            if (amount != null) {
                out.println("WITHDRAW;" + amount);
                try {
                    JOptionPane.showMessageDialog(frame, in.readLine());
                } catch (IOException ex) {
                    showError(ex);
                }
            }
        });

        transferButton.addActionListener(e -> {
            JTextField kontoField = new JTextField();
            JTextField kwotaField = new JTextField();
            Object[] msg = {"Numer konta odbiorcy:", kontoField, "Kwota:", kwotaField};

            int ok = JOptionPane.showConfirmDialog(frame, msg, "Przelew", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                out.println("TRANSFER;" + kontoField.getText() + ";" + kwotaField.getText());
                try {
                    JOptionPane.showMessageDialog(frame, in.readLine());
                } catch (IOException ex) {
                    showError(ex);
                }
            }
        });

        exitButton.addActionListener(e -> {
            try {
                socket.close();
            } catch (IOException ignored) {}
            frame.dispose();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void showError(Exception ex) {
        JOptionPane.showMessageDialog(frame, "Błąd: " + ex.getMessage());
    }
}
