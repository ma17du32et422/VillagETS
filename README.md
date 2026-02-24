# TCH099 - Projet intégrateur - Application de forum publique pour étudiants

**Contexte du Projet**

Notre projet se veut d'offrir un moyen de communication et d'entraide numérique simplifié entre les étudiants de l'ÉTS (et de d'autres milieux étudiants). Les applications web et mobiles seront assez semblables permettant un meilleur accès multiplateforme selon les préférences des utilisateurs. La page principale de l'application est un flux des idées et commentaires de la communauté étudiante en continu, permettant de discuter sur divers sujets et de créer de nouvelles connexions. Nous utiliserons un API Rest pour gérer l'interaction des utilisateurs entre l'application et le serveur.

**Objectifs Pédagogiques**
-Mettre à profit nos apprentissages en utilisant les outils qui nous ont été fournit lors de notre cheminement en développement web, Android, base de données et serveur.
-Concevoir et comprendre une architecture API REST pour l'échange de données entre les différents utilisateurs et le serveur.
-Apprendre à concevoir un projet logiciel avec une équipe et respecter un échéancier concis.
-Diviser notre projet en déléguant les diverses tâches selon les compétences de chaque programmeur. 

**Description du projet:**

L'application se divise en trois composants principaux :

1. **Application mobile android pour utilisateurs** :
   - Salon publique de clavardage entre tous les étudiants.
   - Les publications sont de sujets divers, permettant également l'envoie de photos/vidéos.
   - Filtre entre les types de publication
   - Fonctionnalité de ventes d'objets (petites annonces)
   - Salons privés entre les étudiants: 1 à 1 ou mini-groupes de clavardage.

2. **Application web d'administration** :
   - Tableaux de bord pour la gestion des utilisateurs.
   - Outils de filtres des publications
   - NextJS pour simplifier le développement

3. **API REST en C#** :
   - Gestion des différentes publications et clavardages publiques/privés
   - Gestion de l'authentification des utilisateurs en communicant avec la base de données Oracle SQL.

**Fonctionnalités de l'application**

1. **Application mobile Android** :
   - **Authentification** : Connexion sécurisée pour tous les utilisateurs.
   - **Gestion des forums publiés**: Gérer ses propres publications et messages.
   - **Notifications** : Alertes pour les nouvelles publications et messages.
   - **Collaboration** : Chat entres les étudiants

2. **Interface Web** :
   - **Tableau de Bord** : Vue d'ensemble de tous les utilisateurs et fonctionnalités de modération
   - **Rapports d'utilisation** : Statistiques générales d'utilisation des utilisateurs

3. **API REST en C#** :
   - **Endpoints des publications** : Création, modificaftion et suppression des publications
   - **Gestion des Utilisateurs** : Inscription, authentification et modération des utilisateurs, différents rôles et permissions.
   - **Gestion des messageries** : Envoie/réception des messages entre utilisateurs.

**Critères de Réussite**

- **Fonctionnalité** : Le projet doit intégrer l'ensemble des fonctionnalités attendues, autant pour les utilisateurs que pour les organisateurs.
- **Utilisabilité** :  Le UI doit être simple, clair et agréable à prendre en main.
- **Fiabilité** : L'application doit offrir un haut niveau de sécurité et fonctionner de manière stable.
- **Performance** : L'application doit supporter un volume d'utilisation important tout en garantissant des réponses rapides.
- **Maintenabilité** : Le code doit être propre, organisé, bien commenté et conçu pour faciliter les corrections et les évolutions futures.

**Livraisons**

1. **Documentation du Projet** :
   - Spécifications détaillées des exigences fonctionnelles.
   - Architecture du système et choix technologiques.
   - Guides d'utilisation pour les différentes interfaces.
   - Rapport de tests et d'assurance qualité.

2. **Code Source** :
   - Code source pour l'application Android, l'interface Web et l'API REST.
   - Instructions pour la configuration et le déploiement.

3. **Présentation Finale** :
   - Démonstration du fonctionnement de l'application.
   - Explication des choix techniques et des défis rencontrés.

**Évaluation**

Le projet sera évalué sur :

- La conformité aux exigences fonctionnelles et techniques.
- La qualité et la propreté du code.
- L'efficacité de l'interface utilisateur.
- La performance et la stabilité de l'application.
- La qualité de la documentation et de la présentation.

**Conclusion**

Ce projet offre une occasion exceptionnelle de développer une application complète qui intègre des compétences variées en développement logiciel et en gestion de projet. Il vous permettra de comprendre comment les différentes technologies et plateformes peuvent être combinées pour créer une solution efficace répondant aux besoins réels des équipes de projet. Nous attendons avec impatience de voir vos solutions innovantes et efficaces!
