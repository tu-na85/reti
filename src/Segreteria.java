import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;

public class Segreteria {
    public static void main(String[] args) {
        // Indirizzo e porta del Server Universitario
        final String SERVER_HOST_UNIVERSITARIO = "localhost";
        final int SERVER_PORT_UNIVERSITARIO = 12345;
        // Porta per la connessione con lo Studente
        final int PORT_STUDENTE = 12346;

        try {
            // Connessione al Server Universitario
            Socket universitarioSocket = new Socket(SERVER_HOST_UNIVERSITARIO, SERVER_PORT_UNIVERSITARIO);
            System.out.println(
                    "\n\n(*) Connessione a 'ServerUniversitario' riuscita sulla porta : " + SERVER_PORT_UNIVERSITARIO);

            // Creazione del socket per la connessione con lo Studente
            ServerSocket studenteServerSocket = new ServerSocket(PORT_STUDENTE);
            System.out.println(
                    "\n(*) 'Segreteria' in ascolto sulla porta : " + PORT_STUDENTE
                            + ". In attesa di 'Studente' ...\n\n");

            // Thread per l'inserimento di nuovi esami
            Thread inserimentoEsamiThread = new Thread(() -> inserisciNuoviEsami(universitarioSocket));
            inserimentoEsamiThread.start();

            // Thread per inoltrare le richieste degli studenti al ServerUniversitario
            Thread inoltroRichiesteThread = new Thread(
                    () -> inoltraRichieste(universitarioSocket, studenteServerSocket));
            inoltroRichiesteThread.start();

            // Attendiamo la terminazione del thread di inserimento esami
            inserimentoEsamiThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Metodo per l'inserimento di nuovi esami da terminale
    private static void inserisciNuoviEsami(Socket universitarioSocket) {
        try (BufferedReader serverUniversitarioReader = new BufferedReader(
                new InputStreamReader(universitarioSocket.getInputStream()));
                PrintWriter universitarioWriter = new PrintWriter(universitarioSocket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)) {

            while (true) {
                // Chiedi all'utente di premere "Invio" per visualizzare il menu
                System.out.println("Premi Invio per visualizzare il menu...");
                System.console().readLine();

                // Menu per l'inserimento del nuovo esame o uscire
                System.out.println("1. Inserisci nuovo esame");
                System.out.println("2. Esci");
                System.out.print("-> Inserisci la tua scelta: ");
                String scelta = scanner.nextLine();

                switch (scelta) {
                    case "1":
                        // Inserimento del nuovo esame
                        System.out.print("Inserisci il nome del nuovo esame: ");
                        String nomeNuovoEsame = scanner.nextLine();
                        System.out.print("Inserisci la data del nuovo esame (yyyy-MM-dd): ");
                        String dataNuovoEsame = scanner.nextLine();

                        // Invia la richiesta di aggiunta del nuovo esame al ServerUniversitario
                        universitarioWriter.println("NUOVO_ESAME:" + nomeNuovoEsame + "," + dataNuovoEsame);

                        // Leggi la risposta dal ServerUniversitario
                        String rispostaServer = serverUniversitarioReader.readLine();
                        System.out.println("Risposta dal ServerUniversitario: " + rispostaServer);
                        break;

                    case "2":
                        System.out.println("Uscita...");
                        System.exit(0);
                        return;

                    default:
                        System.out.println("Scelta non valida. Riprova.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per inoltrare le richieste degli studenti al ServerUniversitario
    private static void inoltraRichieste(Socket universitarioSocket, ServerSocket studenteServerSocket) {
        try {
            // Accettazione della connessione dallo Studente
            Socket studenteSocket = studenteServerSocket.accept();
            System.out.println(
                    "\n(*) Connessione accettata da 'Studente' su indirizzo : "
                            + studenteSocket.getInetAddress().getHostAddress());

            try (
                    BufferedReader studenteReader = new BufferedReader(
                            new InputStreamReader(studenteSocket.getInputStream()));
                    PrintWriter studenteWriter = new PrintWriter(studenteSocket.getOutputStream(), true);
                    BufferedReader serverUniversitarioReader = new BufferedReader(
                            new InputStreamReader(universitarioSocket.getInputStream()));
                    PrintWriter universitarioWriter = new PrintWriter(universitarioSocket.getOutputStream(), true)) {
                while (true) {
                    // Leggi la richiesta dello studente
                    String richiestaStudente = studenteReader.readLine();
                    System.out.println("Richiesta dallo Studente: " + richiestaStudente);

                    // Inoltra la richiesta al ServerUniversitario
                    universitarioWriter.println(richiestaStudente);

                    // Leggi la risposta dal ServerUniversitario e inoltrala allo Studente
                    String rispostaServer = serverUniversitarioReader.readLine();
                    System.out.println("Risposta dal ServerUniversitario: " + rispostaServer);
                    studenteWriter.println(rispostaServer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
