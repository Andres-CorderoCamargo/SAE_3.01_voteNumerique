package systeme;

public class InfosStockageElecteur {
    private final String login;
    private final String mdpChiffre;
    private final String electeurId;

    public InfosStockageElecteur(String electeurId, String login, String mdpChiffre) {
        this.electeurId = electeurId;
        this.login = login;
        this.mdpChiffre = mdpChiffre;
    }

    public String getLogin() { return login; }
    public String getMdpChiffre() { return mdpChiffre; }
    public String getElecteurId() { return electeurId; }

    @Override
    public String toString() {
        return "{ID=" + electeurId + ", Login=" + login + ", MdpChiffre=" + mdpChiffre + "}";
    }
}
