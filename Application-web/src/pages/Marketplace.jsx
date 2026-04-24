import { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import MarketplaceGrid from '../components/MarketplaceGrid';
import FeedControls from '../components/FeedControls';
import {
  DEFAULT_FEED_SORT_MODE,
  FEED_PAGE_SIZE,
  mergeFeedPosts,
  requestFeed,
  sortFeedPosts,
} from '../utils/feed';

import '../assets/App.css';
import '../assets/MarketplaceCard.css';
import usePageTitle from "../utils/usePageTitle";

function Marketplace(){
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [sortMode, setSortMode] = useState(DEFAULT_FEED_SORT_MODE);
  const [priceFilter, setPriceFilter] = useState({ min: null, max: null });
  const [pageIndex, setPageIndex] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  usePageTitle("Marketplace")
  
  useEffect(() => {
    if (authLoading) return;

    const fetchPosts = async () => {
      setLoading(true);
      try {
        const data = await requestFeed({
          isMarketplace: true,
          minPrice: priceFilter.min,
          maxPrice: priceFilter.max,
          pageIndex: 0,
          sortMode,
        });

        const marketplacePosts = data.filter(post => post.prix != null);
        setPosts(sortFeedPosts(marketplacePosts, sortMode));
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
  }, [authLoading, priceFilter, sortMode, user]);

  const handlePriceChange = (nextRange) => {
    setPriceFilter({
      min: nextRange.min,
      max: nextRange.max,
    });
  };

  const handlePostCreated = (newPost) => {
    if (newPost.prix != null) {
      setPosts((prev) => [newPost, ...prev]);
    }
  };

  const handleLoadMore = async () => {
    if (loading || loadingMore || !hasMore) return;

    const nextPageIndex = pageIndex + 1;
    setLoadingMore(true);
    try {
      const data = await requestFeed({
        isMarketplace: true,
        minPrice: priceFilter.min,
        maxPrice: priceFilter.max,
        pageIndex: nextPageIndex,
        sortMode,
      });

      const marketplacePosts = data.filter(post => post.prix != null);
      setPosts((currentPosts) => sortFeedPosts(mergeFeedPosts(currentPosts, marketplacePosts), sortMode));
      setPageIndex(nextPageIndex);
      setHasMore(data.length >= FEED_PAGE_SIZE && data.length > 0);
    } catch (err) {
      console.error('Error fetching more marketplace posts:', err);
    } finally {
      setLoadingMore(false);
    }
  };

  return(
    <main className="home-main">
      <section className="home-flux-container">
        <div className="home-flux marketplace-feed">
          <div className="home-flux-panel">
            <FeedControls
              sortMode={sortMode}
              onSortChange={setSortMode}
              marketplacePriceFilter={{
                minValue: priceFilter.min,
                maxValue: priceFilter.max,
                onChange: handlePriceChange,
              }}
            />
            <MarketplaceGrid
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

export default Marketplace;
