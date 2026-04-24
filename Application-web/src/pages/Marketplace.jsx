import { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import Flux from "../components/Flux";
import FeedControls from '../components/FeedControls';
import {
  DEFAULT_FEED_SORT_MODE,
  requestFeed,
  sortFeedPosts,
} from '../utils/feed';

import '../assets/App.css';
import usePageTitle from "../utils/usePageTitle";

function Marketplace(){
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sortMode, setSortMode] = useState(DEFAULT_FEED_SORT_MODE);
  const [priceFilter, setPriceFilter] = useState({ min: null, max: null });

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
        setError(null);
      } catch (err) {
        console.error('Error fetching posts:', err);
        setError(err.message ?? 'Failed to fetch posts');
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

  return(
    <main className="home-main">
      <section className="home-flux-container">
        <div className="home-flux">
          <FeedControls
            sortMode={sortMode}
            onSortChange={setSortMode}
            marketplacePriceFilter={{
              minValue: priceFilter.min,
              maxValue: priceFilter.max,
              onChange: handlePriceChange,
            }}
          />
          <Flux posts={posts} loading={loading} error={error} />
        </div>
      </section>
    </main>
  );
}

export default Marketplace;
