import { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import Header from "../components/Header";
import Actions from "../components/Actions";
import Flux from "../components/Flux";
import Messages from "../components/Messages";
import { getBaseUrl } from '../API';

import '../assets/App.css'

function App(){
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    if (authLoading) return;

    //if (!user) {
    //  setPosts([]);
    //  setError('You must be logged in to view posts.');
    //  setLoading(false);
    //  return;
    //}

    const fetchPosts = async () => {
      try {
        const res = await fetch(`${getBaseUrl()}/feed`, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            searchString: null,
            tags: null,
            isMarketplace: false,
          }),
        });

        if (!res.ok) {
          throw new Error('Failed to fetch posts');
        }

        const data = await res.json();
        setPosts(data.map((post) => ({
        id: post.id,
        title: post.titre ?? '',
        contents: post.contenu ?? '',
        op: post.op ?? { id: null, pseudo: 'Unknown', photoProfil: null },
        datetime: post.datePublication ?? '',
        media: post.media ?? [],
        tags: post.tags ?? [],
        prix: post.prix ?? null,
        articleAVendre: post.articleAVendre ?? false,
        likes: post.likes ?? 0,
        dislikes: post.dislikes ?? 0,
        commentaires: post.commentaires ?? 0,
        userReaction: post.userReaction ?? null,
        comments: [],
      })));
        setError(null);
      } catch (err) {
        console.error('Error fetching posts:', err);
        setError(err.message ?? 'Failed to fetch posts');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [authLoading, user]);

  const handlePostCreated = (newPost) => {
    setPosts((prev) => [newPost, ...prev]);
  };

  return(
    <>
      <header id="header"><Header /></header>

      <main className="home-main">

        <section className="home-actions-container">
          <div className="home-actions"><Actions onPostCreated={handlePostCreated} user={user} /></div>
        </section>

        <section className="home-flux-container">
          <div className="home-flux"><Flux posts={posts} loading={loading} error={error} /></div>
        </section>

      </main>
    </>
  );
}

export default App;
