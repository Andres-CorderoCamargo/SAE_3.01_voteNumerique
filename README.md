# SAE_3.01 Système de vote électronique sécurisé

Ce projet consiste à développer un **système de vote électronique sécurisé** permettant de créer et gérer des référendums en ligne au sein d'une entreprise.

L'application repose sur une architecture **client-serveur en Java**, utilisant des sockets pour les communications. La confidentialité des votes est assurée grâce au **chiffrement ElGamal**, permettant d'agréger les bulletins chiffrés sans avoir à les déchiffrer individuellement.

Le système permet notamment de :

- Gérer les électeurs et les référendums.
- Authentifier les utilisateurs selon leur rôle.
- Lancer, clôturer et dépouiller un référendum.
- Chiffrer et enregistrer les votes.
- Calculer et publier les résultats.
- Garantir la confidentialité et l'intégrité des votes.

