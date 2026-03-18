### **Base de données** 
db utilisée: Supabase SQL
Lien: https://rkemgkyymxadkjwkxizn.supabase.co/rest/v1/
apikey à mettre en headers: sb_publishable_sogmdAShEZTEGT1V1i9iTA_iTJkkvxa

Pour faire des requêtes HTTP (ex. sur Postman):

- Pour accéder par GET, à la fin du lien ajouter le nom de la table (ex. utilisateur).
- Pour accéder par POST, idem, et ajouter dans le body en raw json les valeurs à ajouter.

### **Pour faire des requêtes dans la base des données à partir du backend:**

- Nous établissons une connexion avec la base de données Supabase en utilisant le package Supabase pour .NET, en fournissant l'URL de la base de données ainsi que la clé API, qui sont stockées dans des variables d'environnement.
- Nous pouvons ensuite créer des classes pour nos tables SQL et utiliser des méthodes incluent dans le package pour effectuer tous types de commandes SQL, comme les exemples suivants :
---
#### **Récupérer des données :** 
```cs
// Étant donné le modèle suivant (Ville.cs)
[Table("villes")]
class Ville : BaseModel
{
    [PrimaryKey("id")]
    public int Id { get; set; }
    [Column("nom")]
    public string Nom { get; set; }
    [Column("pays_id")]
    public int PaysId { get; set; }
    //... etc.
}
// Un résultat peut être récupéré de la façon suivante.
var resultat = await supabase.From().Get();
var villes = resultat.Models
```
---
#### **Insérer des données :**
```cs 
[Table("villes")]
class Ville : BaseModel
{
    [PrimaryKey("id", false)]
    public int Id { get; set; }
    [Column("nom")]
    public string Nom { get; set; }
    [Column("pays_id")]
    public int PaysId { get; set; }
}
var modele = new Ville
{
  Nom = "La Comté",
  PaysId = 554
};
await supabase.From().Insert(modele);
```
---
#### **Mettre à jour des données :**
```cs
var miseAJour = await supabase
  .From()
  .Where(x => x.Nom == "Auckland")
  .Set(x => x.Nom, "Terre du Milieu")
  .Update();
```
---
## **Comment executer:**

**Allez dans le dossier racine** où se trouve le dossier backend et **exécutez cette commande** dans **l'invite de commandes** afin d'initialiser le docker container:
```bash
docker compose up --build
```
Une fois que le Docker container a démarré,
- vous pouvez envoyer des **requêtes HTTP** à [localhost:5000](http://localhost:5000/) pour tester l'api
- vous pouvez accéder au **site web** sur [localhost:80](http://localhost:80/)

