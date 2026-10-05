# Structure du projet et contrôle des DSS

## Arborescence retenue

La proposition de départ sépare correctement la cryptographie et le réseau, et elle prévoit bien les trois programmes Java demandés. La partie web/PHP y est indiquée comme optionnelle. Si elle est développée dès le sprint 1, elle crée toutefois un second chemin pour la connexion et l'envoi du vote, à synchroniser avec l'urne Java. Pour commencer, gardons un seul traitement métier côté urne Java; une interface web pourra être étudiée ensuite sans remplacer le client Java demandé.

```text
SAE_3.01_voteNumerique/
├── README.md
├── bdd/
│   ├── 001_initialisation.sql      # schéma et contraintes
│   └── README.md                   # règles d'utilisation de la base
├── src/
│   ├── main/
│   │   ├── java/fr/sae301/vote/
│   │   │   ├── commun/
│   │   │   │   ├── modele/          # Utilisateur, Electeur, Referendum, Role, Statut
│   │   │   │   ├── protocole/       # Message, TypeMessage : échanges par sockets
│   │   │   │   └── crypto/          # ClePublique, BulletinChiffre, agrégation
│   │   │   ├── urne/               # ServeurUrne, authentification, vote, résultats
│   │   │   │   └── persistance/     # connexion JDBC et requêtes SQL
│   │   │   ├── electeur/           # programme ClientElecteur et interface Java
│   │   │   └── scrutateur/         # programme de dépouillement, clé privée locale
│   │   └── resources/              # configuration sans secret
│   └── test/java/                 # tests Java, notamment crypto et protocole
├── tests/test_schema.py           # contrôle des contraintes SQL déjà prêt
└── docs/
    ├── analyse/                   # dossier d'analyse, DSS, diagrammes
    ├── architecture/              # décisions et schémas techniques
    └── maquettes/                 # HTML/CSS existants si conservés comme maquettes
```

Cette arborescence décrit les **emplacements à utiliser au fil des sprints**. Il n'est pas utile de créer maintenant des classes Java vides. Les fichiers HTML/CSS présents dans le dépôt sont le travail d'interface déjà commencé par l'équipe : ils restent en place jusqu'à ce que leur auteur les intègre ou les classe comme maquettes. Aucun script `login.php` ou `traiter_vote.php` n'est nécessaire à l'architecture Java prévue dans le sujet.

Le code peut être organisé en paquets Java sans imposer immédiatement Maven ou Gradle. Le choix de l'outil de construction et du SGBD doit être commun à l'équipe. La clé privée du scrutateur et les fichiers de base locale ne doivent jamais être versionnés.

## Diagrammes de séquence système du dossier d'analyse

Le PDF contient bien **dix DSS**, un pour chacun des dix cas d'utilisation, aux pages 4 à 13. La lecture du seul texte extrait du PDF ne montrait pas leurs images. La présence demandée par le sujet est donc satisfaite. Les ajustements ci-dessous visent la cohérence des messages avec les scénarios nominaux et le protocole.

| Page | Cas | Ajustement utile |
| --- | --- | --- |
| 4 | Générer les clés | Montrer la **demande de génération** par le scrutateur. Le diagramme passe directement de l'authentification à « envoie la clé publique ». Préciser que seule la clé publique arrive à l'urne; la clé privée reste locale. |
| 5 | Gérer les électeurs | Remplacer « une action de choix » et « valider les modifications » par un échange concret du scénario nominal, par exemple `ajouterElecteur(nom, prenom, identifiant)` puis confirmation. La modification et la suppression sont d'autres parcours à préciser si nécessaire. |
| 6 | Gérer les référendums | Le DSS représente une modification; l'indiquer clairement et nommer les données transmises (titre, question, dates). |
| 7 | S'authentifier | Le scénario nominal est représenté. La première connexion et les trois échecs relèvent des alternatives déjà décrites. |
| 8 | Lancer le référendum | La réponse « liste des référendums prêts » suppose que la question, les électeurs inscrits et la clé publique ont été vérifiés. Faire apparaître cette condition dans le libellé de la réponse ou dans une note. |
| 9 | Voter | Ajouter la demande et la remise de la clé publique prévues par le scénario. Préciser que le **client Java chiffre le choix avant l'envoi à l'urne**. Un DSS garde le système comme boîte noire; un diagramme technique séparé pourra montrer client, urne et scrutateur. |
| 10 | Clôturer | Le scénario nominal est représenté. Préciser que la confirmation de l'urne intervient après fermeture effective des votes. |
| 11 | Dépouiller | Renommer « résultats chiffrés » en **agrégat chiffré final** : le scrutateur ne reçoit pas les bulletins individuels. Il renvoie seulement le total déchiffré, puis l'urne publie le résultat. |
| 12 | Consulter les résultats | Remplacer le second message `listeReferendums` par `selectionnerReferendum(id)`. La réponse doit être le résultat publié de ce référendum; si le dépouillement n'est pas terminé, l'alternative décrite dans le dossier s'applique. |
| 13 | Changer son mot de passe | Le DSS est présent et cohérent avec le scénario nominal. Rappeler dans l'implémentation que le nouveau mot de passe est haché avant stockage. |

Pour distinguer les deux niveaux : les **DSS** montrent les échanges entre un acteur et le système vu comme une boîte noire. Les échanges internes entre client Java, serveur Java, base de données et scrutateur appartiennent à des diagrammes de conception technique complémentaires.
