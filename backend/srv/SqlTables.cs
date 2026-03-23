using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;

namespace sql
{
    [Table("categorie_publication")]
    class CategoriePublication : BaseModel
    {
        [PrimaryKey("id_categorie_publication")]
        public string? Id { get; set; }
        [Column("nom")]
        public string? Nom { get; set; }
        [Column("categorie_base")]
        public bool? CategorieBase { get; set; }
    }

    [Table("utilisateur")]
    class Utilisateur : BaseModel
    {
        [PrimaryKey("id_utilisateur")]
        public string? Id { get; set; }
        [Column("pseudo")]
        public string? Pseudo { get; set; }
        [Column("password")]
        public string? Password { get; set; }
        [Column("nom")]
        public string? Nom { get; set; }
        [Column("prenom")]
        public string? Prenom { get; set; }
        [Column("email")]
        public string? Email { get; set; }
        [Column("date_creation")]
        public DateTime? DateCreation { get; set; }
        [Column("annee_naissance")]
        public DateTime? AnneeNaissance { get; set; }
        [Column("main_admin")]
        public bool? MainAdmin { get; set; }
    }

    [Table("publication")]
    class Publication : BaseModel
    {
        [PrimaryKey("id_publication")]
        public string? Id { get; set; }
        [Column("utilisateurid")]
        public string? UtilisateurId { get; set; }
        [Column("nom")]
        public string? Nom { get; set; }
        [Column("contenu")]
        public string? Contenu { get; set; }
        [Column("likes")]
        public int? Likes { get; set; }
        [Column("dislikes")]
        public int? Dislikes { get; set; }
    }

    [Table("commentaire")]
    class Commentaire : BaseModel
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

    [Table("message")]
    class Message : BaseModel
    {
        [PrimaryKey("id_message")]
        public string? Id { get; set; }
        [Column("contenu")]
        public string? Contenu { get; set; }
        [Column("envoyeur_id_utilisateur")]
        public string? EnvoyeurId { get; set; }
    }

    [Table("piece_jointe")]
    class PieceJointe : BaseModel
    {
        [PrimaryKey("id_piece_jointe")]
        public string? Id { get; set; }
        [Column("nom")]
        public string? Nom { get; set; }
        [Column("lien_fichier")]
        public string? LienFichier { get; set; }
        [Column("type")]
        public string? Type { get; set; }
    }

    [Table("article_a_vendre")]
    class ArticleAVendre : BaseModel
    {
        [PrimaryKey("id_article")]
        public string? Id { get; set; }
        [Column("nom")]
        public string? Nom { get; set; }
        [Column("description")]
        public string? Description { get; set; }
        [Column("prix")]
        public decimal? Prix { get; set; }
        [Column("id_publication")]
        public string? PublicationId { get; set; }
    }
}
