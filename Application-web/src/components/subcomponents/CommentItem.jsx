import { useState } from 'react'
import { getBaseUrl } from '../../API'
import { useAuth } from '../../AuthContext'
import ProfileAvatar from '../ProfileAvatar'
import '../../assets/CommentItem.css'

const imageAttachmentPattern = /\.(png|jpe?g|gif|webp)(?:$|[?#])/i

function isImageAttachment(url) {
  return imageAttachmentPattern.test(url)
}

function getAttachmentLabel(url) {
  try {
    const pathname = new URL(url).pathname
    return decodeURIComponent(pathname.split('/').pop() || 'Attachment')
  } catch {
    return 'Attachment'
  }
}

export default function CommentItem({ comment, postId, onDeleted, onCountChange, depth = 0 }) {
  const { user } = useAuth()
  const [repliesVisible, setRepliesVisible] = useState(false)
  const [replies, setReplies] = useState([])
  const [repliesLoaded, setRepliesLoaded] = useState(false)
  const [replyText, setReplyText] = useState('')
  const [replyFormVisible, setReplyFormVisible] = useState(false)
  const [nbReponses, setNbReponses] = useState(comment.nbReponses ?? 0)
  const [loadingReplies, setLoadingReplies] = useState(false)
  const [isSubmittingReply, setIsSubmittingReply] = useState(false)

  const canDelete = user?.userId === comment.op?.id || user?.mainAdmin === true
  const canReply = user && depth === 0
  const canShowReplies = depth === 0 && nbReponses > 0

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
    if (isSubmittingReply) return

    const text = replyText.trim()
    if (!text) return

    try {
      setIsSubmittingReply(true)
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
      onCountChange?.(1)
      setReplyText('')
      setReplyFormVisible(false)
    } catch (err) {
      console.error('Failed to post reply:', err)
    } finally {
      setIsSubmittingReply(false)
    }
  }

  const deleteComment = async () => {
    try {
      const res = await fetch(`${getBaseUrl()}/comment/${comment.id}`, {
        method: 'DELETE',
        credentials: 'include',
      })
      if (res.ok) {
        let deletedCount = 1
        try {
          const data = await res.json()
          deletedCount = Math.max(1, Number(data?.deletedCount) || 1)
        } catch {
          deletedCount = 1
        }

        onDeleted?.(comment.id, deletedCount)
      }
    } catch (err) {
      console.error('Failed to delete comment:', err)
    }
  }

  const handleReplyDeleted = (deletedId, deletedCount = 1) => {
    setReplies(r => r.filter(r => r.id !== deletedId))
    setNbReponses(n => Math.max(0, n - 1))
    onCountChange?.(-deletedCount)
  }

  return (
    <div className="comment-item">
      <div className="comment-avatar">
        <ProfileAvatar
          user={comment.op}
          imageProps={{}}
          placeholderProps={{ className: 'comment-avatar-placeholder' }}
        />
      </div>

      <div className="comment-body">
        <div className="comment-bubble">
          <span className="comment-pseudo">{comment.op?.pseudo ?? 'Unknown'}</span>
          {comment.contenu && !isImageAttachment(comment.contenu) && (
            <p className="comment-text">{comment.contenu}</p>
          )}
          {(comment.media ?? []).length > 0 && (
            <div className="comment-attachments">
              {comment.media.map((url) => (
                isImageAttachment(url) ? (
                  <a key={url} href={url} target="_blank" rel="noreferrer" className="comment-image-link">
                    <img src={url} alt={getAttachmentLabel(url)} className="comment-image" />
                  </a>
                ) : (
                  <a key={url} href={url} target="_blank" rel="noreferrer" className="comment-file-link">
                    {getAttachmentLabel(url)}
                  </a>
                )
              ))}
            </div>
          )}
          {comment.contenu && isImageAttachment(comment.contenu) && (
            <div className="comment-attachments">
              <a href={comment.contenu} target="_blank" rel="noreferrer" className="comment-image-link">
                <img src={comment.contenu} alt={getAttachmentLabel(comment.contenu)} className="comment-image" />
              </a>
            </div>
          )}
        </div>

        <div className="comment-actions">
          <span className="comment-date">
            {comment.dateCommentaire ? new Date(comment.dateCommentaire).toLocaleDateString() : ''}
          </span>
          {canReply && (
            <button
              className="comment-action-btn"
              type="button"
              onClick={() => setReplyFormVisible(v => !v)}
            >
              Reply
            </button>
          )}
          {canDelete && (
            <button
              className="comment-action-btn danger"
              type="button"
              onClick={deleteComment}
            >
              Delete
            </button>
          )}
          {canShowReplies && (
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

        {canReply && replyFormVisible && (
          <form className="reply-form" onSubmit={submitReply}>
            <input
              className="comment-input"
              type="text"
              placeholder={`Reply to ${comment.op?.pseudo ?? 'user'}...`}
              value={replyText}
              disabled={isSubmittingReply}
              onChange={e => setReplyText(e.target.value)}
              autoFocus
            />
            <button className="reaction-button comment-post" type="submit" disabled={isSubmittingReply}>
              {isSubmittingReply ? 'Sending...' : 'Send'}
            </button>
            <button
              className="reaction-button"
              type="button"
              disabled={isSubmittingReply}
              onClick={() => { setReplyFormVisible(false); setReplyText('') }}
            >
              Cancel
            </button>
          </form>
        )}

        {canShowReplies && repliesVisible && replies.length > 0 && (
          <div className="replies-list">
            {replies.map(reply => (
              <CommentItem
                key={reply.id}
                comment={reply}
                postId={postId}
                onDeleted={handleReplyDeleted}
                onCountChange={onCountChange}
                depth={depth + 1}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
