// SB.java – Serwer Bankowy z obsługą logowania, rejestracji i hasła
import java.io.*;
import java.net.*;
import java.util.*;

public class SB {
    private static Map<String, Klient> klienci = new HashMap<>();
    private static final String PLIK_DANYCH = "klienci.csv";

    public static void main(String[] args) throws IOException {
        wczytajDane();
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serwer bankowy uruchomiony...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> obsluzKlienta(socket)).start();
        }
    }

    private static void obsluzKlienta(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            Klient aktualny = null;
            while (true) {
                String line = in.readLine();
                if (line == null) break;

                String[] parts = line.split(";");
                String komenda = parts[0];

                if (komenda.equals("LOGIN")) {
                    String pesel = parts[1];
                    String haslo = parts[2];

                    if (!klienci.containsKey(pesel)) {
                        out.println("ERROR: Klient nie istnieje.");
                    } else if (!klienci.get(pesel).haslo.equals(haslo)) {
                        out.println("ERROR: Błędne hasło.");
                    } else {
                        aktualny = klienci.get(pesel);
                        out.println(aktualny.imie + " " + aktualny.nazwisko + " | " + aktualny.numerKonta);
                    }

                } else if (komenda.equals("SIGNUP")) {
                    String imie = parts[1];
                    String nazwisko = parts[2];
                    String pesel = parts[3];
                    String haslo = parts[4];

                    if (klienci.containsKey(pesel)) {
                        out.println("ERROR: Klient już istnieje.");
                    } else if (isValidPesel(pesel) == false) {
                        out.println("ERROR: Niepoprawny numer pesel");
                    } else {
                        String numerKonta = "K" + (100000 + klienci.size());
                        Klient nowy = new Klient(imie, nazwisko, pesel, numerKonta, 0.0, haslo);
                        klienci.put(pesel, nowy);
                        zapiszDane();
                        out.println("Zarejestrowano. Twój numer konta to: " + numerKonta);
                    }

                } else if (aktualny == null) {
                    out.println("ERROR: Niezalogowany użytkownik.");

                } else if (komenda.equals("BALANCE")) {
                    out.println(aktualny.saldo);

                } else if (komenda.equals("DEPOSIT")) {
                    double kwota = Double.parseDouble(parts[1]);
                    if (kwota <= 0) {
                        out.println("ERROR: Kwota musi być dodatnia.");
                    } else {
                        aktualny.saldo += kwota;
                        zapiszDane();
                        out.println("Wpłacono " + kwota + " zł");
                    }

                } else if (komenda.equals("WITHDRAW")) {
                    double kwota = Double.parseDouble(parts[1]);
                    if (kwota <= 0) {
                        out.println("ERROR: Kwota musi być dodatnia.");
                    } else if (aktualny.saldo >= kwota) {
                        aktualny.saldo -= kwota;
                        zapiszDane();
                        out.println("Wypłacono " + kwota + " zł");
                    } else {
                        out.println("ERROR: Brak środków.");
                    }

                } else if (komenda.equals("TRANSFER")) {
                    String kontoDocelowe = parts[1];
                    double kwota = Double.parseDouble(parts[2]);

                    if (kwota <= 0) {
                        out.println("ERROR: Kwota musi być dodatnia.");
                        continue;
                    }

                    Klient odbiorca = null;
                    for (Klient k : klienci.values()) {
                        if (k.numerKonta.equals(kontoDocelowe)) {
                            odbiorca = k;
                            break;
                        }
                    }

                    if (odbiorca == null) {
                        out.println("ERROR: Konto docelowe nie istnieje.");
                    } else if (aktualny.saldo >= kwota) {
                        aktualny.saldo -= kwota;
                        odbiorca.saldo += kwota;
                        zapiszDane();
                        out.println("Przelano " + kwota + " zł do " + odbiorca.imie + " " + odbiorca.nazwisko);
                    } else {
                        out.println("ERROR: Brak środków.");
                    }
                } else {
                    out.println("ERROR: Nieznana komenda.");
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Błąd klienta: " + e.getMessage());
        }
    }

    private static void wczytajDane() {
        try (BufferedReader br = new BufferedReader(new FileReader(PLIK_DANYCH))) {
            String linia;
            while ((linia = br.readLine()) != null) {
                String[] parts = linia.split(";");
                if (parts.length < 6) continue;
                Klient k = new Klient(parts[0], parts[1], parts[2], parts[3], Double.parseDouble(parts[4]), parts[5]);
                klienci.put(k.pesel, k);
            }
        } catch (IOException e) {
            System.out.println("Nie udało się wczytać danych: " + e.getMessage());
        }
    }

    private static void zapiszDane() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PLIK_DANYCH))) {
            for (Klient k : klienci.values()) {
                pw.println(k.imie + ";" + k.nazwisko + ";" + k.pesel + ";" + k.numerKonta + ";" + k.saldo + ";" + k.haslo);
            }
        } catch (IOException e) {
            System.out.println("Nie udało się zapisać danych: " + e.getMessage());
        }
    }
    public static boolean isValidPesel(String pesel) {
        if (pesel == null || !pesel.matches("\\d{11}")) {
            return false;
        }
        int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
        int sum = 0;

        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(pesel.charAt(i));
            sum += digit * weights[i];
        }

        int controlDigit = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(pesel.charAt(10));

        return controlDigit == lastDigit;
    }
}

class Klient {
    String imie, nazwisko, pesel, numerKonta, haslo;
    double saldo;

    Klient(String imie, String nazwisko, String pesel, String numerKonta, double saldo, String haslo) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.pesel = pesel;
        this.numerKonta = numerKonta;
        this.saldo = saldo;
        this.haslo = haslo;
    }
}
