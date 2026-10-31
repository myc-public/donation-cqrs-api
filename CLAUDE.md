# donation-cqrs-api

Côté lecture du CQRS (Spring Boot 4, Java 21, client Elasticsearch 8). Lire d'abord [README.md](README.md).
Règles communes du socle : `platform-handbook`.

## Commandes

- Tests : `mvn verify` (sans Docker).
- Pile : `gitops-platform/local/stack` (`docker compose up -d --build donation-cqrs-api`, après `mvn package`).

## Règles propres à ce dépôt

- Lecture seule : aucune écriture dans Elasticsearch, aucun appel à `inner-donation-api`.
- Toujours les alias de lecture `donors` et `donations`, jamais un index `-vN` ni un alias d'écriture.
- Client Elasticsearch de la même version majeure que le serveur (8.19) : un client 9 est refusé.
- Requêtes par clauses typées (`SearchQueries`), jamais de concaténation.
- Chaque opération porte un `@PreAuthorize` ; les vues renvoyées n'exposent pas l'enveloppe d'événement.
- Données personnelles dans `DonorView` : jamais dans les logs.
- Nouveau test de sécurité : ligne `SEC-*` dans `inner-donation-api/postman/PLAN-DE-TEST-E2E.md` § 5.
