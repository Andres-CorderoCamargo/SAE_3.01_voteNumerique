package electeur;

import java.util.Scanner;

public class SaisieElecteur {

    private static final Scanner scanner = new Scanner(System.in);

 public static class InfosInscription {
     private final String electeurId;
     private final String login;
     private final String mdpClair;

     public InfosInscription(String electeurId, String login, String mdpClair) {
        this.electeurId = electeurId;
        this.login = login;
        this.mdpClair = mdpClair;
    }

    public String getElecteurId() { return electeurId; }
    public String getLogin() { return login; }
    public String getMdpClair() { return mdpClair; }
}

    public static int afficherMenuEtChoisir() {

        //uniquement l'orga doit pouvoir ajouter un electeur
        //donc on devra modifier l'affichage si la personne est connectee en admin
        System.out.println("\n=== MENU PRINCIPAL ÉLECTEUR ===");
        System.out.println("1. Inscrire un compte électeur");
        System.out.println("2. Se connecter");
        System.out.println("3. Voter (à venir)");
        System.out.println("0. Quitter");
        System.out.print("Faites votre choix : ");

        try {
            int choix = Integer.parseInt(scanner.nextLine().trim());
            return choix;
        } catch (NumberFormatException e) {
            return -1; // Choix invalide
        }
    }

    public static InfosInscription demanderInformationsInscription() {
        System.out.println("\n--- INSCRIPTION D'UN ÉLECTEUR ---");

        System.out.print("Entrez l'identifiant électeur : ");
        String electeurId = scanner.nextLine();

        System.out.print("Entrez le login : ");
        String login = scanner.nextLine();

        System.out.print("Entrez le mot de passe : ");
        String mdpClair = scanner.nextLine();

        return new InfosInscription(electeurId, login, mdpClair);
    }




}
