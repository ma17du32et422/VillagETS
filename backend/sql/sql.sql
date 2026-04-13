/**DROP TABLE IF EXISTS Message_Fichier CASCADE;
DROP TABLE IF EXISTS Message_Utilisateur CASCADE;
DROP TABLE IF EXISTS Message CASCADE;

DROP TABLE IF EXISTS Commentaire_Fichier CASCADE;
DROP TABLE IF EXISTS Publication_Fichier CASCADE;

DROP TABLE IF EXISTS Cat_Pub_Publication CASCADE;
DROP TABLE IF EXISTS Categorie_Publication CASCADE;

DROP TABLE IF EXISTS Commentaire CASCADE;
DROP TABLE IF EXISTS Publication CASCADE;

--DROP TABLE IF EXISTS Fichier CASCADE;
--DROP TABLE IF EXISTS Utilisateur CASCADE;
**/

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE Utilisateur (
    id_utilisateur UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pseudo VARCHAR(16) UNIQUE,
    password VARCHAR(40),
    date_creation DATE,
    nom VARCHAR(30),
    prenom VARCHAR(30),
    email VARCHAR(50) UNIQUE,
    annee_naissance DATE,
    main_admin BOOLEAN,
    photo_profil VARCHAR(255)
);

CREATE TABLE Publication (
    id_publication INTEGER GENERATED ALWAYS AS IDENTITY 
                   (START WITH 1000 INCREMENT BY 1) PRIMARY KEY,
    id_utilisateur UUID,
    nom VARCHAR(50),
    date_publication TIMESTAMP,
    contenu VARCHAR(255),
    likes INTEGER DEFAULT 0,
    dislikes INTEGER DEFAULT 0,
    prix DECIMAL(8,2),
    article_a_vendre BOOLEAN,
    CONSTRAINT fk_publication_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE Commentaire (
    id_commentaire UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Utilisateurid UUID,
	Publicationid INTEGER,
	Parent_commentaire UUID,
    date_commentaire TIMESTAMP,
    contenu VARCHAR(255),
	likes INTEGER DEFAULT 0,
	dislikes INTEGER DEFAULT 0,
    CONSTRAINT fk_commentaire_utilisateur
        FOREIGN KEY (Utilisateurid)
        REFERENCES Utilisateur(id_utilisateur),
	CONSTRAINT fk_commentaire_publication
        FOREIGN KEY (Publicationid)
        REFERENCES Publication(id_publication),
	CONSTRAINT fk_commentaire_parent
        FOREIGN KEY (Parent_commentaire)
        REFERENCES Commentaire(id_commentaire)
);

CREATE TABLE Categorie_Publication (
    id_categorie_publication UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(20) UNIQUE,
	categorie_base BOOLEAN DEFAULT FALSE
);

CREATE TABLE Cat_Pub_Publication (
    id_categorie_publication UUID,
    publicationid_publication INTEGER,
    PRIMARY KEY (id_categorie_publication, publicationid_publication),
    CONSTRAINT fk_cpp_categorie
        FOREIGN KEY (id_categorie_publication)
        REFERENCES Categorie_Publication(id_categorie_publication),
    CONSTRAINT fk_cpp_publication
        FOREIGN KEY (publicationid_publication)
        REFERENCES Publication(id_publication)
);

CREATE TABLE Fichier (
    id_fichier UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(255),
    id_proprietaire UUID,
    lien_fichier VARCHAR(255),
    type VARCHAR(10)
);

CREATE TABLE Publication_Fichier (
    id_fichier UUID,
    id_publication INTEGER,
    PRIMARY KEY (id_fichier, id_publication),
    CONSTRAINT fk_pf_fichier
        FOREIGN KEY (id_fichier)
        REFERENCES Fichier(id_fichier),
    CONSTRAINT fk_pf_publication
        FOREIGN KEY (id_publication)
        REFERENCES Publication(id_publication)
);

CREATE TABLE Commentaire_Fichier (
    id_fichier UUID,
    id_commentaire UUID,
    PRIMARY KEY (id_fichier, id_commentaire),
    CONSTRAINT fk_cf_fichier
        FOREIGN KEY (id_fichier)
        REFERENCES Fichier(id_fichier),
    CONSTRAINT fk_cf_commentaire
        FOREIGN KEY (id_commentaire)
        REFERENCES Commentaire(id_commentaire)
);

CREATE TABLE Conversation (
    id_conversation UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user1_id UUID,
    user2_id UUID,
    nom_conversation VARCHAR(50),
    description_conversation VARCHAR(255)
);

CREATE TABLE Message (
    id_message UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contenu TEXT,
    envoyeur_id UUID,
    receveur_id UUID,
    date_msg TIMESTAMP, 
    id_conversation UUID, 
    CONSTRAINT fk_message_envoyeur
        FOREIGN KEY (envoyeur_id)
        REFERENCES Utilisateur(id_utilisateur),
    CONSTRAINT fk_message_receveur
        FOREIGN KEY (envoyeur_id)
        REFERENCES Utilisateur(id_utilisateur),
    CONSTRAINT fk_convo
        FOREIGN KEY (id_conversation)
        REFERENCES Conversation(id_conversation)
);



CREATE TABLE Message_Fichier (
    id_message UUID,
    id_fichier UUID,
    PRIMARY KEY (id_message, id_fichier),
    CONSTRAINT fk_mf_message
        FOREIGN KEY (id_message)
        REFERENCES Message(id_message),
    CONSTRAINT fk_mf_fichier
        FOREIGN KEY (id_fichier)
        REFERENCES Fichier(id_fichier)
);

/**CREATE TABLE Message_Utilisateur (
    Messageid_message UUID,
    receveur_id_utilisateur UUID,
    PRIMARY KEY (Messageid_message, receveur_id_utilisateur),
    CONSTRAINT fk_mu_message
        FOREIGN KEY (Messageid_message)
        REFERENCES Message(id_message),
    CONSTRAINT fk_mu_utilisateur
        FOREIGN KEY (receveur_id_utilisateur)
        REFERENCES Utilisateur(id_utilisateur)
);*/



-- Valeurs de test
INSERT INTO Utilisateur (
    pseudo,
    password,
    date_creation,
    nom,
    prenom,
    email,
    annee_naissance,
    main_admin
) VALUES (
    'PSEUDO',
    'mdp',
    '1900-01-01',
    'NOM',
    'PRENOM',
    'email',
    '2000-01-01',
    false
);
INSERT INTO Categorie_Publication (nom, categorie_base) VALUES ('Marketplace', TRUE);
INSERT INTO Fichier (nom, lien_fichier, type) VALUES ('LegaultLeBoss', 'https://apivillagets.lesageserveur.com/uploads/90c9dce0-039c-408d-b8fd-d0956bf46814.jpg', 'Image');
