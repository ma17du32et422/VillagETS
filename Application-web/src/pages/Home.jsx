import { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import Flux from "../components/Flux";
import FeedControls from '../components/FeedControls';
import {
  DEFAULT_FEED_SORT_MODE,
  requestFeed,
  sortFeedPosts,
} from '../utils/feed';

import '../assets/App.css'
import usePageTitle from "../utils/usePageTitle";

function App(){
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sortMode, setSortMode] = useState(DEFAULT_FEED_SORT_MODE);

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
        setError(null);
      } catch (err) {
        console.error('Error fetching posts:', err);
        setError(err.message ?? 'Failed to fetch posts');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [authLoading, sortMode, user]);

  const handlePostCreated = (newPost) => {
    setPosts((prev) => [newPost, ...prev]);
  };

  return(
    <main className="home-main">
      <section className="home-flux-container">
        <div className="home-flux">
          <FeedControls sortMode={sortMode} onSortChange={setSortMode} />
          <Flux posts={posts} loading={loading} error={error} />
        </div>
      </section>
    </main>
  );
}

export default App;
