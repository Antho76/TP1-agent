# 🚀 Guide de Démarrage - Système Multi-Agents de Transport Urbain

## ⚡ Démarrage Rapide

### 🏃 Lancer le Projet en 2 Commandes

```bash
# 1. Compiler le projet
cd "/Users/antho/Desktop/projets/TP ADAM"
javac -cp ".:lib/*" agents/*.java comportements/*.java data/*.java gui/*.java launch/*.java examples/*.java

# 2. Exécuter la simulation
java -cp ".:lib/*" launch.LaunchSimu
```

**C'est parti ! 🎉** L'interface JADE et les agents vont démarrer.

---

## 📋 Table des Matières
1. [Démarrage Rapide](#-démarrage-rapide)
2. [Compilation et Exécution Détaillée](#-compilation-et-exécution-détaillée)
3. [Utilisation Rapide](#-utilisation-rapide)
4. [Description du Projet](#-description-du-projet)
5. [Prérequis](#-prérequis)
6. [Installation Complète](#-installation-complète)
7. [Fonctionnalités](#-fonctionnalités)
8. [Utilisation Détaillée](#-utilisation)
9. [Architecture](#-architecture)
10. [Tests](#-tests)
11. [Configuration](#-configuration)
12. [Dépannage](#-dépannage)

---

## 🏗️ Compilation et Exécution Détaillée

### Compilation Complète
```bash
cd "/Users/antho/Desktop/projets/TP ADAM"

# Compiler tous les fichiers Java
javac -cp ".:lib/*" agents/*.java comportements/*.java data/*.java gui/*.java launch/*.java examples/*.java test/*.java
```

### Exécution - Version Standard (Recommandée)
```bash
# Lancer la simulation complète avec interface JADE
java -cp ".:lib/*" launch.LaunchSimu
```

### Exécution - Version Améliorée avec Ollama
```bash
# Lancer avec support IA et langage naturel (nécessite Ollama)
java -cp ".:lib/*" launch.LaunchEnhancedSimu
```

### Exécution - Tests Individuels
```bash
# Test GUI voyageur avec Ollama
java -cp ".:lib/*" test.TestTravellerGuiWithOllama

# Test console simple
java -cp ".:lib/*" test.ConsoleTestTraveller

# Test gestion des capacités
java -cp ".:lib/*" data.CapacityManagementTest

# Test météo
java -cp ".:lib/*" data.WeatherManagementTest
```

---

##  Description du Projet

Système multi-agents intelligent basé sur JADE pour la gestion de voyages urbains. Les voyageurs peuvent rechercher et réserver des trajets en utilisant différents modes de transport (bus, tram, vélo, voiture) en tenant compte de multiples critères (coût, durée, confort, émissions CO2).

### 🎯 Objectifs
- Protocole Contract-Net pour négociation entre agents
- Optimisation multi-critères des trajets
- Gestion dynamique des capacités et disponibilités
- Intégration météo en temps réel
- Interface en langage naturel avec IA (Ollama)
- Système d'alertes et réservations

---

## ⚙️ Prérequis

### Logiciels Requis
- **Java JDK 11+** (recommandé: JDK 17)
- **Ollama** (optionnel, pour l'IA en langage naturel)

### Librairies Incluses
Les librairies suivantes sont déjà présentes dans le dossier `lib/`:
- `JadeUPHF.jar` - Framework multi-agents JADE
- `opencsv-3.9-3.jar` - Lecture des fichiers CSV
- `json-20250517.jar` - Manipulation JSON

### API Externe
- **OpenWeatherMap API** (pour la météo en temps réel)
  - Clé API configurée dans `weather.properties`

---

## 🔧 Installation Complète

### 1. Cloner ou Télécharger le Projet
```bash
cd ~/Desktop/projets
# Le projet devrait être dans: TP ADAM/
```

### 2. Installer Ollama (Optionnel - pour IA)
```bash
# macOS / Linux
curl -fsSL https://ollama.ai/install.sh | sh

# Télécharger le modèle
ollama pull granite3.3:latest
# ou
ollama pull llama3.2:latest

# Démarrer le service
ollama serve
```

### 3. Vérifier Ollama
```bash
curl http://localhost:11434/api/tags
```

---

## 💻 Utilisation Rapide

### Interface Graphique

Une fois le projet lancé, vous verrez:
- **Interface JADE** - Gestionnaire des agents
- **Interface Voyageur** - Pour faire des demandes de trajet
- **Interfaces Agences** - Affichent les catalogues et réservations

### Faire une Demande de Trajet

**Option 1: Langage Naturel (avec Ollama)**
```
"Je veux aller de a vers c à 9h en bus, option économique"
```

**Option 2: Contrôles Manuels**
- Sélectionner origine: `a`
- Sélectionner destination: `c`
- Heure de départ: `9`
- Critère: `cost`
- Type transport: `bus`
- Cliquer sur "Buy Travel"

---

## 🎨 Fonctionnalités

### 🤖 1. Système Multi-Agents (JADE)
- **Agents Voyageurs** (`TravellerAgent`)
  - Émission d'appels d'offres
  - Analyse et sélection des propositions
  - Gestion des réservations
  - Réaction aux alertes

- **Agents Agences** (`AgenceAgent`)
  - Spécialisés par mode de transport (bus, tram, vélo, voiture)
  - Réponse aux appels d'offres (protocole Contract-Net)
  - Gestion des catalogues de trajets
  - Mise à jour des disponibilités

- **Agents Alertes** (`AlertAgent`)
  - Diffusion d'alertes en temps réel
  - Notification des perturbations
  - Annulation automatique des trajets impactés

### 🧠 2. Intelligence Artificielle (Ollama)
- **Compréhension du langage naturel**
  - Analyse de requêtes en français
  - Extraction automatique des paramètres
  - Support des demandes complexes

- **Exemples de requêtes supportées:**
  ```
  "Je veux aller de a vers c à 9h en bus, option économique"
  "Trajet rapide de b vers f en vélo après 14h"
  "Transport confortable de d à e en tram vers 8h"
  "Aller de a vers f vers midi, meilleur prix"
  "Voyage écologique de c vers e vers 16h"
  ```

### 🌤️ 3. Gestion Météorologique
- **Intégration OpenWeatherMap**
  - Données météo en temps réel pour Lille
  - Mise à jour automatique toutes les 10 minutes

- **Impact sur les trajets:**
  - ☔ **Pluie + vent fort:** vélos non proposés
  - 💨 **Vent fort:** durée vélo +50%
  - ❄️ **Neige:** durée voiture +50%, coût +20%

### 🚲 4. Gestion des Capacités
- **Vélos:** 20 par zone
  - Décrémentation à l'emprunt
  - Retour différé dans la zone d'arrivée
  - Disponibilité temps réel

- **Transports en commun:**
  - Bus: 50 places par trajet
  - Tram: 200 places par trajet
  - Voitures: 3 places par trajet
  - Mise à jour après chaque réservation

### 🔔 5. Système d'Alertes
- **Types d'alertes:**
  - Annulation de trajet
  - Perturbations du trafic
  - Incidents techniques

- **Réactions automatiques:**
  - Notification des voyageurs impactés
  - Proposition de trajets alternatifs
  - Remboursement ou remplacement

### 📊 6. Optimisation Multi-Critères
- **Critères de choix:**
  - 💰 **cost** - Prix minimum
  - ⏱️ **duration** - Temps le plus court
  - 🛋️ **confort** - Confort maximal
  - 🌱 **co2** - Émissions minimales
  - ⚖️ **duration-cost** - Meilleur compromis

- **Trajets composés:**
  - Combinaison de plusieurs modes de transport
  - Optimisation des correspondances
  - Calcul automatique des totaux (durée, coût, CO2)

---

## 💻 Utilisation

### Interface Graphique Voyageur

#### 1. Panneau Supérieur - Demande en Langage Naturel
- **Champ de texte** pour saisir votre demande
- **Bouton "Process Request with AI"** pour analyser avec Ollama
- **Bouton "Show Examples"** pour voir des exemples
- **Affichage météo** en temps réel

#### 2. Panneau Central - Résultats
- Affichage des propositions de trajets
- Détails: durée, coût, émissions CO2, confort
- Messages de l'agent
- Confirmations de réservation

#### 3. Panneau Inférieur - Contrôles Manuels (Fallback)
- **Sélection manuelle** si Ollama non disponible
- Origine, destination, heure de départ
- Critère de choix, type de transport
- **Bouton "Buy Travel"** pour lancer la recherche

### Workflow Typique

```
1. Démarrer l'application
   └─> Interface JADE + Agents s'initialisent

2. Saisir une demande en langage naturel
   └─> "Je veux aller de a vers f à 9h en bus, pas cher"

3. Cliquer sur "Process Request with AI"
   └─> Ollama analyse la demande
   └─> Extraction des paramètres
   └─> Lancement automatique de la recherche

4. Consultation des résultats
   └─> Liste des trajets proposés
   └─> Comparaison selon critères

5. Confirmation automatique
   └─> Réservation du meilleur trajet
   └─> Mise à jour des capacités
```

---

## 🏛️ Architecture

### Structure des Packages

```
TP ADAM/
├── agents/                    # Agents JADE
│   ├── TravellerAgent.java   # Agent voyageur
│   ├── AgenceAgent.java      # Agent agence
│   └── AlertAgent.java       # Agent alertes
│
├── comportements/            # Comportements des agents
│   ├── ContractNetAchat.java # Protocole achat (voyageur)
│   ├── ContractNetVente.java # Protocole vente (agence)
│   ├── ClientAlertHandler.java # Gestion alertes client
│   └── AlertHandler.java     # Gestion alertes agence
│
├── data/                     # Modèles de données
│   ├── Journey.java          # Trajet simple
│   ├── ComposedJourney.java  # Trajet composé
│   ├── JourneysList.java     # Liste de trajets
│   ├── BikeZoneManager.java  # Gestion vélos
│   ├── WeatherManager.java   # Gestion météo
│   └── TextEnhancementService.java # Service IA texte
│
├── gui/                      # Interfaces graphiques
│   ├── TravellerGui.java     # Interface voyageur
│   ├── AgenceGui.java        # Interface agence
│   └── AlertGui.java         # Interface alertes
│
├── launch/                   # Classes de lancement
│   ├── LaunchSimu.java       # Version standard
│   └── LaunchEnhancedSimu.java # Version avec Ollama
│
├── test/                     # Tests unitaires
│   ├── TestTravellerGuiWithOllama.java
│   ├── CapacityManagementTest.java
│   ├── WeatherManagementTest.java
│   └── ...
│
├── examples/                 # Exemples
│   └── TravelRequestExamples.java
│
└── lib/                      # Librairies externes
    ├── JadeUPHF.jar
    ├── opencsv-3.9-3.jar
    └── json-20250517.jar
```

### Données CSV

Les catalogues de trajets sont définis dans:
- `bus.csv` - Lignes de bus
- `tram.csv` - Lignes de tram
- `bike.csv` - Pistes cyclables
- `car.csv` - Covoiturage

**Format CSV:**
```csv
start,stop,means,departure,duration,distance,co2,comfort,cost
a,b,bus,600,15,5,2,7,2
```

### Réseau de Transport

```
Zones: A, B, C, D, E, F

🚴 Véloroutes (vert): a-b, b-c, c-d, d-e, e-f
🚌 Bus (noir): a-b, b-c, b-d, c-e, e-f
🚋 Tram (bleu): a-d, d-f
🚗 Voiture (rouge): a-f, c-f
```

---

## 🧪 Tests

### Tests de Fonctionnalités

```bash
# Test interface Ollama
java -cp ".:lib/*" test.TestTravellerGuiWithOllama

# Test gestion capacités
java -cp ".:lib/*" data.CapacityManagementTest

# Test météo
java -cp ".:lib/*" data.WeatherManagementTest

# Test amélioration texte
java -cp ".:lib/*" data.TextEnhancementTest

# Test format temps
java -cp ".:lib/*" test.TimeFormatTest

# Test suppression trajets
java -cp ".:lib/*" test.TripRemovalTest
```

### Tests Console

```bash
# Test console voyageur
java -cp ".:lib/*" test.ConsoleTestTraveller

# Test affichage trajets
java -cp ".:lib/*" test.TripDisplayTest
```

---

## 📚 Configuration

### Fichier `weather.properties`
```properties
# OpenWeatherMap API Configuration
weather.api.key=votre_clé_api
weather.api.url=http://api.openweathermap.org/data/2.5/weather
weather.default.city=Lille
weather.cache.duration=600000
```

### Fichier `ollama-config.properties`
```properties
# Ollama Configuration
ollama.url=http://localhost:11434
ollama.model=granite3.3:latest
ollama.timeout=30000
```

---

## 🐛 Dépannage

### Problème: Ollama non disponible
**Symptôme:** Message "Ollama service not available"
**Solution:**
```bash
# Vérifier si Ollama est lancé
curl http://localhost:11434/api/tags

# Redémarrer Ollama
ollama serve
```
**Alternative:** Utiliser les contrôles manuels de l'interface

### Problème: Erreurs de compilation
**Symptôme:** Classes non trouvées
**Solution:**
```bash
# Vérifier le classpath
export CLASSPATH=".:lib/*"

# Recompiler
javac -cp ".:lib/*" agents/*.java comportements/*.java data/*.java gui/*.java launch/*.java
```

### Problème: Interface JADE ne s'affiche pas
**Symptôme:** Fenêtre JADE absente
**Solution:**
- Vérifier que l'option `-gui` est bien dans les arguments
- Sur macOS: vérifier les autorisations d'affichage
- Essayer sans l'interface: retirer `-gui` du code LaunchSimu

### Problème: Erreur TopicManagementService
**Symptôme:** ServiceNotActiveException
**Solution:**
- Utiliser `LaunchSimu.java` au lieu de `LaunchEnhancedSimu.java`
- Vérifier la configuration JADE dans le code

---

## 📖 Documentation Supplémentaire

- **README_OLLAMA_INTEGRATION.md** - Détails intégration Ollama
- **MODIFICATIONS_SUMMARY.md** - Résumé des modifications
- **TRIP_REMOVAL_DOCUMENTATION.md** - Gestion suppression trajets
- **readme.md** - Documentation originale du projet

---

## 👥 Auteurs
- Emmanuel Adam (architecture originale)
- Améliorations et extensions (IA, météo, capacités, alertes)

---

## 📅 Version
**Date:** 16 janvier 2026
**Version:** 2.0 - Enhanced with AI

---

## 🎯 Prochaines Améliorations Possibles
- [ ] Intégration API trafic routier temps réel
- [ ] Système de fidélité et réductions
- [ ] Historique des trajets par utilisateur
- [ ] Prédiction de la demande par ML
- [ ] Application mobile
- [ ] Support multi-langues pour l'IA
- [ ] Visualisation cartographique des trajets

---

**Bon voyage avec le système multi-agents ! 🚀🚌🚲🚋**
