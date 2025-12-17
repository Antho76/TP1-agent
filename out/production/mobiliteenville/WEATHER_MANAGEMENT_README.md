# Système de Gestion Météorologique pour le Transport

## Fonctionnalités Implémentées (5 points)

### 1. Règles d'Adaptation au Climat
- **Pluie et/ou vent très fort (≥15 m/s)** : Les trajets à vélo ne sont pas proposés
- **Vent fort (≥10 m/s)** : Les trajets à vélo sont proposés avec un temps majoré de 50%
- **Neige** : Les trajets en voiture sont proposés avec un temps majoré de 50% et un coût majoré de 20%

### 2. Intégration API OpenWeatherMap
- Inscription gratuite requise sur [OpenWeatherMap](https://openweathermap.org/api)
- 60 utilisations par minute gratuites
- Configuration automatique de la clé API via fichier `weather.properties`
- Cache des données météo (10 minutes par défaut)

### 3. Interface de Sélection de Ville
- Menu déroulant dans `TravellerGui` pour choisir la ville
- Villes disponibles : Lille, Paris, Lyon, Marseille, Toulouse, Nice, Nantes, Strasbourg, Montpellier, Bordeaux
- Bouton de rafraîchissement des données météo
- Affichage en temps réel de l'impact météo

### 4. Adaptation Dynamique des Propositions
- Filtrage automatique des voyages selon les conditions météo
- Ajustement des durées et coûts en temps réel
- Messages informatifs sur les impacts météorologiques

## Classes Créées/Modifiées

### Classes Créées
1. **WeatherManager.java**
   - Gestionnaire singleton pour les conditions météorologiques
   - Analyse des conditions : pluie, neige, vent fort/très fort
   - Calcul des facteurs d'ajustement pour durée et coût
   - Cache intelligent des données météo

2. **WeatherConfig.java**
   - Gestionnaire de configuration pour l'API météo
   - Création automatique du fichier `weather.properties`
   - Gestion sécurisée des clés API

3. **WeatherManagementTest.java**
   - Tests de démonstration du système météorologique
   - Validation des adaptations de voyages
   - Tests de filtrage et configuration

### Classes Modifiées
1. **Journey.java**
   - Ajout de `baseDuration` et `baseCost` (valeurs avant ajustement météo)
   - Méthodes `applyWeatherAdjustments()` et `isAvailableWithWeather()`
   - Calcul automatique des ajustements lors de la création
   - Méthode `getWeatherImpactDescription()` pour l'affichage

2. **JourneysList.java**
   - Filtrage des voyages selon les conditions météo dans `findDirectJourneys()`
   - Vérification de disponibilité météo dans `findIndirectJourney()`
   - Méthode `refreshWeatherAdjustments()` pour mise à jour globale

3. **TravellerGui.java**
   - Ajout d'un panneau de sélection de ville
   - Affichage des informations météo en temps réel
   - Bouton de rafraîchissement des données météo

4. **AgenceGui.java**
   - Affichage de l'impact météo sur les transports
   - Bouton de mise à jour des ajustements météo
   - Interface étendue pour informations météorologiques

5. **AgenceAgent.java**
   - Ajout de méthodes `getCatalog()` et `getWindow()` pour l'accès GUI

6. **Meteo.java** (existante)
   - Intégration avec `WeatherConfig` pour la clé API
   - Utilisation de la configuration centralisée

## Configuration

### 1. Clé API OpenWeatherMap
1. Créer un compte gratuit sur [OpenWeatherMap](https://openweathermap.org/api)
2. Obtenir votre clé API
3. Modifier le fichier `weather.properties` créé automatiquement :
```properties
openweather.api.key=VOTRE_CLE_API_ICI
openweather.api.url=http://api.openweathermap.org/data/2.5/weather
weather.cache.duration.minutes=10
weather.default.city=Lille
```

### 2. Utilisation
- Lancer l'application normalement
- Sélectionner la ville dans l'interface `TravellerGui`
- Les adaptations météo sont appliquées automatiquement
- Utiliser le bouton "🌤️ Refresh Weather" pour mettre à jour

## Exemples d'Utilisation

### Test Manuel
```java
// Tester le système météo
WeatherManagementTest.main(new String[]{});

// Changer de ville
WeatherManager weatherManager = WeatherManager.getInstance();
weatherManager.setCurrentCity("Paris");

// Vérifier impact sur voyages
Journey bikeJourney = new Journey("LILLE", "VALENCIENNES", "BIKE", 900, 60, 2.0);
System.out.println("Vélo autorisé: " + bikeJourney.isAvailableWithWeather());
System.out.println("Impact météo: " + bikeJourney.getWeatherImpactDescription());
```

### Conditions Météo Simulées
Pour les tests sans connexion Internet, le système utilise des conditions par défaut :
- **Temps clair** : Aucun impact
- **Vent fort** : Durée vélo +50%
- **Pluie** : Vélos non proposés
- **Neige** : Voiture durée +50%, coût +20%

## Seuils de Conditions
- **Vent fort** : ≥ 10 m/s (36 km/h)
- **Vent très fort** : ≥ 15 m/s (54 km/h)
- **Majoration vent** : +50% durée vélo
- **Majoration neige** : +50% durée voiture, +20% coût voiture

## Logs et Debugging
Le système génère des logs détaillés :
- Changements de ville
- Mise à jour des données météo
- Application des ajustements
- Erreurs de connexion API

Utilisez `WeatherManager.getInstance().getWeatherImpactDescription()` pour obtenir un résumé des impacts en cours.