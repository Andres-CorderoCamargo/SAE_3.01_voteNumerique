package systeme;

import com.g3.vote.proto.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class systeme {

    private static final ConcurrentHashMap<String, InfosStockageElecteur> baseElecteursTemporaire = new ConcurrentHashMap<>();
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
                ClientMessage clientMsg;

                // Boucle pour écouter tant que le client envoie des messages sur la même socket
                while ((clientMsg = ClientMessage.parseDelimitedFrom(s.getInputStream())) != null) {

                    // Routage selon le type de requête reçue grâce au oneof
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

                System.out.println("[SERVEUR] L'électeur s'est déconnecté proprement : " + s.getRemoteSocketAddress());

            } catch (IOException e) {
                System.err.println("[SERVEUR] Erreur réseau ou déconnexion brusque : " + e.getMessage());
            }
        }
    }

    private static void traiterInscription(RegisterRequest request, Socket socket) throws IOException {
        System.out.println("[SERVEUR] Traitement inscription pour : " + request.getLogin());

        String electeurId = request.getElecteurId();
        String login = request.getLogin();

        String mdpChiffreStr = request.getPasswordChiffre().toStringUtf8();

        boolean success;
        String message;

        // Vérifications de base
        if (electeurId == null || electeurId.isEmpty() || login == null || login.isEmpty()) {
            success = false;
            message = "Échec : L'identifiant et le login ne peuvent pas être vides.";
        }
        // Vérification si l'électeur existe déjà
        else if (baseElecteursTemporaire.containsKey(electeurId)) {
            success = false;
            message = "Échec : L'électeur avec l'ID " + electeurId + " est déjà inscrit.";
        }
        else {
            // 3. Enregistrement temporaire en mémoire
            InfosStockageElecteur infos = new InfosStockageElecteur(electeurId, login, mdpChiffreStr);
            baseElecteursTemporaire.put(electeurId, infos);

            success = true;
            message = "Inscription réussie. Contenu de la base : " + baseElecteursTemporaire.toString();

            System.out.println("[SERVEUR - MÉMOIRE] Électeur ajouté. Total inscrits : " + baseElecteursTemporaire.size());
        }

        System.out.println("[SERVEUR] Message renvoyé : " + message);

        // Construction et envoi de la réponse
        RegisterResponse response = RegisterResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();


        response.writeDelimitedTo(socket.getOutputStream());
        socket.getOutputStream().flush();
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