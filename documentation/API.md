# VillagETS API — Final Route Specs

## Auth

### `POST /auth/signup`
**Auth required:** No  
**Sets token cookie:** Yes

**Input**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "pseudo": "Guarded",
  "nom": "Doe",
  "prenom": "John",
  "photoProfil": "https://.../avatar.jpg"
}
```

**Output**
```json
{
  "userId": "uuid"
}
```

---

### `POST /auth/login`
**Auth required:** No  
**Sets token cookie:** Yes

**Input**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Output**
```json
{
  "userId": "uuid"
}
```

---

### `POST /auth/logout`
**Auth required:** No  
**Sets token cookie:** No

**Input:** none

**Output:** `200 OK`

---

## Current User

### `GET /me`
**Auth required:** Yes

**Input:** none

**Output**
```json
{
  "userId": "uuid",
  "pseudo": "Guarded",
  "nom": "Doe",
  "prenom": "John",
  "email": "user@example.com",
  "photoProfil": "https://.../avatar.jpg",
  "mainAdmin": false
}
```

---

## Users

### `GET /user/{id}`
**Auth required:** No

**Output**
```json
{
  "userId": "uuid",
  "pseudo": "Guarded",
  "nom": "Doe",
  "prenom": "John",
  "photoProfil": "https://.../avatar.jpg"
}
```

---

### `GET /user/search?query=...`
**Auth required:** No

**Output**
```json
[
  {
    "id": "uuid",
    "pseudo": "Guarded",
    "nom": "Doe",
    "prenom": "John",
    "photoProfil": "https://.../avatar.jpg",
    "dateCreation": "2026-04-01T00:00:00"
  }
]
```

---

### `PATCH /user/pseudo`
**Auth required:** Yes

**Input**
```json
{
  "pseudo": "NewPseudo"
}
```

**Output:** `200 OK`

---

### `PATCH /user/password`
**Auth required:** Yes

**Input**
```json
{
  "currentPassword": "oldpass",
  "newPassword": "newpass123"
}
```

**Output:** `200 OK`

---

### `PATCH /user/email`
**Auth required:** Yes

**Input**
```json
{
  "email": "new@example.com"
}
```

**Output:** `200 OK`

---

### `PATCH /user/photo`
**Auth required:** Yes

**Input**
```json
{
  "photoUrl": "https://.../uploads/file.jpg"
}
```

**Output:** `200 OK`

---

## Posts

### Post object
```json
{
  "id": 1005,
  "titre": "Post title",
  "contenu": "Post text content",
  "media": ["https://.../image1.jpg"],
  "datePublication": "2026-01-15T14:32:00Z",
  "prix": null,
  "articleAVendre": false,
  "likes": 12,
  "dislikes": 2,
  "commentaires": 5,
  "userReaction": "like",
  "op": {
    "id": "uuid",
    "pseudo": "Guarded",
    "photoProfil": "https://.../avatar.jpg"
  }
}
```

---

### `GET /post/{id}`
**Auth required:** No

**Output**
```json
{
  "id": 1005,
  "titre": "Post title",
  "contenu": "Post text content",
  "media": ["https://.../image1.jpg"],
  "datePublication": "2026-01-15T14:32:00Z",
  "prix": null,
  "articleAVendre": false,
  "likes": 12,
  "dislikes": 2,
  "commentaires": 5,
  "op": {
    "id": "uuid",
    "pseudo": "Guarded",
    "photoProfil": "https://.../avatar.jpg"
  }
}
```

---

### `POST /post`
**Auth required:** Yes

**Input**
```json
{
  "nom": "Post title",
  "contenu": "Post text content",
  "media": ["https://.../uploaded-file.jpg"],
  "prix": null,
  "articleAVendre": false
}
```

**Output**
```json
{
  "id": 1005,
  "titre": "Post title",
  "contenu": "Post text content",
  "media": ["https://.../uploaded-file.jpg"],
  "utilisateurId": "uuid",
  "datePublication": "2026-04-20T19:00:00Z",
  "prix": null,
  "articleAVendre": false
}
```

---

### `DELETE /post/{id}`
**Auth required:** Yes

**Output**
- `200 OK`
- `403 Forbid`
- `404 NotFound`

---

### `POST /feed`
**Auth required:** No  
**Note:** auth cookie is optional for `userReaction`

**Input**
```json
{
  "searchString": "bike",
  "tags": null,
  "isMarketplace": false
}
```

**Output**
```json
[
  {
    "id": 1005,
    "titre": "Post title",
    "contenu": "Post text content",
    "media": ["https://.../image1.jpg"],
    "datePublication": "2026-01-15T14:32:00Z",
    "prix": null,
    "articleAVendre": false,
    "likes": 12,
    "dislikes": 2,
    "commentaires": 5,
    "userReaction": "like",
    "op": {
      "id": "uuid",
      "pseudo": "Guarded",
      "photoProfil": "https://.../avatar.jpg"
    }
  }
]
```

---

### `GET /user/{id}/posts`
**Auth required:** No  
**Note:** auth cookie is optional for `userReaction`

**Output:** array of post objects

---

## Reactions

### `POST /post/{id}/react`
**Auth required:** Yes

**Input**
```json
{
  "type": "like"
}
```

**Output**
```json
{
  "likes": 12,
  "dislikes": 2,
  "userReaction": "like"
}
```

---

### `GET /post/{id}/react`
**Auth required:** Yes

**Output**
```json
{
  "userReaction": "like"
}
```

---

## Comments

### `GET /post/{publicationId}/comments`
**Auth required:** No

**Output**
```json
[
  {
    "id": "uuid",
    "publicationId": 1005,
    "parentCommentaire": null,
    "dateCommentaire": "2026-04-20T19:00:00Z",
    "contenu": "Nice post",
    "nbReponses": 2,
    "op": {
      "id": "uuid",
      "pseudo": "Guarded",
      "photoProfil": "https://.../avatar.jpg"
    }
  }
]
```

---

### `GET /comment/{commentId}/replies`
**Auth required:** No

**Output:** array of comment objects

---

### `POST /post/{publicationId}/comment`
**Auth required:** Yes

**Input**
```json
{
  "contenu": "Nice post",
  "parentCommentaireId": null
}
```

**Output:** comment object

---

### `DELETE /comment/{commentId}`
**Auth required:** Yes

**Output**
```json
{
  "deletedCount": 1
}
```

---

## Uploads

### `POST /upload`
**Auth required:** Yes  
**Content-Type:** `multipart/form-data`

**Fields**
- `file`
- `nom`
- `type`
- `scope` optional, use `"message"` for DM file uploads

**Output**
```json
{
  "url": "https://.../uploads/file.jpg"
}
```

---

## Messaging

### `WS /ws/chat`
**Auth required:** Yes

**Send**
```json
{
  "receiverId": "target-user-uuid",
  "contenu": "Hello",
  "media": ["https://.../uploads/file.jpg"]
}
```

**Receive**
```json
{
  "id": "message-uuid",
  "conversationId": "conversation-uuid",
  "envoyeurId": "sender-uuid",
  "receveurId": "receiver-uuid",
  "contenu": "Hello",
  "media": ["https://.../uploads/file.jpg"],
  "dateMsg": "2026-04-20T19:00:00Z"
}
```

---

### `GET /chat/history/{targetUserId}`
**Auth required:** Yes

**Output**
```json
[
  {
    "id": "message-uuid",
    "conversationId": "conversation-uuid",
    "envoyeurId": "sender-uuid",
    "receveurId": "receiver-uuid",
    "contenu": "Hi",
    "media": [],
    "dateMsg": "2026-04-17T17:00:00Z"
  }
]
```

---

### `GET /chat/conversations`
**Auth required:** Yes

**Output**
```json
[
  {
    "conversationId": "conversation-uuid",
    "otherUser": {
      "id": "uuid",
      "pseudo": "John",
      "nom": "Doe",
      "prenom": "John",
      "photoProfil": "https://.../avatar.jpg",
      "dateCreation": "2026-04-01T00:00:00"
    }
  }
]
```