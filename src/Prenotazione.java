public class Prenotazione {
    private String idEsame;
    private String nomeStudente;
    private String dataPrenotazione;

    public Prenotazione(String idEsame, String nomeStudente, String dataPrenotazione) {
        this.idEsame = idEsame;
        this.nomeStudente = nomeStudente;
        this.dataPrenotazione = dataPrenotazione;
    }

    public String getIdEsame() {
        return idEsame;
    }

    public String getNomeStudente() {
        return nomeStudente;
    }

    public String getDataPrenotazione() {
        return dataPrenotazione;
    }
}
