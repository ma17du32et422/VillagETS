-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.commentaire (
  id_commentaire uuid NOT NULL DEFAULT gen_random_uuid(),
  id_utilisateur uuid,
  id_publication integer,
  parent_commentaire uuid,
  date_commentaire timestamp without time zone,
  contenu character varying,
  nb_reponses integer NOT NULL DEFAULT 0,
  CONSTRAINT commentaire_pkey PRIMARY KEY (id_commentaire),
  CONSTRAINT fk_commentaire_parent FOREIGN KEY (parent_commentaire) REFERENCES public.commentaire(id_commentaire),
  CONSTRAINT commentaire_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id_utilisateur),
  CONSTRAINT commentaire_id_publication_fkey FOREIGN KEY (id_publication) REFERENCES public.publication(id_publication)
);
CREATE TABLE public.conversation (
  id_conversation uuid NOT NULL DEFAULT gen_random_uuid(),
  user1_id uuid,
  user2_id uuid,
  CONSTRAINT conversation_pkey PRIMARY KEY (id_conversation)
);
CREATE TABLE public.fichier (
  id_fichier uuid NOT NULL DEFAULT gen_random_uuid(),
  nom character varying,
  id_proprietaire uuid,
  lien_fichier character varying,
  type character varying,
  date_creation timestamp without time zone,
  CONSTRAINT fichier_pkey PRIMARY KEY (id_fichier)
);
CREATE TABLE public.message (
  id_message uuid NOT NULL DEFAULT gen_random_uuid(),
  contenu text,
  envoyeur_id uuid,
  receveur_id uuid,
  date_msg timestamp without time zone,
  id_conversation uuid,
  CONSTRAINT message_pkey PRIMARY KEY (id_message),
  CONSTRAINT fk_message_envoyeur FOREIGN KEY (envoyeur_id) REFERENCES public.utilisateur(id_utilisateur),
  CONSTRAINT fk_convo FOREIGN KEY (id_conversation) REFERENCES public.conversation(id_conversation),
  CONSTRAINT fk_message_receveur FOREIGN KEY (receveur_id) REFERENCES public.utilisateur(id_utilisateur)
);
CREATE TABLE public.message_fichier (
  id_message uuid NOT NULL,
  id_fichier uuid NOT NULL,
  CONSTRAINT message_fichier_pkey PRIMARY KEY (id_message, id_fichier),
  CONSTRAINT message_fichier_id_fichier_fkey FOREIGN KEY (id_fichier) REFERENCES public.fichier(id_fichier),
  CONSTRAINT fk_mf_message FOREIGN KEY (id_message) REFERENCES public.message(id_message)
);
CREATE TABLE public.publication (
  id_publication integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_utilisateur uuid,
  nom text,
  date_publication timestamp with time zone,
  contenu text,
  likes integer DEFAULT 0,
  dislikes integer DEFAULT 0,
  nb_commentaires integer DEFAULT 0,
  prix numeric,
  article_a_vendre boolean,
  commentaires text,
  CONSTRAINT publication_pkey PRIMARY KEY (id_publication),
  CONSTRAINT fk_publication_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id_utilisateur)
);
CREATE TABLE public.publication_fichier (
  id_fichier uuid NOT NULL,
  id_publication integer NOT NULL,
  CONSTRAINT publication_fichier_pkey PRIMARY KEY (id_fichier, id_publication),
  CONSTRAINT fk_pf_publication FOREIGN KEY (id_publication) REFERENCES public.publication(id_publication),
  CONSTRAINT publication_fichier_id_fichier_fkey FOREIGN KEY (id_fichier) REFERENCES public.fichier(id_fichier)
);
CREATE TABLE public.reaction_publication (
  id_utilisateur uuid NOT NULL DEFAULT gen_random_uuid(),
  id_publication integer NOT NULL,
  type USER-DEFINED DEFAULT 'like'::"reactionType",
  CONSTRAINT reaction_publication_pkey PRIMARY KEY (id_utilisateur, id_publication),
  CONSTRAINT reaction_publication_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id_utilisateur),
  CONSTRAINT reaction_publication_id_publication_fkey FOREIGN KEY (id_publication) REFERENCES public.publication(id_publication)
);
CREATE TABLE public.utilisateur (
  id_utilisateur uuid NOT NULL DEFAULT gen_random_uuid(),
  pseudo text UNIQUE,
  password text,
  date_creation date,
  nom text,
  prenom text,
  email text UNIQUE,
  annee_naissance date,
  main_admin boolean,
  photo_profil character varying,
  deleted_at timestamp with time zone,
  CONSTRAINT utilisateur_pkey PRIMARY KEY (id_utilisateur)
);