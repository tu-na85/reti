import java.io.*;
import java.net.Socket;

public class Studente {
    public static void main(String[] args) {
        // Indirizzo e porta del server della Segreteria
        final String SERVER_HOST_SEGRETERIA = "localhost";
        final int SERVER_PORT_SEGRETERIA = 12346;

        try (
                // Creazione della connessione con il server della Segreteria
                Socket serverSegreteriaSocket = new Socket(SERVER_HOST_SEGRETERIA, SERVER_PORT_SEGRETERIA);

                // Stream per leggere dal server della Segreteria
                BufferedReader segreteriaReader = new BufferedReader(
                        new InputStreamReader(serverSegreteriaSocket.getInputStream()));

                // Stream per scrivere al server della Segreteria
                PrintWriter segreteriaWriter = new PrintWriter(serverSegreteriaSocket.getOutputStream(), true);

                // Stream per leggere l'input dell'utente da terminale
                BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("\n\n(*) Connessione a 'Segreteria' riuscita sulla porta : " + SERVER_PORT_SEGRETERIA);

            boolean continua = true;

            while (continua) {
                // Menu dell'utente
                System.out.println();
                System.out.println();
                System.out.println("Menu:");
                System.out.println("1. Richiedi date disponibili");
                System.out.println("2. Prenota un esame");
                System.out.println("3. Esci");
                System.out.print("-> Inserisci la tua scelta: ");
                String scelta = userInputReader.readLine();
                System.out.println();
                switch (scelta) {
                    case "1":
                        // Richiedi date disponibili per un esame scelto
                        System.out.print("--> Inserisci il nome dell'esame: ");
                        String nomeEsameRichiestaDate = userInputReader.readLine();

                        // Invia la richiesta al server della Segreteria
                        segreteriaWriter.println("RICHIEDI_DATE_ESAME:" + nomeEsameRichiestaDate);

                        // Leggi la risposta dal server della Segreteria e stampa le date disponibili
                        String rispostaDate = segreteriaReader.readLine();
                        System.out.println();
                        // System.out.println("\nDate disponibili per l'esame di " +
                        // nomeEsameRichiestaDate + ":");

                        // Splitta la stringa usando la virgola come delimitatore
                        String[] informazioni = rispostaDate.split(",");

                        // Stampa ogni informazione su una nuova riga
                        for (String informazione : informazioni) {
                            System.out.println(informazione);
                        }
                        break;
                    case "2":
                        // Prenota un esame
                        System.out.print("--> Inserisci l'ID dell'esame: ");
                        String idEsamePrenotazione = userInputReader.readLine();

                        System.out.print("--> Inserisci la tua matricola: ");
                        String matricolaStudente = userInputReader.readLine();

                        // Invia la richiesta al server della Segreteria
                        segreteriaWriter.println("PRENOTA_ESAME:" + idEsamePrenotazione + "," + matricolaStudente);

                        // Leggi la risposta dal server della Segreteria e stampa il risultato della
                        // prenotazione
                        String rispostaPrenotazione = segreteriaReader.readLine();
                        System.out.println(rispostaPrenotazione);
                        break;
                    case "3":
                        // Esci dal menu
                        System.out.println("Uscita...");
                        continua = false;
                        // serverSegreteriaSocket.close();
                        // segreteriaWriter.close();
                        // segreteriaReader.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Scelta non valida. Riprova.");
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la gestione della connessione: " + e.getMessage());
        }
    }
}
