-- SAE 3.01 - premiere migration SQLite.
-- Toutes les dates sont des instants UTC au format YYYY-MM-DDTHH:MM:SSZ.
-- Activer les cles etrangeres sur CHAQUE connexion JDBC : PRAGMA foreign_keys = ON.

CREATE TABLE IF NOT EXISTS utilisateurs (
    id INTEGER PRIMARY KEY,
    identifiant TEXT NOT NULL COLLATE NOCASE UNIQUE,
    mot_de_passe_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('ELECTEUR', 'ORGANISATEUR', 'SCRUTATEUR')),
    doit_changer_mdp INTEGER NOT NULL DEFAULT 1 CHECK (doit_changer_mdp IN (0, 1)),
    tentatives_echouees INTEGER NOT NULL DEFAULT 0 CHECK (tentatives_echouees BETWEEN 0 AND 3),
    verrouille_jusqua TEXT,
    actif INTEGER NOT NULL DEFAULT 1 CHECK (actif IN (0, 1)),
    cree_le TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    CHECK (length(trim(identifiant)) > 0 AND identifiant = trim(identifiant)),
    CHECK (length(mot_de_passe_hash) > 0)
);

CREATE TABLE IF NOT EXISTS electeurs (
    utilisateur_id INTEGER PRIMARY KEY REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    nom TEXT NOT NULL CHECK (length(trim(nom)) > 0),
    prenom TEXT NOT NULL CHECK (length(trim(prenom)) > 0)
);

CREATE TABLE IF NOT EXISTS referendums (
    id INTEGER PRIMARY KEY,
    titre TEXT NOT NULL CHECK (length(trim(titre)) > 0),
    question TEXT NOT NULL CHECK (length(trim(question)) > 0),
    ouvre_le TEXT NOT NULL,
    ferme_le TEXT NOT NULL,
    statut TEXT NOT NULL DEFAULT 'BROUILLON'
        CHECK (statut IN ('BROUILLON', 'OUVERT', 'CLOTURE', 'DEPOUILLE')),
    cree_par INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    cree_le TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    CHECK (ouvre_le < ferme_le)
);

-- Un electeur est inscrit a un referendum precis. Sa participation ne contient
-- ni son choix ni le bulletin chiffre : elle sert uniquement au vote unique.
CREATE TABLE IF NOT EXISTS inscriptions (
    referendum_id INTEGER NOT NULL REFERENCES referendums(id) ON DELETE RESTRICT,
    electeur_id INTEGER NOT NULL REFERENCES electeurs(utilisateur_id) ON DELETE RESTRICT,
    a_vote_le TEXT,
    PRIMARY KEY (referendum_id, electeur_id)
);
CREATE INDEX IF NOT EXISTS idx_inscriptions_electeur ON inscriptions(electeur_id);

-- Uniquement la cle PUBLIQUE et le bulletin AGREGE chiffre. La cle privee
-- reste sur le poste du scrutateur. Les grands entiers sont stockes en decimal.
CREATE TABLE IF NOT EXISTS urnes_chiffrees (
    referendum_id INTEGER PRIMARY KEY REFERENCES referendums(id) ON DELETE RESTRICT,
    cle_publique_p TEXT NOT NULL,
    cle_publique_g TEXT NOT NULL,
    cle_publique_h TEXT NOT NULL,
    agregat_u TEXT NOT NULL DEFAULT '1',
    agregat_v TEXT NOT NULL DEFAULT '1',
    nombre_votes INTEGER NOT NULL DEFAULT 0 CHECK (nombre_votes >= 0)
);

-- Le resultat publie est global : aucun lien avec l'identite d'un electeur.
CREATE TABLE IF NOT EXISTS resultats (
    referendum_id INTEGER PRIMARY KEY REFERENCES referendums(id) ON DELETE RESTRICT,
    voix_pour INTEGER NOT NULL CHECK (voix_pour >= 0),
    nombre_votes INTEGER NOT NULL CHECK (nombre_votes >= 0),
    publie_le TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    CHECK (voix_pour <= nombre_votes)
);

-- La base protege aussi les associations entre compte et role.
CREATE TRIGGER IF NOT EXISTS electeur_avec_bon_role
BEFORE INSERT ON electeurs
WHEN (SELECT role FROM utilisateurs WHERE id = NEW.utilisateur_id) <> 'ELECTEUR'
BEGIN
    SELECT RAISE(ABORT, 'le compte doit avoir le role ELECTEUR');
END;

CREATE TRIGGER IF NOT EXISTS electeur_avec_bon_role_modification
BEFORE UPDATE OF utilisateur_id ON electeurs
WHEN (SELECT role FROM utilisateurs WHERE id = NEW.utilisateur_id) <> 'ELECTEUR'
BEGIN
    SELECT RAISE(ABORT, 'le compte doit avoir le role ELECTEUR');
END;

CREATE TRIGGER IF NOT EXISTS referendum_cree_par_organisateur
BEFORE INSERT ON referendums
WHEN (SELECT role FROM utilisateurs WHERE id = NEW.cree_par) <> 'ORGANISATEUR'
BEGIN
    SELECT RAISE(ABORT, 'le createur doit avoir le role ORGANISATEUR');
END;

CREATE TRIGGER IF NOT EXISTS referendum_cree_par_organisateur_modification
BEFORE UPDATE OF cree_par ON referendums
WHEN (SELECT role FROM utilisateurs WHERE id = NEW.cree_par) <> 'ORGANISATEUR'
BEGIN
    SELECT RAISE(ABORT, 'le createur doit avoir le role ORGANISATEUR');
END;

CREATE TRIGGER IF NOT EXISTS role_avec_historique
BEFORE UPDATE OF role ON utilisateurs
WHEN OLD.role <> NEW.role AND (
    EXISTS (SELECT 1 FROM electeurs WHERE utilisateur_id = OLD.id) OR
    EXISTS (SELECT 1 FROM referendums WHERE cree_par = OLD.id)
)
BEGIN
    SELECT RAISE(ABORT, 'le role est utilise par des donnees existantes');
END;

-- L'intitule, la question et les dates ne changent plus apres l'ouverture.
CREATE TRIGGER IF NOT EXISTS referendum_immuable_apres_ouverture
BEFORE UPDATE OF titre, question, ouvre_le, ferme_le, cree_par ON referendums
WHEN OLD.statut <> 'BROUILLON'
BEGIN
    SELECT RAISE(ABORT, 'referendum deja lance');
END;

CREATE TRIGGER IF NOT EXISTS referendum_non_supprimable_apres_ouverture
BEFORE DELETE ON referendums
WHEN OLD.statut <> 'BROUILLON'
BEGIN
    SELECT RAISE(ABORT, 'referendum deja lance');
END;

-- Les vues publiques n'exposent que les referendums depouilles.
CREATE VIEW IF NOT EXISTS resultats_publics AS
SELECT r.id AS referendum_id, r.titre, r.question,
       s.voix_pour, s.nombre_votes - s.voix_pour AS voix_contre,
       s.nombre_votes, s.publie_le
FROM referendums AS r
JOIN resultats AS s ON s.referendum_id = r.id
WHERE r.statut = 'DEPOUILLE';
