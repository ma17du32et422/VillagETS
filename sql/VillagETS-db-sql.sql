-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.article_a_vendre (
  id_article integer NOT NULL,
  nom character varying,
  description text,
  prix numeric,
  id_publication integer,
  CONSTRAINT article_a_vendre_pkey PRIMARY KEY (id_article),
  CONSTRAINT fk_article_publication FOREIGN KEY (id_publication) REFERENCES public.publication(id_publication)
);
CREATE TABLE public.categorie_publication (
  id_categorie_publication integer NOT NULL,
  nom character varying UNIQUE,
  CONSTRAINT categorie_publication_pkey PRIMARY KEY (id_categorie_publication)
);
CREATE TABLE public.categorie_publication_publication (
  id_categorie_publication integer NOT NULL,
  publicationid_publication integer NOT NULL,
  CONSTRAINT categorie_publication_publication_pkey PRIMARY KEY (id_categorie_publication, publicationid_publication),
  CONSTRAINT fk_cpp_categorie FOREIGN KEY (id_categorie_publication) REFERENCES public.categorie_publication(id_categorie_publication),
  CONSTRAINT fk_cpp_publication FOREIGN KEY (publicationid_publication) REFERENCES public.publication(id_publication)
);
CREATE TABLE public.message (
  id_message integer NOT NULL,
  contenu text,
  envoyeur_id_utilisateur integer,
  CONSTRAINT message_pkey PRIMARY KEY (id_message),
  CONSTRAINT fk_message_envoyeur FOREIGN KEY (envoyeur_id_utilisateur) REFERENCES public.utilisateur(id_utilisateur)
);
CREATE TABLE public.message_piece_jointe (
  id_message integer NOT NULL,
  id_piece_jointe integer NOT NULL,
  CONSTRAINT message_piece_jointe_pkey PRIMARY KEY (id_message, id_piece_jointe),
  CONSTRAINT fk_mpj_message FOREIGN KEY (id_message) REFERENCES public.message(id_message),
  CONSTRAINT fk_mpj_piece FOREIGN KEY (id_piece_jointe) REFERENCES public.piece_jointe(id_piece_jointe)
);
CREATE TABLE public.message_utilisateur (
  messageid_message integer NOT NULL,
  receveur_id_utilisateur integer NOT NULL,
  CONSTRAINT message_utilisateur_pkey PRIMARY KEY (messageid_message, receveur_id_utilisateur),
  CONSTRAINT fk_mu_message FOREIGN KEY (messageid_message) REFERENCES public.message(id_message),
  CONSTRAINT fk_mu_utilisateur FOREIGN KEY (receveur_id_utilisateur) REFERENCES public.utilisateur(id_utilisateur)
);
CREATE TABLE public.piece_jointe (
  id_piece_jointe integer NOT NULL,
  nom character varying UNIQUE,
  lien_fichier character varying,
  type character varying,
  CONSTRAINT piece_jointe_pkey PRIMARY KEY (id_piece_jointe)
);
CREATE TABLE public.publication (
  id_publication integer NOT NULL,
  utilisateurid integer,
  nom character varying,
  contenu character varying,
  CONSTRAINT publication_pkey PRIMARY KEY (id_publication),
  CONSTRAINT fk_publication_utilisateur FOREIGN KEY (utilisateurid) REFERENCES public.utilisateur(id_utilisateur)
);
CREATE TABLE public.publication_piece_jointe (
  id_piece_jointe integer NOT NULL,
  id_publication integer NOT NULL,
  CONSTRAINT publication_piece_jointe_pkey PRIMARY KEY (id_piece_jointe, id_publication),
  CONSTRAINT fk_ppj_piece FOREIGN KEY (id_piece_jointe) REFERENCES public.piece_jointe(id_piece_jointe),
  CONSTRAINT fk_ppj_publication FOREIGN KEY (id_publication) REFERENCES public.publication(id_publication)
);
CREATE TABLE public.utilisateur (
  id_utilisateur integer NOT NULL,
  pseudo character varying UNIQUE,
  password character varying,
  date_creation timestamp without time zone,
  nom character varying,
  prenom character varying,
  email character varying UNIQUE,
  annee_naissance timestamp without time zone,
  main_admin boolean,
  CONSTRAINT utilisateur_pkey PRIMARY KEY (id_utilisateur)
);