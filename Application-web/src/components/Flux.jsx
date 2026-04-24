import { Fragment, useEffect, useRef, useState } from 'react';
import Post from './subcomponents/Post';

export default function Flux({ posts: propPosts, loading, error, onLoadMore, hasMore = false, loadingMore = false }) {
  const [posts, setPosts] = useState(propPosts ?? []);
  const triggerRef = useRef(null)

  useEffect(() => setPosts(propPosts ?? []), [propPosts]);

  const handleDelete = (id) => setPosts(p => p.filter(post => post.id !== id));

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

  if (loading) return <p className="feed-status-message">Loading posts...</p>;
  if (error) return <p className="feed-status-message">Error: {error}</p>;

  return (
    <div id="feed">
      {posts.map((post, index) => (
        <Fragment key={post.id}>
          <div className="feed-item">
            <span className="feed-debug-badge">#{index + 1}</span>
            <Post post={post} onDelete={handleDelete} mediaMode="feed" />
          </div>
        </Fragment>
      ))}
      {hasMore && <div ref={triggerRef} className="feed-load-trigger" aria-hidden="true" />}
      {loadingMore && <p className="feed-status-message">Loading more...</p>}
    </div>
  );
}
