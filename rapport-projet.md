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

Notre objectif, étant de faciliter la communication entre les membres de la communauté universitaire, reste inchangé depuis la création de notre projet. Par contre, notre but n'est plus de créer un forum pour connecter étudiants, mais plutôt d'utiliser un système de publication afin d'unifier les fonctionnalités de vente et de discussions publiques. 


### Comparaison des technologies décidée durant le rapport d'analyse et des technologies finalement utilités 

Nous avons finalement utilisé une base de données Supabase (sql) au lieu de Oracle. L'interface de la plateforme et le fait que la base de données soit hostée en ligne a simplifié le développement. 
Supabase permet de faire des queries et de changer plein de paramètres seulement avec quelques clics et est partageable par Github pour collaborer. Ça a été un bon choix.



Nous avons également utilisé le système inclus dans .NET pour stocker les fichiers qui n'était pas mentionné au début. L'API REST recoit le fichier qui sera ensuite stocké sur un volume du Docker. Tous les fichiers sont listés sur notre db et servis statiquement via https://apivillagets.lesageserveur.com/uploads/"nomFichier". 

Le site web est public et accessible au https://villagets.lesageserveur.com . Nous avons utilisé un serveur personnel avec l'hyperviseur Proxmox d'installé, créé un container Debian dedans, puis ouvert le projet dedans pour l'exécuter avec Docker.
Le site est accessible grâce à un tunnel Cloudflare qui permet d'ouvrir son réseau local sans faire de redirection de ports, donc d'ouvrir des ports de son réseau de maison.

Pour le site web, nous avons utilisé Vite pour compiler et servir le website à partir du container. 


Pour faciliter la maintenance du serveur nous utilisé Open telemetry et pour encrypter les mots de passe, nous utilisons BCrypt.


Pour les technologies de base, nous sommes resté au plan initial.

### Diagramme d'architecture du projet

CHARLES LESAGE
Architecture de l'application et des différents clients:


Routes du tunnel Cloudflare
<img width="1200" height="696" alt="routes" src="https://github.com/user-attachments/assets/d44023ad-5166-4b00-9f64-130943a5f359" />

Base de données SQL Supabase:
<img width="1460" height="933" alt="schema-tablesSQL" src="https://github.com/user-attachments/assets/4a7b8b3a-ccd5-4a87-bb60-3f1ea0eec7e6" />

### Revue des tâches des sprints

THEO 
Pour chaque sprint, indiqué dans un tableau :
* Le nombre de tâches de chaque taille complété par chaque membre
* Le nombre de tâches assignées en tout dans l'équipe.

### Rétrospective des procédés du projet
Nous aurions pu mieux utiliser les outils fournis par GitHub pour mieux gérer les branches et les potentiels merge conflicts. De plus, nous aurions pu mieux gérer notre temps, car nous avons respecté les échéanciers des sprints dans leur entièreté. Les tâches se sont empilées, mais elles ont été complétées avant la remise finale.

### Perspective future
Si nous continuons à développer le projet, nous considérons adapter l'interface web pour développer une version iOS et d'héberger la base de données localement pour ne plus dépendre d'une entitée externe.
