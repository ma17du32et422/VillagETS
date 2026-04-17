import { useEffect, useState, useRef } from 'react'
import '../.././assets/Post.css'
import Comments from './Comments'
import { getBaseUrl } from '../../API'
import { useAuth } from '../../AuthContext'

export default function Post({ post, onDelete }) {
  const { user } = useAuth()
  const [likes, setLikes] = useState(post.likes ?? 0)
  const [dislikes, setDislikes] = useState(post.dislikes ?? 0)
  const [liked, setLiked] = useState(post.userReaction === 'like')
  const [disliked, setDisliked] = useState(post.userReaction === 'dislike')
  const [comments, setComments] = useState(post.comments ?? [])
  const [commentVisible, setCommentVisible] = useState(false)
  const [commentText, setCommentText] = useState('')
  const [mediaIndex, setMediaIndex] = useState(0)
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)

  const media = post.media ?? []
  const tags = post.tags ?? []
  const isOwner = user?.userId === post.op?.id

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target))
        setMenuOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const deletePost = async () => {
    setMenuOpen(false)
    try {
      const res = await fetch(`${getBaseUrl()}/post/${post.id}`, {
        method: 'DELETE',
        credentials: 'include',
      })
      if (res.ok) onDelete?.(post.id)
    } catch (err) {
      console.error('Delete failed:', err)
    }
  }


  // I LOVE OPTIMIST UPDATES AHAHAHAHAHAHAHAHAHAHAHE FUCK THIS SHI
const toggleReaction = async (type) => {
  const prevLiked = liked;
  const prevDisliked = disliked;
  const prevLikes = likes;
  const prevDislikes = dislikes;

  // OPTIMISMMMMMMMMM MORE LIKE TISM
  if (type === 'like') {
    if (liked) {
      setLiked(false);
      setLikes(l => Math.max(0, l - 1));
    } else {
      setLiked(true);
      setLikes(l => l + 1);
      if (disliked) { setDisliked(false); setDislikes(d => Math.max(0, d - 1)); }
    }
  } else {
    if (disliked) {
      setDisliked(false);
      setDislikes(d => Math.max(0, d - 1));
    } else {
      setDisliked(true);
      setDislikes(d => d + 1);
      if (liked) { setLiked(false); setLikes(l => Math.max(0, l - 1)); }
    }
  }

  try {
    const res = await fetch(`${getBaseUrl()}/post/${post.id}/react`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type }),
    });

    if (!res.ok) throw new Error('Failed');

    const data = await res.json();
    setLikes(data.likes);
    setDislikes(data.dislikes);
    setLiked(data.userReaction === 'like');
    setDisliked(data.userReaction === 'dislike');
  } catch (err) {
    console.error('Reaction failed:', err);
    // Revert to previous state
    setLiked(prevLiked);
    setDisliked(prevDisliked);
    setLikes(prevLikes);
    setDislikes(prevDislikes);
  }
}

  const toggleComments = () => setCommentVisible(c => !c)

  const addComment = (event) => {
    event.preventDefault()
    const text = commentText.trim()
    if (!text) return
    setComments(c => [...c, { id: Date.now(), text }])
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

        <div id="post-menu-wrap" ref={menuRef}>
          <button id="post-menu-btn" type="button" onClick={() => setMenuOpen(o => !o)}>⋮</button>
          {menuOpen && (
            <div id="post-menu-dropdown">
              {isOwner && (
                <button className="post-menu-item danger" type="button" onClick={deletePost}>
                  Delete post
                </button>
              )}
              <button className="post-menu-item" type="button" onClick={() => setMenuOpen(false)}>
                Cancel
              </button>
            </div>
          )}
        </div>
      </div>

      <h2 id="title">{post.title ?? post.titre}</h2>

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
            <button className="media-arrow media-arrow-left" type="button" onClick={() => setMediaIndex(i => i - 1)}>‹</button>
          )}
          <img id="image" src={media[mediaIndex]} alt={`Post visual ${mediaIndex + 1}`} />
          {media.length > 1 && mediaIndex < media.length - 1 && (
            <button className="media-arrow media-arrow-right" type="button" onClick={() => setMediaIndex(i => i + 1)}>›</button>
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
        <button className={`reaction-button ${liked ? 'active' : ''}`} type="button" onClick={() => toggleReaction('like')}>
          👍 {likes}
        </button>
        <button className={`reaction-button ${disliked ? 'active' : ''}`} type="button" onClick={() => toggleReaction('dislike')}>
          👎 {dislikes}
        </button>
        <button
          className="reaction-button"
          type="button"
          onClick={() => setCommentVisible(v => !v)}
        >
          💬 {post.commentaires ?? 0}
        </button>
      </div>

      {commentVisible && (
        <Comments postId={post.id} initialCount={post.commentaires ?? 0} />
      )}
    </article>
  )
}