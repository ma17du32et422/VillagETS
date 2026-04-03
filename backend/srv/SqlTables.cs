using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;

namespace sql
{
    [Table("utilisateur")]
    public class Utilisateur : BaseModel
    {
        [PrimaryKey("id_utilisateur")]
        public string? Id { get; set; }

        [Column("pseudo")]
        public string? Pseudo { get; set; }

        [Column("password")]
        public string? Password { get; set; }

        [Column("date_creation")]
        public DateTime? DateCreation { get; set; }

        [Column("nom")]
        public string? Nom { get; set; }

        [Column("prenom")]
        public string? Prenom { get; set; }

        [Column("email")]
        public string? Email { get; set; }

        [Column("annee_naissance")]
        public DateTime? AnneeNaissance { get; set; }

        [Column("main_admin")]
        public bool? MainAdmin { get; set; }

        [Column("photo_profil")]
        public string? PhotoProfil { get; set; }
    }

    [Table("publication")]
    public class Publication : BaseModel
    {
        [PrimaryKey("id_publication")]
        public string? Id { get; set; }

        [Column("utilisateurid")]
        public string? UtilisateurId { get; set; }

        [Column("nom")]
        public string? Nom { get; set; }

        [Column("date_publication")]
        public DateTime? DatePublication { get; set; }

        [Column("contenu")]
        public string? Contenu { get; set; }

        [Column("likes")]
        public int? Likes { get; set; }

        [Column("dislikes")]
        public int? Dislikes { get; set; }

        [Column("prix")]
        public decimal? Prix { get; set; }

        [Column("article_a_vendre")]
        public bool? ArticleAVendre { get; set; }
    }

    [Table("commentaire")]
    public class Commentaire : BaseModel
    {
        [PrimaryKey("id_commentaire")]
        public string? Id { get; set; }

        [Column("utilisateurid")]
        public string? UtilisateurId { get; set; }

        [Column("publicationid")]
        public string? PublicationId { get; set; }

        [Column("parent_commentaire")]
        public string? ParentCommentaire { get; set; }

        [Column("contenu")]
        public string? Contenu { get; set; }

        [Column("likes")]
        public int? Likes { get; set; }

        [Column("dislikes")]
        public int? Dislikes { get; set; }
    }

    [Table("categorie_publication")]
    public class CategoriePublication : BaseModel
    {
        [PrimaryKey("id_categorie_publication")]
        public string? Id { get; set; }

        [Column("nom")]
        public string? Nom { get; set; }

        [Column("categorie_base")]
        public bool? CategorieBase { get; set; }
    }

    [Table("cat_pub_publication")]
    public class CatPubPublication : BaseModel
    {
        [PrimaryKey("id_categorie_publication", false)]
        public string? IdCategorie { get; set; }

        [PrimaryKey("publicationid_publication", false)]
        public string? IdPublication { get; set; }
    }

    [Table("fichier")]
    public class Fichier : BaseModel
    {
        [PrimaryKey("id_fichier")]
        public Guid? Id { get; set; }

        [Column("nom")]
        public string? Nom { get; set; }

        [Column("id_proprietaire")]
        public Guid? IdProprietaire { get; set; }

        [Column("lien_fichier")]
        public string? LienFichier { get; set; }

        [Column("type")]
        public string? Type { get; set; }
    }

    [Table("publication_fichier")]
    public class PublicationFichier : BaseModel
    {
        [PrimaryKey("id_fichier", false)]
        public string? IdFichier { get; set; }

        [PrimaryKey("id_publication", false)]
        public string? IdPublication { get; set; }
    }

    [Table("commentaire_fichier")]
    public class CommentaireFichier : BaseModel
    {
        [PrimaryKey("id_fichier", false)]
        public string? IdFichier { get; set; }

        [PrimaryKey("id_commentaire", false)]
        public string? IdCommentaire { get; set; }
    }

    [Table("message")]
    public class Message : BaseModel
    {
        [PrimaryKey("id_message")]
        public string? Id { get; set; }

        [Column("contenu")]
        public string? Contenu { get; set; }

        [Column("envoyeur_id_utilisateur")]
        public string? EnvoyeurId { get; set; }
    }

    [Table("message_utilisateur")]
    public class MessageUtilisateur : BaseModel
    {
        [PrimaryKey("messageid_message", false)]
        public string? MessageId { get; set; }

        [PrimaryKey("receveur_id_utilisateur", false)]
        public string? ReceveurId { get; set; }
    }

    [Table("message_fichier")]
    public class MessageFichier : BaseModel
    {
        [PrimaryKey("id_message", false)]
        public string? MessageId { get; set; }

        [PrimaryKey("id_fichier", false)]
        public string? FichierId { get; set; }
    }
}