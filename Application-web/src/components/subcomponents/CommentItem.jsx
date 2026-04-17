import { useState } from 'react'
import { getBaseUrl } from '../../API'
import { useAuth } from '../../AuthContext'
import '../../assets/CommentItem.css'
export default function CommentItem({ comment, postId, onDeleted }) {
  const { user } = useAuth()
  const [repliesVisible, setRepliesVisible] = useState(false)
  const [replies, setReplies] = useState([])
  const [repliesLoaded, setRepliesLoaded] = useState(false)
  const [replyText, setReplyText] = useState('')
  const [replyFormVisible, setReplyFormVisible] = useState(false)
  const [nbReponses, setNbReponses] = useState(comment.nbReponses ?? 0)
  const [loadingReplies, setLoadingReplies] = useState(false)

  const isOwner = user?.userId === comment.op?.id

  const toggleReplies = async () => {
    if (!repliesLoaded) {
      setLoadingReplies(true)
      try {
        const res = await fetch(`${getBaseUrl()}/comment/${comment.id}/replies`, {
          credentials: 'include',
        })
        if (res.ok) {
          const data = await res.json()
          setReplies(data)
          setRepliesLoaded(true)
        }
      } catch (err) {
        console.error('Failed to load replies:', err)
      } finally {
        setLoadingReplies(false)
      }
    }
    setRepliesVisible(v => !v)
  }

  const submitReply = async (e) => {
    e.preventDefault()
    const text = replyText.trim()
    if (!text) return

    try {
      const res = await fetch(`${getBaseUrl()}/post/${postId}/comment`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ contenu: text, parentCommentaireId: comment.id }),
      })
      if (!res.ok) return

      const newReply = await res.json()
      setReplies(r => [...r, newReply])
      setRepliesLoaded(true)
      setRepliesVisible(true)
      setNbReponses(n => n + 1)
      setReplyText('')
      setReplyFormVisible(false)
    } catch (err) {
      console.error('Failed to post reply:', err)
    }
  }

  const deleteComment = async () => {
    try {
      const res = await fetch(`${getBaseUrl()}/comment/${comment.id}`, {
        method: 'DELETE',
        credentials: 'include',
      })
      if (res.ok) onDeleted?.(comment.id)
    } catch (err) {
      console.error('Failed to delete comment:', err)
    }
  }

  const handleReplyDeleted = (deletedId) => {
    setReplies(r => r.filter(r => r.id !== deletedId))
    setNbReponses(n => Math.max(0, n - 1))
  }

  return (
    <div className="comment-item">
      <div className="comment-avatar">
        {comment.op?.photoProfil
          ? <img src={comment.op.photoProfil} alt={comment.op.pseudo} />
          : <div className="comment-avatar-placeholder">{comment.op?.pseudo?.[0]?.toUpperCase() ?? 'U'}</div>
        }
      </div>

      <div className="comment-body">
        <div className="comment-bubble">
          <span className="comment-pseudo">{comment.op?.pseudo ?? 'Unknown'}</span>
          <p className="comment-text">{comment.contenu}</p>
        </div>

        <div className="comment-actions">
          <span className="comment-date">
            {comment.dateCommentaire ? new Date(comment.dateCommentaire).toLocaleDateString() : ''}
          </span>
          {user && (
            <button
              className="comment-action-btn"
              type="button"
              onClick={() => setReplyFormVisible(v => !v)}
            >
              Reply
            </button>
          )}
          {isOwner && (
            <button
              className="comment-action-btn danger"
              type="button"
              onClick={deleteComment}
            >
              Delete
            </button>
          )}
          {nbReponses > 0 && (
            <button
              className="comment-action-btn replies-toggle"
              type="button"
              onClick={toggleReplies}
            >
              {loadingReplies
                ? 'Loading...'
                : repliesVisible
                  ? `▲ Hide replies`
                  : `▼ ${nbReponses} ${nbReponses === 1 ? 'reply' : 'replies'}`
              }
            </button>
          )}
        </div>

        {replyFormVisible && (
          <form className="reply-form" onSubmit={submitReply}>
            <input
              className="comment-input"
              type="text"
              placeholder={`Reply to ${comment.op?.pseudo ?? 'user'}...`}
              value={replyText}
              onChange={e => setReplyText(e.target.value)}
              autoFocus
            />
            <button className="reaction-button" type="submit">Send</button>
            <button
              className="reaction-button"
              type="button"
              onClick={() => { setReplyFormVisible(false); setReplyText('') }}
            >
              Cancel
            </button>
          </form>
        )}

        {repliesVisible && replies.length > 0 && (
          <div className="replies-list">
            {replies.map(reply => (
              <CommentItem
                key={reply.id}
                comment={reply}
                postId={postId}
                onDeleted={handleReplyDeleted}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}