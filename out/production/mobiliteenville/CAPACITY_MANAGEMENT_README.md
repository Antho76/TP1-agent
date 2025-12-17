# Système de Gestion des Capacités de Transport

## Fonctionnalités Implémentées

### 1. Capacités par Type de Transport
- **Voiture** : 3 places par trajet
- **Vélo** : 20 vélos disponibles par zone
- **Bus** : 50 places par trajet  
- **Tram** : 200 places par trajet

### 2. Décrémentation des Places (5 points)
- Lors de la réservation d'un voyage, le nombre de places disponibles est automatiquement décrémenté
- Utilisation de la méthode `bookPlace()` dans la classe `Journey`
- Intégration dans le comportement `ContractNetVente` lors de l'acceptation d'une offre

### 3. Filtrage des Voyages Sans Places (1 point)
- Les voyages sans places disponibles ne sont plus proposés
- Modification des méthodes `findDirectJourneys()` et `findIndirectJourney()` dans `JourneysList`
- Vérification automatique de la disponibilité via `hasAvailablePlaces()`

### 4. Gestion Spéciale des Vélos (4 points)
- **Décrémentation zone départ** : Lors de la prise d'un vélo, le nombre de vélos dans la zone de départ est réduit
- **Incrémentation zone arrivée** : Le vélo est rendu disponible dans la zone d'arrivée après la date d'arrivée prévue
- **Gestion temporelle** : Les vélos sont disponibles selon un système de planning temporel

## Classes Modifiées/Créées

### Classes Modifiées
1. **Journey.java** 
   - Ajout des constantes de capacité pour chaque type de transport
   - Méthodes `bookPlace()`, `hasAvailablePlaces()`, `getAvailablePlaces()`
   - Initialisation automatique des places selon le type de transport

2. **JourneysList.java**
   - Filtrage des voyages sans places dans `findDirectJourneys()`
   - Vérification des places disponibles dans `findIndirectJourney()`

3. **ContractNetVente.java**
   - Utilisation de la nouvelle logique de réservation dans `removeTicket()`
   - Messages informatifs sur les réservations

4. **AgenceAgent.java**
   - Initialisation des zones de vélos au démarrage

### Classes Créées
1. **BikeZoneManager.java**
   - Gestionnaire singleton pour les vélos par zone
   - Gestion des transferts entre zones avec planification temporelle
   - Méthodes : `bookBike()`, `getAvailableBikes()`, `initializeZone()`

2. **BikeZoneInitializer.java**
   - Utilitaire pour initialiser les zones de vélos depuis les fichiers CSV
   - Détection automatique des zones utilisées par les vélos

3. **CapacityManagementTest.java**
   - Classe de test pour démontrer le bon fonctionnement du système

## Utilisation

### Initialisation
Les zones de vélos sont automatiquement initialisées au démarrage des agents :
- Lecture des fichiers CSV pour détecter les zones utilisées par les vélos
- Initialisation de chaque zone avec 20 vélos

### Réservation
1. **Transport classique** : Décrémentation directe du nombre de places
2. **Vélos** : Transfert entre zones avec gestion temporelle

### Vérification
- Utiliser `journey.hasAvailablePlaces(currentTime)` pour vérifier la disponibilité
- Utiliser `journey.bookPlace(currentTime)` pour réserver une place

## Exemples d'Utilisation

```java
// Créer un voyage en voiture
Journey carJourney = new Journey("LILLE", "PARIS", "CAR", 800, 120);
System.out.println("Places disponibles: " + carJourney.getPlaces()); // Affiche 3

// Réserver une place
boolean success = carJourney.bookPlace();
System.out.println("Réservation: " + success); // true
System.out.println("Places restantes: " + carJourney.getPlaces()); // Affiche 2

// Gérer les vélos
BikeZoneManager bikeManager = BikeZoneManager.getInstance();
bikeManager.initializeZone("LILLE");
System.out.println("Vélos à Lille: " + bikeManager.getAvailableBikes("LILLE", 800)); // Affiche 20

// Réserver un vélo
Journey bikeJourney = new Journey("LILLE", "VALENCIENNES", "BIKE", 900, 60);
boolean bikeSuccess = bikeJourney.bookPlace(800);
System.out.println("Vélos à Lille après réservation: " + 
                   bikeManager.getAvailableBikes("LILLE", 800)); // Affiche 19
```

## Tests
Exécuter `CapacityManagementTest.main()` pour tester toutes les fonctionnalités.