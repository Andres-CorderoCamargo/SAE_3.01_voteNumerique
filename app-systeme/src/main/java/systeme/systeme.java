package systeme;

import com.g3.vote.proto.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class systeme {
    public static void main(String[] args) {
        int port = 8080;
        System.out.println("[SERVEUR] Démarrage de l'urne sur le port " + port + "...");



        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                // Attente d'un nouvel électeur
                Socket socket = serverSocket.accept();
                System.out.println("[SERVEUR] Nouvel électeur connecté : " + socket.getRemoteSocketAddress());

                // Lancement d'un thread dédié pour ce client
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur sur le serveur : " + e.getMessage());
        }
    }

    /**
     * Classe interne pour gérer chaque client dans un thread séparé
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Socket s = socket) {
                // 1. Lecture de l'enveloppe globale envoyée par le client
                ClientMessage clientMsg = ClientMessage.parseDelimitedFrom(s.getInputStream());

                if (clientMsg != null) {
                    // 2. Routage selon le type de requête reçue grâce au oneof
                    if (clientMsg.hasRegisterRequest()) {
                        traiterInscription(clientMsg.getRegisterRequest(), s);

                    } else if (clientMsg.hasVoteRequest()) {
                        traiterVote(clientMsg.getVoteRequest(), s);

                    } else if (clientMsg.hasPublicKeyRequest()) {
                        traiterPublicKey(clientMsg.getPublicKeyRequest(), s);

                    } else {
                        System.out.println("[SERVEUR] Type de message inconnu ou non géré.");
                    }
                }
            } catch (IOException e) {
                System.err.println("[SERVEUR] Erreur réseau : " + e.getMessage());
            }
        }
    }

    private static void traiterInscription(RegisterRequest request, Socket socket) throws IOException {
        System.out.println("[SERVEUR] Traitement inscription pour : " + request.getLogin());

        // vérifications
        boolean success = !request.getLogin().isEmpty();
        String message = success ? "Inscription réussie" : "Échec de l'inscription";

        // Construction et envoi de la réponse
        RegisterResponse response = RegisterResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();

        response.writeDelimitedTo(socket.getOutputStream());
    }

    private static void traiterVote(VoteRequest request, Socket socket) throws IOException {
        System.out.println("[SERVEUR] Réception d'un bulletin de vote pour l'électeur : " + request.getElecteurId());

        // TODO: Ajouter la logique de traitement et de réponse du vote
    }

    private static void traiterPublicKey(PublicKeyRequest request, Socket socket) throws IOException {
        System.out.println("[SERveur] Demande de clé publique reçue.");

        // TODO: Envoyer la PublicKeyResponse
    }
}