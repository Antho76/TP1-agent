# 🔧 Guide de Résolution des Problèmes API Météo

## Problème Rencontré : "Clé API invalide ou manquante"

### ✅ Solutions par Ordre de Priorité

#### 1. Vérifier la Clé API OpenWeatherMap

**Étapes à suivre :**

1. **Connectez-vous sur [openweathermap.org](https://openweathermap.org/)**
2. **Allez dans "My API keys"** (ou "API keys" dans votre profil)
3. **Vérifiez l'état de votre clé :**
   - ✅ **Active** : La clé fonctionne
   - ⏳ **Pending** : Attendez jusqu'à 2 heures
   - ❌ **Invalid** : Créez une nouvelle clé

#### 2. Corriger le Fichier Configuration

**Fichier : `weather.properties`**

```properties
# ✅ CORRECT
openweather.api.key=a40c8adb0ed9179f41224123d7f80d38

# ❌ INCORRECT (avec guillemets)
openweather.api.key="a40c8adb0ed9179f41224123d7f80d38"
```

#### 3. Nouvelle Clé API

Si votre clé ne fonctionne toujours pas :

1. **Créez un nouveau compte** sur openweathermap.org
2. **Vérifiez votre email** (lien de confirmation)
3. **Générez une nouvelle clé API**
4. **Remplacez la clé** dans `weather.properties`
5. **Attendez 10-120 minutes** pour l'activation

### 🔄 Mode Simulation (Solution Temporaire)

Le système bascule automatiquement en **mode simulation** si l'API ne fonctionne pas.

**Avantages du mode simulation :**
- ✅ Teste toutes les fonctionnalités météo
- ✅ Génère des conditions réalistes
- ✅ Démontre les impacts sur les transports
- ✅ Permet de continuer le développement

**Conditions simulées :**
- **Temps clair** (30%) : Aucun impact
- **Nuageux** (20%) : Aucun impact  
- **Pluie** (20%) : 🚫 Vélos interdits
- **Vent fort** (15%) : 🚴‍♂️ Vélos +50% durée
- **Neige** (15%) : 🚗 Voitures +50% durée, +20% coût

### 🧪 Test de Diagnostic

Compilez et exécutez le diagnostic :

```bash
cd /Volumes/SSD_Mac/Projets/ProjetAdam/mobiliteenville
javac -cp "lib/*:." data/WeatherApiDiagnostic.java
java -cp "lib/*:." data.WeatherApiDiagnostic
```

### 🌐 Test Manuel de l'API

```bash
# Test direct avec curl (remplacez VOTRE_CLE par votre clé)
curl "http://api.openweathermap.org/data/2.5/weather?q=London&appid=VOTRE_CLE&units=metric"

# Réponse attendue si clé valide :
{"coord":{"lon":-0.1257,"lat":51.5085},"weather":[...],"main":{...}}

# Réponse si clé invalide :
{"cod":401, "message": "Invalid API key. Please see https://openweathermap.org/faq#error401"}
```

### 📋 Checklist de Vérification

- [ ] ✅ Compte OpenWeatherMap créé et vérifié
- [ ] ✅ Clé API générée et statut "Active"  
- [ ] ✅ Clé copiée sans guillemets dans weather.properties
- [ ] ✅ Fichier weather.properties sans caractères spéciaux
- [ ] ✅ Connexion Internet fonctionnelle
- [ ] ✅ Firewall/proxy n'bloque pas api.openweathermap.org
- [ ] ⏳ Attente 10-120 min après création clé

### 🔄 Redémarrage Complet

1. **Fermez l'application**
2. **Vérifiez weather.properties**
3. **Supprimez les fichiers temporaires** (cache)
4. **Relancez l'application**

### 📞 Sources d'Aide

- **Documentation officielle :** [openweathermap.org/faq](https://openweathermap.org/faq)
- **Problèmes courants :** [openweathermap.org/faq#error401](https://openweathermap.org/faq#error401)
- **Support OpenWeatherMap :** contact via leur site web

### 💡 Note Importante

**Le système fonctionne parfaitement en mode simulation !** 

Vous pouvez :
- ✅ Tester toutes les fonctionnalités météo
- ✅ Voir l'impact sur les transports  
- ✅ Changer de ville dans l'interface
- ✅ Démontrer le système complet

L'API réelle n'est nécessaire que pour les **données météo en temps réel** en production.

---

*Le mode simulation est activé automatiquement et affiche "🔄 Mode simulation" dans l'interface.*