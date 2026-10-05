# Base de données — première version (sprint 1)

Cette version utilise **SQLite** pour pouvoir démarrer le projet Java sans installer de serveur de base de données. Le choix du SGBD reste à valider avec l'équipe. La migration est dans [`001_initialisation.sql`](001_initialisation.sql).

## Ce que contient la base

| Table | Rôle |
| --- | --- |
| `utilisateurs` | Identifiant unique, empreinte du mot de passe, rôle, première connexion et blocage temporaire après trois échecs. |
| `electeurs` | Identité minimale de l'électeur, liée à un compte utilisateur. |
| `referendums` | Question, période de vote, état et organisateur créateur. |
| `inscriptions` | Électeurs autorisés pour chaque référendum et indication qu'ils ont voté. |
| `urnes_chiffrees` | Clé **publique**, agrégat chiffré et nombre de votes. Aucune clé privée. |
| `resultats` | Décompte global après dépouillement. La vue `resultats_publics` ne montre que les référendums dépouillés. |

Les trois premières tables correspondent aux tâches de création du sprint 1. Les trois dernières préparent les cas d'utilisation du sujet et évitent de confondre « inscrit » et « a voté ». Il n'existe ni colonne `choix` associée à un électeur, ni table liant son identité à un bulletin chiffré.

## Initialisation

Créer une base vide et exécuter la migration avec un outil SQLite ou, en Python :

```sh
python3 -c "import sqlite3; c=sqlite3.connect('vote.db'); c.execute('PRAGMA foreign_keys=ON'); c.executescript(open('bdd/001_initialisation.sql', encoding='utf-8').read()); c.close()"
```

Exécuter la commande depuis la racine du dépôt. Le fichier `vote.db` est une donnée locale et ne doit pas être versionné. Chaque connexion Java devra aussi exécuter `PRAGMA foreign_keys=ON`; utiliser un pilote SQLite JDBC à choisir avec l'équipe.

## Règles à appliquer dans le programme Java

- Hacher le mot de passe **avant** l'insertion, selon le format choisi par Zakaria. Ne jamais enregistrer le mot de passe en clair. `mot_de_passe_hash` contient la chaîne complète produite par l'algorithme (avec sel et paramètres si inclus).
- À l'authentification, retrouver l'utilisateur par `identifiant`, puis vérifier l'empreinte dans le code Java. Après trois échecs consécutifs, fixer `verrouille_jusqua`; remettre le compteur à zéro après une connexion réussie ou à l'expiration du blocage. La durée du blocage reste à décider.
- Si `doit_changer_mdp=1`, imposer le changement avant d'ouvrir une session normale.
- La base interdit de modifier ou supprimer un référendum lancé. Pour « supprimer » un électeur dans l'interface, désactiver son compte (`actif=0`) afin de conserver l'historique. L'application doit refuser cette opération s'il a déjà voté à un référendum en cours, comme l'indique le dossier d'analyse. La base interdit aussi de rattacher un électeur à un compte d'un autre rôle.
- Lors d'un vote, vérifier l'état `OUVERT`, la période, l'inscription et `a_vote_le IS NULL`. Dans **une seule transaction**, marquer la participation, mettre à jour l'agrégat `(u,v)` et incrémenter `nombre_votes`. Une contrainte de table seule ne suffit pas pour garantir ces trois changements ensemble.
- Ne publier le résultat qu'après clôture et dépouillement par le scrutateur. Le nombre de voix contre vaut `nombre_votes - voix_pour`. Ne jamais envoyer ou conserver la clé privée dans cette base.
- L'application doit vérifier qu'un bulletin chiffré représente bien 0 ou 1. Le schéma SQL ne peut pas le prouver.

Les instants sont enregistrés en UTC au format `YYYY-MM-DDTHH:MM:SSZ`, afin de pouvoir comparer les périodes sans ambiguïté. Les colonnes des grands entiers ElGamal sont du texte décimal, adapté à `BigInteger` en Java.

## Points à confirmer avec l'équipe

1. SGBD final : SQLite, PostgreSQL ou MariaDB. La migration SQL devra être adaptée si le choix change.
2. Format exact de l'empreinte des mots de passe et durée du blocage temporaire.
3. Définition précise d'un électeur éligible et qui crée les comptes organisateur/scrutateur.
4. Mise en œuvre de la preuve que chaque vote chiffré vaut 0 ou 1 et de la protection des échanges réseau.

Le sujet officiel donne l'authentification en base comme amélioration possible; le dossier d'analyse de l'équipe et le Trello l'ont retenue pour le projet.
