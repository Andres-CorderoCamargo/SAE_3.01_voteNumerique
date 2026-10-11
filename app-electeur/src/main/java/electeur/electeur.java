package electeur;

import java.io.IOException;
import java.net.Socket;

public class electeur {

    public static void main(String[] args) {
        String serveurHost = "127.0.0.1";
        int serveurPort = 8080;

        System.out.println("[CLIENT] Connexion en cours vers le serveur de vote...");

        // Établissement de la connexion TCP avec l'urne
        try (Socket socket = new Socket(serveurHost, serveurPort)) {
            System.out.println("[CLIENT] Connecté avec succès au serveur !");

            // Données de test pour l'inscription
            String electeurId = "ELEC-2026-001";
            String login = "jean.dupont";
            String mdpClair = "MonMotDePasseSecret123";

            // Appel de la méthode d'inscription
            boolean resultat = ElecteurService.inscrireElecteur(socket, electeurId, login, mdpClair);

            if (resultat) {
                System.out.println("[CLIENT] Test réussi : l'électeur est bien inscrit.");
            } else {
                System.out.println("[CLIENT] Test échoué : l'inscription a été refusée.");
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] Impossible de se connecter au serveur (est-il démarré ?) : " + e.getMessage());
        }
    }
}