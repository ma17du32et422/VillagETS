using Supabase.Postgrest.Attributes;
using Supabase.Postgrest.Models;

namespace sql
{
    [Supabase.Postgrest.Attributes.Table("utilisateur")]
    public class Utilisateur : BaseModel
    {
        public const string DeletedUserPseudo = "[deleted user]";

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

        [Column("deleted_at")]
        public DateTime? DeletedAt { get; set; }

        public string GetDisplayPseudo() =>
            DeletedAt.HasValue || string.IsNullOrWhiteSpace(Pseudo)
                ? DeletedUserPseudo
                : Pseudo!;

        public object ToJson() => new
        {
            id = Id,
            pseudo = GetDisplayPseudo(),
            nom = DeletedAt.HasValue ? null : Nom,
            prenom = DeletedAt.HasValue ? null : Prenom,
            photoProfil = DeletedAt.HasValue ? null : PhotoProfil,
            dateCreation = DateCreation,
            deleted = DeletedAt.HasValue
        };
    }

    [Supabase.Postgrest.Attributes.Table("publication")]
    public class Publication : BaseModel
    {
        [PrimaryKey("id_publication")]
        public int? Id { get; set; }

        [Column("id_utilisateur")]
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
        [Column("nb_commentaires")]
        public int? CommentairesCount { get; set; }

        public object ToJson(string[]? media = null, Utilisateur? user = null, string? userReaction = null) => new
        {
            id = Id,
            titre = Nom,
            contenu = Contenu,
            media = media ?? [],
            datePublication = DatePublication,
            prix = Prix,
            articleAVendre = ArticleAVendre,
            likes = Likes ?? 0,
            dislikes = Dislikes ?? 0,
            commentaires = CommentairesCount ?? 0,
            userReaction = userReaction,
            op = new
            {
                id = user?.Id,
                pseudo = user?.GetDisplayPseudo(),
                photoProfil = user?.DeletedAt.HasValue == true ? null : user?.PhotoProfil
            }
        };
    }

    [Supabase.Postgrest.Attributes.Table("commentaire")]
    public class Commentaire : BaseModel
    {
        [PrimaryKey("id_commentaire", false)]
        public string? Id { get; set; }

        [Column("id_utilisateur")]
        public string? UtilisateurId { get; set; }

        [Column("id_publication")]
        public int? PublicationId { get; set; }

        [Column("parent_commentaire")]
        public string? ParentCommentaire { get; set; }

        [Column("date_commentaire")]
        public DateTime? DateCommentaire { get; set; }

        [Column("contenu")]
        public string? Contenu { get; set; }

        [Column("nb_reponses")]
        public int? NbReponses { get; set; }
        public object ToJson(Utilisateur? user = null) => new
        {
            id = Id,
            publicationId = PublicationId,
            parentCommentaire = ParentCommentaire,
            dateCommentaire = DateCommentaire,
            contenu = Contenu,
            nbReponses = NbReponses ?? 0,
            op = new
            {
                id = user?.Id,
                pseudo = user?.GetDisplayPseudo(),
                photoProfil = user?.DeletedAt.HasValue == true ? null : user?.PhotoProfil
            }
        };
    }

    [Supabase.Postgrest.Attributes.Table("categorie_publication")]
    public class CategoriePublication : BaseModel
    {
        [PrimaryKey("id_categorie_publication")]
        public string? Id { get; set; }

        [Column("nom")]
        public string? Nom { get; set; }

        [Column("categorie_base")]
        public bool? CategorieBase { get; set; }
    }

    [Supabase.Postgrest.Attributes.Table("cat_pub_publication")]
    public class CatPubPublication : BaseModel
    {
        [PrimaryKey("id_categorie_publication", false)]
        public string? IdCategorie { get; set; }

        [PrimaryKey("publicationid_publication", false)]
        public int? IdPublication { get; set; }
    }

    [Supabase.Postgrest.Attributes.Table("fichier")]
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

    [Supabase.Postgrest.Attributes.Table("publication_fichier")]
    public class PublicationFichier : BaseModel
    {
        [PrimaryKey("id_fichier", false)]
        [Column("id_fichier")]
        public string? IdFichier { get; set; }

        [PrimaryKey("id_publication", false)]
        [Column("id_publication")]
        public int? IdPublication { get; set; }
    }

    [Supabase.Postgrest.Attributes.Table("commentaire_fichier")]
    public class CommentaireFichier : BaseModel
    {
        [PrimaryKey("id_fichier", false)]
        public string? IdFichier { get; set; }

        [PrimaryKey("id_commentaire", false)]
        public string? IdCommentaire { get; set; }
    }

    [Supabase.Postgrest.Attributes.Table("message")]
    public class Message : BaseModel
    {
        [PrimaryKey("id_message")]
        public string? Id { get; set; }

        [Column("contenu")]
        public string? Contenu { get; set; }

        [Column("envoyeur_id_utilisateur")]
        public string? EnvoyeurId { get; set; }
    }

    [Supabase.Postgrest.Attributes.Table("message_utilisateur")]
    public class MessageUtilisateur : BaseModel
    {
        [PrimaryKey("messageid_message", false)]
        public string? MessageId { get; set; }

        [PrimaryKey("receveur_id_utilisateur", false)]
        public string? ReceveurId { get; set; }
    }

    [Supabase.Postgrest.Attributes.Table("message_fichier")]
    public class MessageFichier : BaseModel
    {
        [PrimaryKey("id_message", false)]
        [Column("id_message")]
        public string? MessageId { get; set; }

        [PrimaryKey("id_fichier", false)]
        [Column("id_fichier")]
        public string? FichierId { get; set; }
    }

    [Table("reaction_publication")]
    public class ReactionPublication : BaseModel
    {
        [PrimaryKey("id_utilisateur", false)]
        [Column("id_utilisateur")]
        public string? IdUtilisateur { get; set; }

        [PrimaryKey("id_publication", false)]
        [Column("id_publication")]
        public int? IdPublication { get; set; }

        [Column("type")]
        public string? Type { get; set; }
    }

    [Table("conversation")]
    public class Conversation : BaseModel
    {
        [PrimaryKey("id_conversation")]
        public string? Id { get; set; }
        [Column("user1_id")]
        public string? User1Id { get; set; }
        [Column("user2_id")]
        public string? User2Id { get; set; }
    }

    [Table("message")]
    public class ConversationMessage : BaseModel
    {
        [PrimaryKey("id_message")]
        public string? Id { get; set; }
        [Column("id_conversation")]
        public string? ConversationId { get; set; }
        [Column("envoyeur_id")]
        public string? EnvoyeurId { get; set; }
        [Column("receveur_id")]
        public string? ReceveurId { get; set; }
        [Column("date_msg")]
        public DateTime? DateMsg { get; set; }

        [Column("contenu")]
        public string? Contenu { get; set; }
    }
}
