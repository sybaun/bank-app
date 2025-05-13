import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TB {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner sc = new Scanner(System.in);

        out.println("BANKIER");
        String odp = in.readLine();
        if (!odp.startsWith("BANKIER_OK")) {
            System.out.println("Błąd połączenia z serwerem");
            return;
        }

        while (true) {
            System.out.println("1. Dodaj klienta\n2. Edytuj dane klienta\n3. Wyjście");
            String wybor = sc.nextLine();
            switch (wybor) {
                case "1":
                    System.out.print("Imię: ");
                    String imie = sc.nextLine();
                    System.out.print("Nazwisko: ");
                    String nazwisko = sc.nextLine();
                    System.out.print("PESEL: ");
                    String pesel = sc.nextLine();
                    out.println("ADD;" + imie + ";" + nazwisko + ";" + pesel);
                    System.out.println(in.readLine());
                    break;
                case "2":
                    System.out.print("PESEL klienta: ");
                    String p = sc.nextLine();
                    System.out.print("Nowe imię: ");
                    String ni = sc.nextLine();
                    System.out.print("Nowe nazwisko: ");
                    String nn = sc.nextLine();
                    out.println("EDIT;" + p + ";" + ni + ";" + nn);
                    System.out.println(in.readLine());
                    break;
                case "3":
                    socket.close();
                    return;
                default:
                    System.out.println("Nieprawidłowy wybór");
            }
        }
    }
}
