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
  const [mediaIndex, setMediaIndex] = useState(0)

  const media = post.media ?? []
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
        <div id="op-info">
          {post.op?.photoProfil
            ? <img id="op-avatar" src={post.op.photoProfil} alt={post.op.pseudo} />
            : <div id="op-avatar-placeholder">{post.op?.pseudo?.[0]?.toUpperCase() ?? 'U'}</div>
          }
          <div id="op-details">
            <p id="op-name">{post.op?.pseudo ?? 'Unknown'}</p>
            <p id="datetime">{post.datetime}</p>
          </div>
        </div>
        <h2 id="title">{post.title ?? post.titre}</h2>
      </div>

      {tags.length > 0 && (
        <div className="post-tags">
          {tags.map((tag) => (
            <span key={tag} className="post-tag">#{tag}</span>
          ))}
        </div>
      )}

      {media.length > 0 && (
        <div id="image-container">
          {media.length > 1 && mediaIndex > 0 && (
            <button
              className="media-arrow media-arrow-left"
              type="button"
              onClick={() => setMediaIndex(i => i - 1)}
            >‹</button>
          )}
          <img id="image" src={media[mediaIndex]} alt={`Post visual ${mediaIndex + 1}`} />
          {media.length > 1 && mediaIndex < media.length - 1 && (
            <button
              className="media-arrow media-arrow-right"
              type="button"
              onClick={() => setMediaIndex(i => i + 1)}
            >›</button>
          )}
          {media.length > 1 && (
            <div className="media-dots">
              {media.map((_, i) => (
                <span key={i} className={`media-dot ${i === mediaIndex ? 'active' : ''}`} />
              ))}
            </div>
          )}
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
