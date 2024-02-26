import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;

public class ServerUniversitario {
    // Porta su cui il server è in ascolto per la connessione dalla Segreteria
    private static final int PORT = 12345;

    // URL del database MySQL
    private static final String DB_URL = "jdbc:mysql://localhost:3306/db_universita";
    private static final String DB_USER = "root"; // Utente del database
    private static final String DB_PASSWORD = ""; // Password del database

    public static void main(String[] args) {
        try {
            // Inizia a ascoltare sulla porta specificata
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println(
                        "\n\n(*) 'ServerUniversitario' in ascolto sulla porta : " + PORT
                                + ". In attesa della 'Segreteria' ...");

                while (true) {
                    // Accetta la connessione dalla Segreteria
                    Socket segreteriaSocket = serverSocket.accept();
                    System.out.println(
                            "\n(*) Connessione accettata da 'Segreteria' su indirizzo : "
                                    + segreteriaSocket.getInetAddress().getHostAddress());

                    // Inizia la gestione delle richieste della Segreteria
                    try (
                            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                            BufferedReader segreteriaReader = new BufferedReader(
                                    new InputStreamReader(segreteriaSocket.getInputStream()));
                            PrintWriter segreteriaWriter = new PrintWriter(segreteriaSocket.getOutputStream(), true)) {

                        String messaggio;

                        // Ciclo principale per gestire le richieste dalla Segreteria
                        while ((messaggio = segreteriaReader.readLine()) != null) {
                            System.out.println("(*) 'Segreteria' ha inviato il seguente messaggio : " + messaggio);

                            // Divide il messaggio in tipoRichiesta e argomenti
                            String[] comandoDiviso = messaggio.split(":", 2);

                            if (comandoDiviso.length == 2) {
                                String tipoRichiesta = comandoDiviso[0];
                                String argomenti = comandoDiviso[1];

                                // Switch per gestire diversi tipi di richieste
                                switch (tipoRichiesta) {

                                    // Gestisci la richiesta di date per un esame
                                    case "RICHIEDI_DATE_ESAME":
                                        String[] infoDateEsame = recuperaEsamePerNomeEsame(conn, argomenti);
                                        if (infoDateEsame != null) {
                                            String idEsame = infoDateEsame[0];
                                            String nomeEsame = infoDateEsame[1];
                                            String dataEsame = infoDateEsame[2];
                                            segreteriaWriter.println("ID_ESAME:" + idEsame + ",NOME_ESAME:" + nomeEsame
                                                    + ",DATE_ESAME:" + dataEsame);
                                        } else {
                                            segreteriaWriter.println("Esame non trovato");
                                        }
                                        break;

                                    // Gestisci la richiesta di prenotazione di un esame
                                    case "PRENOTA_ESAME":
                                        String[] argomentiPrenotazione = argomenti.split(",");
                                        if (argomentiPrenotazione.length == 2) {
                                            String idEsamePrenotazione = argomentiPrenotazione[0].trim();
                                            String matricolaStudente = argomentiPrenotazione[1].trim();
                                            String rispostaPrenotazione = prenotaEsame(conn, idEsamePrenotazione,
                                                    matricolaStudente);
                                            segreteriaWriter.println(rispostaPrenotazione);
                                        } else {
                                            segreteriaWriter.println("Comando non valido");
                                        }
                                        break;

                                    // Gestisci la richiesta di aggiunta di un nuovo esame
                                    case "NUOVO_ESAME":
                                        String[] argomentiNuovoEsame = argomenti.split(",");
                                        if (argomentiNuovoEsame.length == 2) {
                                            String nomeNuovoEsame = argomentiNuovoEsame[0].trim();
                                            String dataNuovoEsame = argomentiNuovoEsame[1].trim();

                                            // Esegui l'operazione di aggiunta del nuovo esame
                                            String rispostaAggiuntaEsame = aggiungiNuovoEsame(conn, nomeNuovoEsame,
                                                    dataNuovoEsame);
                                            segreteriaWriter.println(rispostaAggiuntaEsame);
                                        } else {
                                            segreteriaWriter.println("Comando NUOVO_ESAME non valido");
                                        }
                                        break;

                                    default:
                                        segreteriaWriter.println("Comando sconosciuto");
                                }
                            } else {
                                segreteriaWriter.println("Messaggio non valido");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funzione per prenotare un esame
    private static String prenotaEsame(Connection conn, String idEsame, String matricolaStudente) throws SQLException {
        // Verifica se l'esame esiste
        String[] infoEsame = recuperaEsamePerIdEsame(conn, idEsame);

        if (infoEsame != null) {
            String nomeEsame = infoEsame[1];
            LocalDate dataPrenotazione = LocalDate.now();

            // Esegue la prenotazione
            if (effettuaPrenotazione(conn, idEsame, nomeEsame, matricolaStudente, dataPrenotazione)) {
                return "Prenotazione avvenuta con successo per l'esame " + nomeEsame + " (ID: " + idEsame +
                        ", Data: " + dataPrenotazione + ")";
            } else {
                return "Errore durante la prenotazione per l'esame " + nomeEsame;
            }
        } else {
            return "Esame non trovato per ID " + idEsame;
        }
    }

    // Funzione per effettuare la prenotazione dell'esame
    private static boolean effettuaPrenotazione(Connection conn, String idEsame, String nomeEsame,
            String matricolaStudente, LocalDate dataPrenotazione) throws SQLException {
        String query = "INSERT INTO prenotazioni (id_esame, matricola_studente, data_prenotazione) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, idEsame);
            preparedStatement.setString(2, matricolaStudente);

            // Imposta il valore del parametro
            preparedStatement.setDate(3, java.sql.Date.valueOf(dataPrenotazione));

            // Esegui l'inserimento
            int result = preparedStatement.executeUpdate();

            return result > 0;
        }
    }

    // Funzione per recuperare le informazioni sull'esame passando come parametro
    // nome_esame
    private static String[] recuperaEsamePerNomeEsame(Connection conn, String nomeEsame) throws SQLException {
        String query = "SELECT id_esame, data_esame FROM esami WHERE nome_esame = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, nomeEsame);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String idEsame = resultSet.getString("id_esame");
                    String dataEsame = resultSet.getString("data_esame");
                    return new String[] { idEsame, nomeEsame, dataEsame };
                }
            }
        }
        return null;
    }

    // Funzione per recuperare le informazioni sull'esame passando come parametro
    // id_esame
    private static String[] recuperaEsamePerIdEsame(Connection conn, String idEsame) throws SQLException {
        String query = "SELECT nome_esame, data_esame FROM esami WHERE id_esame = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, idEsame);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String nomeEsame = resultSet.getString("nome_esame");
                    String dataEsame = resultSet.getString("data_esame");
                    return new String[] { idEsame, nomeEsame, dataEsame };
                }
            }
        }
        return null;
    }

    // Funzione per aggiungere un nuovo esame
    private static String aggiungiNuovoEsame(Connection conn, String nomeEsame, String dataEsame) throws SQLException {
        String query = "INSERT INTO esami (nome_esame, data_esame) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, nomeEsame);
            preparedStatement.setString(2, dataEsame);

            // Esegui l'inserimento
            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                return "Nuovo esame aggiunto con successo";
            } else {
                return "Errore durante l'aggiunta del nuovo esame";
            }
        }
    }
}
