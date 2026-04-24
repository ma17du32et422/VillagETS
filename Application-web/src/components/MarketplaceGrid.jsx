import { Fragment, useEffect, useRef } from 'react'
import MarketplaceCard from './subcomponents/MarketplaceCard'

export default function MarketplaceGrid({ posts, loading, error, onLoadMore, hasMore = false, loadingMore = false }) {
  const triggerRef = useRef(null)

  useEffect(() => {
    if (!onLoadMore || !hasMore || loadingMore || loading) return

    const triggerNode = triggerRef.current
    if (!triggerNode) return
    const scrollRoot = triggerNode.closest('.home-flux-container')

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          onLoadMore()
        }
      },
      {
        root: scrollRoot,
        rootMargin: '0px 0px 900px 0px',
      }
    )

    observer.observe(triggerNode)
    return () => observer.disconnect()
  }, [hasMore, loading, loadingMore, onLoadMore, posts])

  if (loading) return <p className="feed-status-message">Loading posts...</p>
  if (error) return <p className="feed-status-message">Error: {error}</p>
  if (!posts.length) return <p className="feed-status-message">No marketplace posts found.</p>

  return (
    <>
      <div className="marketplace-grid">
        {posts.map((post, index) => (
          <Fragment key={post.id}>
            <div className="marketplace-grid-item">
              <span className="feed-debug-badge">#{index + 1}</span>
              <MarketplaceCard post={post} />
            </div>
          </Fragment>
        ))}
      </div>
      {hasMore && <div ref={triggerRef} className="feed-load-trigger" aria-hidden="true" />}
      {loadingMore && <p className="feed-status-message">Loading more...</p>}
    </>
  )
}
