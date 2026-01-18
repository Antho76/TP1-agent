# 🚀 Guide de Démarrage - Système Multi-Agents de Transport Urbain

## ⚡ Compilation

### Windows PowerShell
```powershell

javac -encoding UTF-8 -cp ".;lib/*" agents\*.java comportements\*.java data\*.java gui\*.java launch\*.java examples\*.java test\*.java
```

## 🚀 Lancer le Système

```powershell
# Windows PowerShell
java -cp ".;lib/*" launch.LaunchSimu
```

```bash
# Unix / macOS
java -cp ".:lib/*" launch.LaunchSimu

## � Tests Individuels

```powershell
# Windows PowerShell
java -cp ".;lib/*" test.ConsoleTestTraveller
java -cp ".;lib/*" test.TestTravellerGuiWithOllama
java -cp ".;lib/*" data.CapacityManagementTest
java -cp ".;lib/*" data.WeatherManagementTest
```

```bash
# Unix / macOS
java -cp ".:lib/*" test.ConsoleTestTraveller
java -cp ".:lib/*" test.TestTravellerGuiWithOllama
java -cp ".:lib/*" data.CapacityManagementTest
java -cp ".:lib/*" data.WeatherManagementTest
```

## 📅 Version
**Date:** 18 janvier 2026
**Version:** 2.1 - Enhanced with AI-Generated Confirmations

## ✨ Nouvelles Fonctionnalités avec Ollama

### Requêtes en Langage Naturel
Décrivez votre voyage en français naturel et Ollama comprend automatiquement :
- Station de départ et d'arrivée
- Horaire préféré
- Type de transport
- Critère de sélection (coût, durée, écologie, etc.)

**Exemples :**
- "Je veux aller de a vers c à 14h en bus, le moins cher"
- "Trajet rapide de b à f à 9h du matin"
- "Transport écologique de d à e vers 16h30"

### Messages de Confirmation Personnalisés
Quand vous réservez un trajet, Ollama génère un **message de confirmation unique et enthousiaste** au lieu d'un message générique.

**Exemples de messages générés :**
- "🎉 Excellent choix! Vous voyagerez en style. N'oubliez pas votre billet numérique!"
- "✨ Parfait! Votre trajet est confirmé et optimisé pour votre portefeuille. À bientôt!"
- "🚀 Génial! Vous êtes prêt à explorer. Pensez à vérifier les conditions de trafic avant de partir!"

**Bon voyage avec le système multi-agents ! 🚀🚌🚲🚋**
