# donation-cqrs-api

Côté lecture du CQRS du socle **myc-public** : recherche du back-office dans Elasticsearch, alimenté par les événements
de `inner-donation-api` (Kafka, Debezium, sink Kafka Connect). Spring Boot 4, Java 21, parent Maven `craft-parent`.
Cohérence à terme : une création est visible après son passage dans la chaîne ; `indexedAt` le rappelle dans chaque réponse.

Place dans le socle : navigateur -> APISIX externe -> APISIX interne -> **donation-cqrs-api** -> Elasticsearch.
Vue d'ensemble et décisions : [platform-handbook](https://github.com/myc-public/platform-handbook) (ADR 0036, 0037).

## API

| Requête | Permission | Filtres |
|---|---|---|
| `GET /api/v1/search/donations` | `donation:list` | `category`, `country`, `donorId`, `from`, `to` |
| `GET /api/v1/search/donors` | `donor:list` | `q` (nom, prénom), `email`, `country` |

Pagination : `page` (0 par défaut), `size` (20 par défaut, 100 au plus), 10 000 résultats au plus.
Les tokens viennent du realm `myc-internal`, audience `donation-api`, scope `donation:read`.
Réponses : 400 (paramètre invalide), 401, 403, 503 (Elasticsearch indisponible).

## Démarrer en local

Le service tourne dans la pile (`gitops-platform/local/stack`) :

```powershell
mvn -DskipTests package
Set-Location ..\gitops-platform\local\stack
docker compose up -d --build donation-cqrs-api
```

## Tests

```powershell
mvn verify     # tests unitaires et MockMvc (sécurité, pagination, erreurs), sans Docker
```

## Configuration

| Variable | Rôle |
|---|---|
| `OIDC_INTERNAL_ISSUER_URI`, `OIDC_INTERNAL_JWK_SET_URI` | realm `myc-internal` |
| `ELASTICSEARCH_URL`, `ELASTICSEARCH_USERNAME` | Elasticsearch, compte `eda-read` |
| `ELASTICSEARCH_PASSWORD` | mot de passe du compte, sans valeur par défaut |
