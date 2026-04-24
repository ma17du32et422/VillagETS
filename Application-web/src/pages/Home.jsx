import { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import Flux from "../components/Flux";
import FeedControls from '../components/FeedControls';
import {
  DEFAULT_FEED_SORT_MODE,
  FEED_PAGE_SIZE,
  mergeFeedPosts,
  requestFeed,
  sortFeedPosts,
} from '../utils/feed';

import '../assets/App.css'
import usePageTitle from "../utils/usePageTitle";

function App(){
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [sortMode, setSortMode] = useState(DEFAULT_FEED_SORT_MODE);
  const [pageIndex, setPageIndex] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  usePageTitle("Votre fil d'actualités");

  useEffect(() => {
    if (authLoading) return;

    //if (!user) {
    //  setPosts([]);
    //  setError('You must be logged in to view posts.');
    //  setLoading(false);
    //  return;
    //}

    const fetchPosts = async () => {
      setLoading(true);
      try {
        const data = await requestFeed({
          isMarketplace: false,
          pageIndex: 0,
          sortMode,
        });
        setPosts(sortFeedPosts(data, sortMode));
        setPageIndex(0);
        setHasMore(data.length >= FEED_PAGE_SIZE);
        setError(null);
      } catch (err) {
        console.error('Error fetching posts:', err);
        setError(err.message ?? 'Failed to fetch posts');
        setHasMore(false);
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [authLoading, sortMode, user]);

  const handlePostCreated = (newPost) => {
    setPosts((prev) => [newPost, ...prev]);
  };

  const handleLoadMore = async () => {
    if (loading || loadingMore || !hasMore) return;

    const nextPageIndex = pageIndex + 1;
    setLoadingMore(true);
    try {
      const data = await requestFeed({
        isMarketplace: false,
        pageIndex: nextPageIndex,
        sortMode,
      });

      setPosts((currentPosts) => sortFeedPosts(mergeFeedPosts(currentPosts, data), sortMode));
      setPageIndex(nextPageIndex);
      setHasMore(data.length >= FEED_PAGE_SIZE && data.length > 0);
    } catch (err) {
      console.error('Error fetching more posts:', err);
    } finally {
      setLoadingMore(false);
    }
  };

  return(
    <main className="home-main">
      <section className="home-flux-container">
        <div className="home-flux">
          <div className="home-flux-panel">
            <FeedControls sortMode={sortMode} onSortChange={setSortMode} />
            <Flux
              posts={posts}
              loading={loading}
              error={error}
              onLoadMore={handleLoadMore}
              hasMore={hasMore}
              loadingMore={loadingMore}
            />
          </div>
        </div>
      </section>
    </main>
  );
}

export default App;
