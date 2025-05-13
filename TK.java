import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TK {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner sc = new Scanner(System.in);

        System.out.print("Podaj PESEL: ");
        String pesel = sc.nextLine();
        out.println(pesel);

        String odp = in.readLine();
        if (odp.startsWith("ERROR")) {
            System.out.println("Błąd logowania: " + odp);
            return;
        }
        System.out.println("Zalogowano: " + odp);

        while (true) {
            System.out.println("1. Sprawdź saldo\n2. Wpłata\n3. Wypłata\n4. Przelew\n5. Wyjście");
            String wybor = sc.nextLine();
            switch (wybor) {
                case "1":
                    out.println("BALANCE");
                    System.out.println("Saldo: " + in.readLine());
                    break;
                case "2":
                    System.out.print("Kwota wpłaty: ");
                    out.println("DEPOSIT;" + sc.nextLine());
                    System.out.println(in.readLine());
                    break;
                case "3":
                    System.out.print("Kwota wypłaty: ");
                    out.println("WITHDRAW;" + sc.nextLine());
                    System.out.println(in.readLine());
                    break;
                case "4":
                    System.out.print("Numer konta odbiorcy: ");
                    String konto = sc.nextLine();
                    System.out.print("Kwota przelewu: ");
                    String kwota = sc.nextLine();
                    out.println("TRANSFER;" + konto + ";" + kwota);
                    System.out.println(in.readLine());
                    break;
                case "5":
                    socket.close();
                    return;
                default:
                    System.out.println("Nieprawidłowy wybór");
            }
        }
    }
}