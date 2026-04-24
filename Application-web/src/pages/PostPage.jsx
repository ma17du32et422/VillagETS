import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Post from '../components/subcomponents/Post'
import { getBaseUrl } from '../API'
import { mapFeedPost } from '../utils/feed'
import arrowIcon from '../assets/icons/arrow.svg'
import '../assets/PostPage.css'
import usePageTitle from '../utils/usePageTitle'

export default function PostPage() {
  const { postId } = useParams()
  const [post, setPost] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  usePageTitle(post?.title ? `${post.title} | VillagETS` : 'Post')

  useEffect(() => {
    let cancelled = false

    const fetchPost = async () => {
      setLoading(true)
      try {
        const response = await fetch(`${getBaseUrl()}/post/${postId}`, {
          credentials: 'include',
        })

        if (!response.ok) {
          throw new Error(response.status === 404 ? 'Post not found.' : 'Failed to load post.')
        }

        const data = await response.json()
        if (!cancelled) {
          setPost(mapFeedPost(data))
          setError(null)
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message ?? 'Failed to load post.')
          setPost(null)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    if (postId) {
      fetchPost()
    } else {
      setLoading(false)
      setError('Post not found.')
    }

    return () => {
      cancelled = true
    }
  }, [postId])

  return (
    <main className="post-page-main">
      <div className="post-page-shell">
        {loading && <p className="feed-status-message">Loading post...</p>}
        {error && <p className="feed-status-message">Error: {error}</p>}
        {!loading && !error && post && (
          <div className="post-page-row">
            <Link to="/Marketplace" className="post-page-back-link" aria-label="Back to Marketplace">
              <img src={arrowIcon} alt="" className="post-page-back-arrow" aria-hidden="true" />
            </Link>
            <div className="post-page-content">
              <Post post={post} />
            </div>
          </div>
        )}
      </div>
    </main>
  )
}
