# QualiTrace

<p align="center">
  <img src="docs/assets/logo-qualitrace.png" alt="QualiTrace Logo" width="200" height="206">
  <br>
  <b>Pilotage de la conformité et traçabilité des flux industriels.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0_M1-brightgreen?style=for-the-badge&logo=spring" alt="Spring Boot 4">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License">
  <br>
  <img src="https://img.shields.io/badge/Status-In_Development-yellow?style=flat-square" alt="Status">
  <img src="https://img.shields.io/badge/Build-Passing-success?style=flat-square" alt="Build">
</p>

---

## 📝 Présentation du projet
**QualiTrace** est une solution légère de **LIMS** (Laboratory Information Management System) et de **MES** (Manufacturing Execution System) dédiée aux industries critiques (Pharmaceutique, Cosmétique).

Le projet vise à garantir l'intégrité des données (**ALCOA+**) et la traçabilité totale du cycle de vie des produits, de la réception des matières premières à la libération des produits finis.

### 🌟 Fonctionnalités clés (MVP)
- **Gestion des Référentiels :** Articles, Spécifications, Fournisseurs et Utilisateurs.
- **Traçabilité des Lots :** Attribution de n° de lots internes, gestion des dates de péremption et statuts (Quarantaine, Libéré, Rejeté).
- **Laboratoire & Qualité :** Saisie des résultats d'analyse avec validation automatique selon les spécifications.
- **Audit Trail :** Journalisation immuable de toutes les actions critiques (Conformité 21 CFR Part 11).

---

## 🛠 Stack Technique
* **Backend :** Java 21, Spring Boot 4.0, Spring Data JPA.
* **API :** RESTful avec support **HATEOAS** pour une navigation hypermédia.
* **Documentation :** OpenAPI / Swagger UI.
* **Base de données :** PostgreSQL (Persistence) & Redis (Cache/Session).
* **Build Tool :** Gradle (Kotlin DSL).
* **DevOps :** Docker & Docker Compose.

---

## 🚀 Démarrage rapide

### Pré-requis
* Docker & Docker Compose
* JDK 21 (pour le développement local)


### Récupération du projet

```bash
# Cloner le projet
git clone https://github.com/neeftarah/qualitrace.git
```

### Lancement avec Docker
```bash
# Lancer les services (Base de données, etc.)
docker-compose up -d

# Lancer l'application
./gradlew bootRun
```
