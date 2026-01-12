# Interface Améliorée avec Ollama pour les Demandes de Transport

## Nouvelles Fonctionnalités

### 1. Interface de Demande en Langage Naturel
- **Champ de texte** pour saisir des demandes en français
- **Traitement IA** avec Ollama pour analyser et extraire les paramètres
- **Support des types de transport** : bus, car, bike, tram, ou "any"

### 2. Exemples de Demandes
Vous pouvez maintenant formuler vos demandes de voyage en langage naturel :

```
"Je veux aller de la station a à la station c à 9h du matin en bus, option la moins chère"
"Besoin d'un trajet de b vers f vers 14h30, priorité confort"
"Transport de d à e demain matin à vélo, le plus rapide possible"
"Aller de a à f en tram vers 18h, meilleur rapport durée-prix"
```

### 3. Types de Transport Supportés
- **bus** : transports en commun
- **car** : voiture
- **bike** : vélo
- **tram** : tramway  
- **any** : tous types de transport

### 4. Critères de Choix
- **cost** : prix le plus bas
- **duration** : temps de trajet le plus court
- **confort** : maximum de confort
- **co2** : impact environnemental minimal
- **duration-cost** : meilleur compromis durée-prix

## Configuration Ollama

### Prérequis
1. **Ollama installé** sur votre machine
2. **Service Ollama actif** sur `http://localhost:11434`
3. **Modèle LLM disponible** (par défaut : `llama3.2:latest`)

### Installation d'Ollama
```bash
# Télécharger et installer Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Télécharger un modèle (exemple avec llama3.2)
ollama pull llama3.2:latest

# Démarrer le service
ollama serve
```

### Vérification du Service
Testez que Ollama fonctionne :
```bash
curl http://localhost:11434/api/tags
```

## Utilisation

### 1. Démarrer l'Application
Lancez l'agent TravellerAgent via la classe principale ou utilisez le test :
```bash
java test.TestTravellerGuiWithOllama
```

### 2. Interface Utilisateur
1. **Panneau supérieur** : 
   - Sélection de la ville actuelle
   - Information météo
   - Champ de demande en langage naturel
   - Bouton "Process Request with AI"

2. **Panneau central** : 
   - Zone d'affichage des résultats
   - Messages de l'agent
   - Confirmations de réservation

3. **Panneau inférieur** : 
   - Contrôles manuels (fallback)
   - Sélection directe des paramètres
   - Bouton "Buy Travel"

### 3. Workflow de Demande
1. **Saisie** : Tapez votre demande en langage naturel
2. **Traitement** : Cliquez sur "Process Request with AI"
3. **Analyse** : Ollama analyse votre demande
4. **Extraction** : Les paramètres sont automatiquement extraits
5. **Exécution** : L'agent lance la recherche de voyage

## Fonctionnalités Avancées

### 1. Gestion d'Erreurs
- **Ollama indisponible** : fallback vers les contrôles manuels
- **Analyse échouée** : message d'erreur et suggestion d'utiliser les contrôles manuels
- **Paramètres invalides** : valeurs par défaut appliquées

### 2. Rétrocompatibilité
- **Interface manuelle** conservée
- **Anciens comportements** toujours fonctionnels
- **Migration progressive** possible

### 3. Intégration Météo
- **Contexte météorologique** pris en compte
- **Suggestions adaptées** selon les conditions
- **Recommandations transport** influencées par la météo

## Architecture Technique

### Classes Modifiées
1. **TravellerGui.java** : Interface utilisateur enrichie
2. **TravellerAgent.java** : Support du type de transport
3. **ContractNetAchat.java** : Paramètre transport type ajouté

### Nouvelles Méthodes
- `processNaturalLanguageRequest()` : Traitement IA
- `analyzeRequestWithOllama()` : Appel à l'API Ollama
- `parseAndExecuteRequest()` : Exécution après analyse
- `computeComposedJourney()` : Version avec type de transport

### Dépendances
- **org.json** : Manipulation JSON pour Ollama
- **java.net.http** : Client HTTP pour appels API
- **javax.swing** : Interface graphique

## Dépannage

### Problèmes Courants
1. **Ollama non accessible** :
   - Vérifiez que le service tourne : `ollama serve`
   - Testez la connectivité : `curl http://localhost:11434/api/tags`

2. **Modèle introuvable** :
   - Installez le modèle : `ollama pull llama3.2:latest`
   - Modifiez le nom du modèle dans `TravellerGui.java` si nécessaire

3. **Analyse incorrecte** :
   - Utilisez les contrôles manuels comme fallback
   - Reformulez votre demande de manière plus claire

### Logs et Debug
- Consultez la console pour les messages d'erreur
- Les réponses de l'IA sont affichées dans l'interface
- Les paramètres extraits sont visibles dans les logs

## Support et Contribution
- Rapportez les bugs via l'interface de debug
- Proposez des améliorations des prompts IA
- Contribuez à l'enrichissement des exemples de demandes