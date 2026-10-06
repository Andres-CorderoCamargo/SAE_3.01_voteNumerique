"""Vérifications des contraintes de la première migration SQLite."""

import sqlite3
import unittest
from pathlib import Path


SCHEMA = Path(__file__).resolve().parents[1] / "bdd" / "001_initialisation.sql"


class SchemaTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.db.execute("PRAGMA foreign_keys = ON")
        self.db.executescript(SCHEMA.read_text(encoding="utf-8"))
        self.db.execute(
            "INSERT INTO utilisateurs (id, identifiant, mot_de_passe_hash, role) "
            "VALUES (1, 'organisateur', 'empreinte-exemple', 'ORGANISATEUR')"
        )
        self.db.execute(
            "INSERT INTO utilisateurs (id, identifiant, mot_de_passe_hash, role) "
            "VALUES (2, 'alice', 'empreinte-exemple', 'ELECTEUR')"
        )
        self.db.execute(
            "INSERT INTO electeurs (utilisateur_id, nom, prenom) VALUES (2, 'Martin', 'Alice')"
        )
        self.db.execute(
            "INSERT INTO referendums "
            "(id, titre, question, ouvre_le, ferme_le, cree_par) "
            "VALUES (1, 'Essai', 'Acceptez-vous ?', "
            "'2026-10-10T08:00:00Z', '2026-10-11T08:00:00Z', 1)"
        )

    def tearDown(self):
        self.db.close()

    def test_identifiant_est_unique_sans_casse(self):
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO utilisateurs (identifiant, mot_de_passe_hash, role) "
                "VALUES ('ALICE', 'empreinte-exemple', 'ELECTEUR')"
            )

    def test_role_et_periode_invalides_sont_refuses(self):
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO utilisateurs (identifiant, mot_de_passe_hash, role) "
                "VALUES ('invalide', 'empreinte-exemple', 'ADMIN')"
            )
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO referendums "
                "(titre, question, ouvre_le, ferme_le, cree_par) "
                "VALUES ('Essai', 'Question', '2026-10-11T08:00:00Z', "
                "'2026-10-10T08:00:00Z', 1)"
            )

    def test_inscription_unique_et_reference_existante(self):
        self.db.execute(
            "INSERT INTO inscriptions (referendum_id, electeur_id) VALUES (1, 2)"
        )
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO inscriptions (referendum_id, electeur_id) VALUES (1, 2)"
            )
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO inscriptions (referendum_id, electeur_id) VALUES (1, 999)"
            )

    def test_resultat_visible_seulement_apres_depouillement(self):
        self.db.execute(
            "INSERT INTO resultats (referendum_id, voix_pour, nombre_votes) "
            "VALUES (1, 1, 2)"
        )
        self.assertEqual(
            self.db.execute("SELECT COUNT(*) FROM resultats_publics").fetchone()[0], 0
        )
        self.db.execute("UPDATE referendums SET statut='DEPOUILLE' WHERE id=1")
        self.assertEqual(
            self.db.execute(
                "SELECT voix_pour, voix_contre FROM resultats_publics"
            ).fetchone(), (1, 1)
        )

    def test_roles_et_referendum_lance_proteges(self):
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO electeurs (utilisateur_id, nom, prenom) "
                "VALUES (1, 'Erreur', 'Role')"
            )
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "UPDATE electeurs SET utilisateur_id=1 WHERE utilisateur_id=2"
            )
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute("UPDATE referendums SET cree_par=2 WHERE id=1")
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "UPDATE utilisateurs SET role='ORGANISATEUR' WHERE id=2"
            )
        self.db.execute("UPDATE referendums SET statut='OUVERT' WHERE id=1")
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "UPDATE referendums SET question='Nouvelle question' WHERE id=1"
            )
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute("DELETE FROM referendums WHERE id=1")

    def test_aucun_champ_de_choix_ni_cle_privee(self):
        colonnes = {
            row[1]
            for table in ("inscriptions", "urnes_chiffrees")
            for row in self.db.execute(f"PRAGMA table_info({table})")
        }
        self.assertNotIn("choix", colonnes)
        self.assertNotIn("cle_privee", colonnes)


if __name__ == "__main__":
    unittest.main()
