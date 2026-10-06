import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class electeur {
    public static void main(String[] args) {
        String adresseSysteme = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(adresseSysteme, port)) {
            // Préparation de l'envoi des données
            OutputStream output = socket.getOutputStream();
            // L'argument 'true' permet l'auto-flush (envoi immédiat)
            PrintWriter writer = new PrintWriter(output, true);

            // La chaîne de caractères à envoyer
            String vote = "VOTE: CANDIDAT_A";

            writer.println(vote);
            System.out.println("Message envoyé au système : " + vote);

        } catch (Exception ex) {
            System.err.println("Erreur côté Électeur : " + ex.getMessage());
        }
    }
}