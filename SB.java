import java.io.*;
import java.net.*;
import java.util.*;

class Klient {
    String imie, nazwisko, pesel, numerKonta;
    double saldo;

    Klient(String imie, String nazwisko, String pesel, String numerKonta, double saldo) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.pesel = pesel;
        this.numerKonta = numerKonta;
        this.saldo = saldo;
    }

    public String toCSV() {
        return imie + "," + nazwisko + "," + pesel + "," + numerKonta + "," + saldo;
    }
}

public class SB {
    static final int PORT = 12345;
    static List<Klient> klienci = Collections.synchronizedList(new ArrayList<>());
    static final String PLIK = "klienci.csv";

    public static void main(String[] args) throws IOException {
        wczytajDane();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serwer SB działa na porcie " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> obsluzKlienta(socket)).start();
        }
    }

    static void obsluzKlienta(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String pesel = in.readLine();
            Klient klient = znajdzPoPeselu(pesel);

            if (pesel.equals("BANKIER")) {
                out.println("BANKIER_OK");
                while (true) {
                    String linia = in.readLine();
                    if (linia == null) break;
                    String[] czesci = linia.split(";");
                    switch (czesci[0]) {
                        case "ADD":
                            String imie = czesci[1];
                            String nazwisko = czesci[2];
                            String nowyPesel = czesci[3];
                            String nowyNumer = generujNumerKonta();
                            klienci.add(new Klient(imie, nazwisko, nowyPesel, nowyNumer, 0.0));
                            zapiszDane();
                            out.println("OK;" + nowyNumer);
                            break;
                        case "EDIT":
                            String editPesel = czesci[1];
                            Klient edytowany = znajdzPoPeselu(editPesel);
                            if (edytowany != null) {
                                edytowany.imie = czesci[2];
                                edytowany.nazwisko = czesci[3];
                                zapiszDane();
                                out.println("OK");
                            } else {
                                out.println("ERROR;Nie znaleziono klienta");
                            }
                            break;
                        default:
                            out.println("ERROR;Nieznana komenda");
                    }
                }
                return;
            }

            if (klient == null) {
                out.println("ERROR;Nie znaleziono klienta");
                return;
            }
            out.println("OK;" + klient.numerKonta + ";" + klient.imie + ";" + klient.nazwisko);

            String linia;
            while ((linia = in.readLine()) != null) {
                String[] czesci = linia.split(";");
                switch (czesci[0]) {
                    case "BALANCE":
                        out.println("OK;" + klient.saldo);
                        break;
                    case "DEPOSIT":
                        double wp = Double.parseDouble(czesci[1]);
                        klient.saldo += wp;
                        zapiszDane();
                        out.println("OK;" + klient.saldo);
                        break;
                    case "WITHDRAW":
                        double wy = Double.parseDouble(czesci[1]);
                        if (klient.saldo >= wy) {
                            klient.saldo -= wy;
                            zapiszDane();
                            out.println("OK;" + klient.saldo);
                        } else {
                            out.println("ERROR;Brak środków");
                        }
                        break;
                    case "TRANSFER":
                        String kontoDocelowe = czesci[1];
                        double kwota = Double.parseDouble(czesci[2]);
                        Klient odbiorca = znajdzPoNrKonta(kontoDocelowe);
                        if (odbiorca != null && klient.saldo >= kwota) {
                            klient.saldo -= kwota;
                            odbiorca.saldo += kwota;
                            zapiszDane();
                            out.println("OK;" + klient.saldo);
                        } else {
                            out.println("ERROR;Transfer nieudany");
                        }
                        break;
                    default:
                        out.println("ERROR;Nieznana komenda");
                }
            }

        } catch (IOException e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    static Klient znajdzPoPeselu(String pesel) {
        synchronized (klienci) {
            for (Klient k : klienci) {
                if (k.pesel.equals(pesel)) return k;
            }
        }
        return null;
    }

    static Klient znajdzPoNrKonta(String nr) {
        synchronized (klienci) {
            for (Klient k : klienci) {
                if (k.numerKonta.equals(nr)) return k;
            }
        }
        return null;
    }

    static void wczytajDane() throws IOException {
        klienci.clear();
        File file = new File(PLIK);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                klienci.add(new Klient(p[0], p[1], p[2], p[3], Double.parseDouble(p[4])));
            }
        }
    }

    static void zapiszDane() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PLIK))) {
            for (Klient k : klienci) pw.println(k.toCSV());
        }
    }

    static String generujNumerKonta() {
        Random rand = new Random();
        String nr;
        do {
            nr = "1" + (rand.nextInt(90000) + 10000);
        } while (znajdzPoNrKonta(nr) != null);
        return nr;
    }
}