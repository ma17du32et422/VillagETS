# Rapport de projet - VillagETS

### Liste des membres de l'équipe avec identifiant Github

Ricardo Ramirez - @Guarded0

Jeremy Chheang - @Bobasaur00

Théo Houlachi - @Yeosuwacos

Charles Lesage - @ma17du32et422

Loïc Beaudin-Kerzérho - @LoicBeaudin

Xavier Van Winden - @XaviervwETS

Ares Gabrielyan - @DrunkAxolotl


### Description l'objectif principal de l'application . 

Mettre une emphase sur les objectifs ayant changé depuis le rapport d'analyse.
Les tags... le ui... android :/ 

### Comparaison des technologies décidée durant le rapport d'analyse et des technologies finalement utilités 

Nous avons finalement utilisé une base de données Supabase (sql) au lieu de Oracle. L'interface de la plateforme et le fait que la base de données soit hostée en ligne a simplifié le développement. 
Supabase permet de faire des queries et de changer plein de paramètres seulement avec quelques clics et est partageable par Github pour collaborer. Ça a été un bon choix.

Nous avons également utilisé un système pour stocker les fichiers qui n'était pas mentionné au début. L'API REST recoit le fichier qui sera ensuite stocké sur un volume du Docker. Tous les fichiers sont listés sur notre db et accessibles via apivillagets.lesageserveur.com/uploads/"nomFichier".

Le site web est public et accessible au https://villagets.lesageserveur.com . Nous avons utilisé un serveur personnel avec l'hyperviseur Proxmox d'installé, créé un container Debian dedans, puis ouvert le projet dedans pour l'exécuter avec Docker.
Le site est accessible grâce à un tunnel Cloudflare qui permet d'ouvrir son réseau local sans faire de redirection de ports, donc d'ouvrir des ports de son réseau de maison.

Pour les technologies de base, nous sommes resté au plan initial.

### Diagramme d'architecture du projet
Le diagramme d'architecture doit comprendre :

* Les différents clients utilisés (web, mobile)
* Le serveur
* La BD
    * Les connexions entre les différents éléments précédents
    * Pour chaque élément précédent, indiquez les technologies qui le soutiennent

### Revue des tâches des sprints
Pour chaque sprint, indiqué dans un tableau :
* Le nombre de tâches de chaque taille complété par chaque membre
* Le nombre de tâches assignées en tout dans l'équipe.

### Rétrospective des procédés du projet
Écrivez un paragraphe sur une amélioration possible que votre équipe aurait pu prendre pour facilité le projet.

### Perspective future
Écrivez un paragraphe qui indique le prochain élément important qui serait développé pour votre application.
