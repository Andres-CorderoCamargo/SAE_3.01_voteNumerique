import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class systeme {
    public static void main(String[] args) {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Le Système attend la connexion d'un électeur sur le port " + port + "...");

            // Bloque l'exécution jusqu'à ce qu'un client se connecte
            Socket socket = serverSocket.accept();
            System.out.println("Électeur connecté !");

            // Lecture des données envoyées par l'électeur
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String messageRecu = reader.readLine();
            System.out.println("Message reçu du client : " + messageRecu);

            // Fermeture de la connexion
            socket.close();

        } catch (Exception ex) {
            System.err.println("Erreur côté Système : " + ex.getMessage());
        }
    }
}