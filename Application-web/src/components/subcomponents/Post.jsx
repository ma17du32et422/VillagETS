import { useState } from 'react'
import '../.././assets/Post.css'

export default function Post({ post }) {
  const [likes, setLikes] = useState(post.likes ?? 0)
  const [dislikes, setDislikes] = useState(post.dislikes ?? 0)
  const [liked, setLiked] = useState(false)
  const [disliked, setDisliked] = useState(false)
  const [comments, setComments] = useState(post.comments ?? [])
  const [commentVisible, setCommentVisible] = useState(false)
  const [commentText, setCommentText] = useState('')
  const tags = post.tags ?? []

  const toggleLike = () => {
    if (liked) {
      setLikes((current) => Math.max(0, current - 1))
      setLiked(false)
      return
    }

    setLikes((current) => current + 1)
    setLiked(true)
    if (disliked) {
      setDislikes((current) => Math.max(0, current - 1))
      setDisliked(false)
    }
  }

  const toggleDislike = () => {
    if (disliked) {
      setDislikes((current) => Math.max(0, current - 1))
      setDisliked(false)
      return
    }

    setDislikes((current) => current + 1)
    setDisliked(true)
    if (liked) {
      setLikes((current) => Math.max(0, current - 1))
      setLiked(false)
    }
  }

  const toggleComments = () => {
    setCommentVisible((current) => !current)
  }

  const addComment = (event) => {
    event.preventDefault()
    const text = commentText.trim()
    if (!text) return

    setComments((current) => [...current, { id: Date.now(), text }])
    setCommentText('')
    setCommentVisible(true)
  }

  return (
    <article className="post">
      <div id="post-header">
        <h2 id="title">{post.title ?? post.titre}</h2>

        <div id="op-info">
          <p id="op-name">{post.op}</p>
          <p id="datetime">{post.datetime}</p>
        </div>
      </div>

      {tags.length > 0 && (
        <div className="post-tags">
          {tags.map((tag) => (
            <span key={tag} className="post-tag">#{tag}</span>
          ))}
        </div>
      )}

      {post.imageUrl && (
        <div id="image-container">
          <img id="image" src={post.imageUrl} alt="Post visual" />
        </div>
      )}

      <p id="contents">{post.contents ?? post.contenu}</p>

      <div className="reaction-bar">
        <button
          className={`reaction-button ${liked ? 'active' : ''}`}
          type="button"
          onClick={toggleLike}
        >
          👍 {likes}
        </button>
        <button
          className={`reaction-button ${disliked ? 'active' : ''}`}
          type="button"
          onClick={toggleDislike}
        >
          👎 {dislikes}
        </button>
        <button className="reaction-button" type="button" onClick={toggleComments}>
          💬 {comments.length}
        </button>
      </div>

      <div className={`comment-section ${commentVisible ? 'visible' : ''}`}>
        <form className="comment-form" onSubmit={addComment}>
          <input
            className="comment-input"
            type="text"
            placeholder="Add a comment"
            value={commentText}
            onChange={(event) => setCommentText(event.target.value)}
          />
          <button className="reaction-button" type="submit">Add</button>
        </form>

        {comments.length > 0 && (
          <div className="comment-list">
            {comments.map((comment) => (
              <p key={comment.id} className="comment-item">{comment.text}</p>
            ))}
          </div>
        )}
      </div>
    </article>
  )
}
