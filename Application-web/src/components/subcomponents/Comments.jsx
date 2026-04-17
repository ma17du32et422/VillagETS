import { useState, useEffect } from 'react'
import { getBaseUrl } from '../../API'
import { useAuth } from '../../AuthContext'
import CommentItem from './CommentItem'
import ProfileAvatar from '../ProfileAvatar'
import '../../assets/Comments.css'
export default function Comments({ postId, initialCount }) {
  const { user } = useAuth()
  const [comments, setComments] = useState([])
  const [commentText, setCommentText] = useState('')
  const [count, setCount] = useState(initialCount ?? 0)
  const [loaded, setLoaded] = useState(false)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchComments()
  }, [postId])

  const fetchComments = async () => {
    setLoading(true)
    try {
      const res = await fetch(`${getBaseUrl()}/post/${postId}/comments`, {
        credentials: 'include',
      })
      if (res.ok) {
        const data = await res.json()
        setComments(data)
        setLoaded(true)
      }
    } catch (err) {
      console.error('Failed to fetch comments:', err)
    } finally {
      setLoading(false)
    }
  }

  const submitComment = async (e) => {
    e.preventDefault()
    const text = commentText.trim()
    if (!text) return

    try {
      const res = await fetch(`${getBaseUrl()}/post/${postId}/comment`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ contenu: text, parentCommentaireId: null }),
      })
      if (!res.ok) return

      const newComment = await res.json()
      setComments(c => [newComment, ...c])
      setCount(n => n + 1)
      setCommentText('')
    } catch (err) {
      console.error('Failed to post comment:', err)
    }
  }

  const handleDeleted = (deletedId) => {
    setComments(c => c.filter(c => c.id !== deletedId))
    setCount(n => Math.max(0, n - 1))
  }

  return (
    <div className="comments-section">
      {user && (
        <form className="comment-form" onSubmit={submitComment}>
          <div className="comment-input-avatar">
            <ProfileAvatar
              user={{ ...user, id: user.userId }}
              imageProps={{}}
              placeholderProps={{ className: 'comment-avatar-placeholder' }}
            />
          </div>
          <input
            className="comment-input"
            type="text"
            placeholder="Write a comment..."
            value={commentText}
            onChange={e => setCommentText(e.target.value)}
          />
          <button className="reaction-button" type="submit">Post</button>
        </form>
      )}

      {loading && <p className="comments-loading">Loading comments...</p>}

      {loaded && comments.length === 0 && (
        <p className="comments-empty">No comments yet. Be the first!</p>
      )}

      <div className="comments-list">
        {comments.map(comment => (
          <CommentItem
            key={comment.id}
            comment={comment}
            postId={postId}
            onDeleted={handleDeleted}
          />
        ))}
      </div>
    </div>
  )
}
