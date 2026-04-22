package com.example.villagets_androidstudio.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
    private String id;
    private String contenu;
    private String dateCommentaire;
    private User op;
    private String parentCommentaireId;
    private List<Comment> replies;
    private int replyCount;
    private boolean isExpanded = false;

    public Comment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(String dateCommentaire) { this.dateCommentaire = dateCommentaire; }

    public User getOp() { return op; }
    public void setOp(User op) { this.op = op; }

    public String getParentCommentaireId() { return parentCommentaireId; }
    public void setParentCommentaireId(String parentCommentaireId) { this.parentCommentaireId = parentCommentaireId; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
