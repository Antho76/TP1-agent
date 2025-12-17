# Service d'Amélioration de Texte avec Ollama

Ce service améliore automatiquement les messages échangés entre l'agent voyageur et l'humain en utilisant des modèles de langage locaux via Ollama.

## Installation d'Ollama

1. **Téléchargez et installez Ollama** depuis [https://ollama.com/](https://ollama.com/)

2. **Téléchargez un modèle de langage** (recommandé : llama2 ou llama3)
   ```bash
   ollama pull llama2
   # ou
   ollama pull llama3
   ```

3. **Démarrez Ollama** (si pas déjà démarré automatiquement)
   ```bash
   ollama serve
   ```

4. **Testez l'installation**
   ```bash
   ollama run llama2
   # Tapez "Hello" et appuyez sur Entrée pour tester
   # Tapez "/bye" pour quitter
   ```

## Fonctionnalités

Le service améliore automatiquement plusieurs types de messages :

### Types de messages supportés

- **JOURNEY_PROPOSAL** : Propositions de voyage plus engageantes
- **BOOKING_CONFIRMATION** : Confirmations de réservation professionnelles
- **ERROR_MESSAGE** : Messages d'erreur plus compréhensibles et utiles  
- **WEATHER_IMPACT** : Informations météo avec conseils
- **GENERAL** : Communication générale améliorée

### Amélioration contextuelle

Le service prend en compte :
- Les conditions météorologiques actuelles
- Le type de transport concerné
- Le contexte du message (erreur, confirmation, proposition, etc.)

## Test du Service

Pour tester si Ollama est bien configuré :

```bash
cd /path/to/mobiliteenville
java -cp "lib/*:." data.TextEnhancementTest
```

## Utilisation dans l'Agent

Le service est automatiquement intégré dans :

- **TravellerAgent** : Messages de bienvenue et sélection de voyages améliorés
- **ContractNetAchat** : Messages d'erreur et confirmations de réservation améliorés

### Exemples d'amélioration

**Message original :**
> "no journey found !!!"

**Message amélioré :**
> "Je suis désolé, aucun voyage correspondant à vos critères n'a pu être trouvé. Voulez-vous essayer avec des critères différents ou une autre date ?"

**Proposition originale :**
> "I choose this journey : Bus from Lille to Paris, departure 14:30, duration 3h45, cost 25€"

**Proposition améliorée :**
> "Excellente nouvelle ! J'ai trouvé un voyage parfait pour vous : un bus confortable de Lille à Paris avec un départ à 14h30. Le trajet dure 3h45 pour seulement 25€ - un excellent rapport qualité-prix !"

## Configuration

Le service se configure automatiquement :
- **URL par défaut** : http://localhost:11434 (port standard d'Ollama)
- **Modèle par défaut** : Utilise le premier modèle disponible (préfère llama2/llama3)
- **Timeout** : 30 secondes par requête

## Fonctionnement en mode dégradé

Si Ollama n'est pas disponible, le service :
- Affiche les messages originaux sans modification
- Indique le statut "Offline" dans l'interface
- Continue de fonctionner normalement (mode de secours)

## Statut du Service

L'interface affiche le statut en temps réel :
- ✅ **Text Enhancement: Active (Model: llama2)** - Service opérationnel
- ❌ **Text Enhancement: Offline** - Ollama non disponible

## Dépannage

### Problème : "Ollama not available"

1. Vérifiez qu'Ollama est installé : `ollama --version`
2. Démarrez Ollama : `ollama serve`  
3. Vérifiez qu'un modèle est téléchargé : `ollama list`
4. Testez la connexion : `curl http://localhost:11434/api/tags`

### Problème : Réponses lentes

- Utilisez un modèle plus petit (ex: llama2 au lieu de llama3:70b)
- Augmentez la RAM disponible pour Ollama
- Vérifiez que le GPU est utilisé si disponible

### Problème : Messages non améliorés

- Vérifiez le statut dans l'interface utilisateur
- Le service fonctionne en mode de secours si Ollama n'est pas disponible
- Les messages originaux sont affichés sans modification

## Développement

Pour ajouter de nouveaux types de messages ou modifier les prompts système, voir :
- `data/TextEnhancementService.java` - Service principal
- `MessageType` enum - Types de messages supportés  
- `getSystemPrompt()` - Prompts système pour chaque type

## Performances

- Latence typique : 1-5 secondes par message (selon le modèle et la machine)
- Le service met en cache la connexion HTTP
- Timeout automatique pour éviter les blocages
- Mode de secours transparent si le service est indisponible