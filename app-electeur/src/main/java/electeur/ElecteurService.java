package electeur;
import com.g3.vote.proto.ClientMessage;
import com.g3.vote.proto.RegisterRequest;
import com.g3.vote.proto.RegisterResponse;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.Socket;

public class ElecteurService {

    /**
     * Encapsule la logique d'inscription d'un électeur.
     *
     * @_socket         La socket connectée au serveur (l'urne)
     * @param electeurId L'identifiant unique de l'électeur
     * @param login      Le login choisi
     * @param mdpClair   Le mot de passe en clair (qui sera chiffré)
     * @return true si l'inscription a réussi, false sinon
     */
    public static boolean inscrireElecteur(Socket socket, String electeurId, String login, String mdpClair) {
        try {
            // Chiffrement du mot de passe
            byte[] mdpChiffre = chiffrerMotDePasse(mdpClair);


            RegisterRequest registerRequest = RegisterRequest.newBuilder()
                    .setElecteurId(electeurId)
                    .setLogin(login)
                    .setPasswordChiffre(ByteString.copyFrom(mdpChiffre))
                    .build();

            ClientMessage clientMsg = ClientMessage.newBuilder()
                    .setRegisterRequest(registerRequest)
                    .build();

            clientMsg.writeDelimitedTo(socket.getOutputStream());
            socket.getOutputStream().flush();

            RegisterResponse response = RegisterResponse.parseDelimitedFrom(socket.getInputStream());

            if (response != null && response.getSuccess()) {
                System.out.println("[SUCCÈS] " + response.getMessage());
                return true;
            } else {
                String errorMsg = (response != null) ? response.getMessage() : "Réponse vide du serveur";
                System.out.println("[ÉCHEC] " + errorMsg);
                return false;
            }

        } catch (IOException e) {
            System.err.println("Erreur réseau/flux : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private static byte[] chiffrerMotDePasse(String mdp) {
        // todo implementer chiffrement
        return mdp.getBytes();
    }
}