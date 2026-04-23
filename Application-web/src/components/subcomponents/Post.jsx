import { useEffect, useState, useRef } from 'react'
import '../.././assets/Post.css'
import Comments from './Comments'
import { getBaseUrl } from '../../API'
import { useAuth } from '../../AuthContext'
import ProfileAvatar from '../ProfileAvatar'
import likeIcon from '../../assets/icons/like.svg'
import dislikeIcon from '../../assets/icons/dislike.svg'
import commentIcon from '../../assets/icons/comment.svg'

const formatPostDate = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    if (diffMins < 1) return 'Just now'
    if (diffMins < 60) return `${diffMins}m ago`
    if (diffHours < 24) return `${diffHours}h ago`
    if (diffDays < 7) return `${diffDays}d ago`
    
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined })
  } catch {
    return dateString
  }
}

export default function Post({ post, onDelete }) {
  const { user } = useAuth()
  const [likes, setLikes] = useState(post.likes ?? 0)
  const [dislikes, setDislikes] = useState(post.dislikes ?? 0)
  const [commentCount, setCommentCount] = useState(post.commentaires ?? 0)
  const [liked, setLiked] = useState(post.userReaction === 'like')
  const [disliked, setDisliked] = useState(post.userReaction === 'dislike')
  const [comments, setComments] = useState(post.comments ?? [])
  const [commentVisible, setCommentVisible] = useState(false)
  const [commentText, setCommentText] = useState('')
  const [mediaIndex, setMediaIndex] = useState(0)
  const [mediaRatios, setMediaRatios] = useState({})
  const [menuOpen, setMenuOpen] = useState(false)
  const [failedImages, setFailedImages] = useState(new Set())
  const menuRef = useRef(null)

  const rawMedia = post.media ?? []
  const media = rawMedia.filter(url => url && typeof url === 'string' && url.trim().length > 0)
  const validMedia = media.filter(url => !failedImages.has(url))
  const currentMediaUrl = validMedia[mediaIndex] ?? ''
  const currentMediaRatio = mediaRatios[currentMediaUrl]
  const tags = post.tags ?? []
  const canDelete = user?.userId === post.op?.id || user?.mainAdmin === true
  const isMarketplaceItem = post.articleAVendre === true
  const parsedPrice = post.prix == null ? null : Number(post.prix)
  const displayPrice = parsedPrice != null && !Number.isNaN(parsedPrice)
    ? new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(parsedPrice)
    : null

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target))
        setMenuOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    setCommentCount(post.commentaires ?? 0)
  }, [post.commentaires])

  useEffect(() => {
    if (mediaIndex >= validMedia.length) {
      setMediaIndex(0)
    }
  }, [validMedia.length])

  const handleImageError = (url) => {
    setFailedImages(prev => new Set(prev).add(url))
  }

  const handleImageLoad = (url, event) => {
    const { naturalWidth, naturalHeight } = event.currentTarget
    if (!naturalWidth || !naturalHeight) return

    const ratio = naturalWidth / naturalHeight
    setMediaRatios((prev) => {
      if (prev[url] === ratio) return prev
      return { ...prev, [url]: ratio }
    })
  }

  const getForegroundStyle = (ratio) => {
    if (!ratio) {
      return { width: '100%', height: '100%' }
    }

    if (ratio >= 1) {
      return {
        width: '100%',
        aspectRatio: `${ratio}`,
      }
    }

    return {
      height: '100%',
      aspectRatio: `${ratio}`,
    }
  }

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
    <div className="post-shell">
      <article className="post">
      <div id="post-header">
        <div id="op-info">
          <ProfileAvatar
            user={post.op}
            imageProps={{ id: 'op-avatar' }}
            placeholderProps={{ id: 'op-avatar-placeholder' }}
          />
          <div id="op-details">
            <p id="op-name">{post.op?.pseudo ?? 'Unknown'}</p>
            <p id="datetime">{formatPostDate(post.datetime)}</p>
          </div>
        </div>

        <div id="post-menu-wrap" ref={menuRef}>
          <button id="post-menu-btn" type="button" onClick={() => setMenuOpen(o => !o)}>⋮</button>
          {menuOpen && (
            <div id="post-menu-dropdown">
              {canDelete && (
                <button className="post-menu-item danger" type="button" onClick={deletePost}>
                  Delete post
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      <h2 id="title">{post.title ?? post.titre}</h2>

      {isMarketplaceItem && displayPrice && (
        <p id="post-price">{displayPrice}</p>
      )}

      {tags.length > 0 && (
        <div className="post-tags">
          {tags.map((tag) => (
            <span key={tag} className="post-tag">#{tag}</span>
          ))}
        </div>
      )}

      {media.length > 0 && (
        <div id="image-container">
          {validMedia.length > 1 && mediaIndex > 0 && (
            <button className="media-arrow media-arrow-left" type="button" onClick={() => setMediaIndex(i => i - 1)}>‹</button>
          )}
          {validMedia.length > 0 ? (
            <>
              <div
                className="image-backdrop"
                aria-hidden="true"
                style={{ backgroundImage: `url("${currentMediaUrl}")` }}
              />
              <div className="image-foreground" style={getForegroundStyle(currentMediaRatio)}>
                <img 
                  id="image" 
                  src={currentMediaUrl} 
                  alt={`Post visual ${mediaIndex + 1}`}
                  onLoad={(event) => handleImageLoad(currentMediaUrl, event)}
                  onError={() => handleImageError(currentMediaUrl)}
                />
              </div>
              {validMedia.length > 1 && mediaIndex < validMedia.length - 1 && (
                <button className="media-arrow media-arrow-right" type="button" onClick={() => setMediaIndex(i => i + 1)}>›</button>
              )}
              {validMedia.length > 1 && (
                <div className="media-dots">
                  {validMedia.map((_, i) => (
                    <span key={i} className={`media-dot ${i === mediaIndex ? 'active' : ''}`} />
                  ))}
                </div>
              )}
            </>
          ) : (
            <div className="image-unavailable">Media unavailable</div>
          )}
        </div>
      )}

      <p id="contents">{post.contents ?? post.contenu}</p>

      <div className="reaction-bar">
        <button className={`reaction-button ${liked ? 'active' : ''}`} type="button" onClick={() => toggleReaction('like')}>
          <img src={likeIcon} alt="Like" className="reaction-icon" />
          {likes}
        </button>
        <button className={`reaction-button ${disliked ? 'active' : ''}`} type="button" onClick={() => toggleReaction('dislike')}>
          <img src={dislikeIcon} alt="Dislike" className="reaction-icon" />
          {dislikes}
        </button>
        <button
          className={`reaction-button ${commentVisible ? 'active' : ''}`}
          type="button"
          onClick={() => setCommentVisible(v => !v)}
        >
          <img src={commentIcon} alt="Comment" className="reaction-icon" />
          {commentCount}
        </button>
      </div>

      {commentVisible && (
        <Comments
          postId={post.id}
          initialCount={commentCount}
          onCountChange={(delta) => setCommentCount((current) => Math.max(0, current + delta))}
        />
      )}
      </article>
    </div>
  )
}
