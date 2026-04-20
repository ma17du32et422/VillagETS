import { useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getBaseUrl } from '../API';
import Flux from '../components/Flux';
import ProfileAvatar from '../components/ProfileAvatar';
import '../assets/SearchResults.css';

export default function SearchResults() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('query') || '';
  
  const [users, setUsers] = useState([]);
  const [posts, setPosts] = useState([]);
  const [usersLoading, setUsersLoading] = useState(true);
  const [postsLoading, setPostsLoading] = useState(true);
  const [usersError, setUsersError] = useState(null);
  const [postsError, setPostsError] = useState(null);
  const [openProfileUserId, setOpenProfileUserId] = useState(null);

  useEffect(() => {
    if (!query.trim()) {
      setUsersLoading(false);
      setPostsLoading(false);
      return;
    }

    // Fetch users
    const fetchUsers = async () => {
      try {
        setUsersLoading(true);
        const res = await fetch(`${getBaseUrl()}/user/search?query=${encodeURIComponent(query)}`);
        
        if (!res.ok) {
          throw new Error('Failed to fetch users');
        }

        const data = await res.json();
        setUsers(data || []);
        setUsersError(null);
      } catch (err) {
        console.error('Error fetching users:', err);
        setUsersError(err.message || 'Failed to fetch users');
        setUsers([]);
      } finally {
        setUsersLoading(false);
      }
    };

    // Fetch posts
    const fetchPosts = async () => {
      try {
        setPostsLoading(true);
        const res = await fetch(`${getBaseUrl()}/feed`, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            searchString: query,
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
        })) || []);
        setPostsError(null);
      } catch (err) {
        console.error('Error fetching posts:', err);
        setPostsError(err.message || 'Failed to fetch posts');
        setPosts([]);
      } finally {
        setPostsLoading(false);
      }
    };

    fetchUsers();
    fetchPosts();
  }, [query]);

  const handlePostDeleted = (id) => {
    setPosts(p => p.filter(post => post.id !== id));
  };

  return (
    <main className="search-results-main">
      {query && (
        <div className="search-results-header">
          <h2>Search Results for "{query}"</h2>
        </div>
      )}

      {/* Users Section */}
      <section className={`search-users-section ${openProfileUserId ? 'expanded' : ''}`}>
        <h3 className="search-section-title">Users</h3>
        {usersLoading && <p className="search-loading">Loading users...</p>}
        {usersError && <p className="search-error">Error: {usersError}</p>}
        {!usersLoading && users.length === 0 && <p className="search-no-results">No users found</p>}
        
        {!usersLoading && users.length > 0 && (
          <div className={`search-users-container ${openProfileUserId ? 'preview-open' : ''}`}>
            <div className="search-users-scroll">
              <div className="search-users-list">
                {users.map((user) => (
                  <div
                    key={user.userId}
                    className="search-user-card-wrapper"
                  >
                    <ProfileAvatar
                      user={user}
                      imageProps={{ className: 'search-user-avatar' }}
                      placeholderProps={{ className: 'search-user-avatar-placeholder' }}
                      align="left"
                      anchorClassName="search-user-anchor"
                    />
                    <p className="search-user-name">{user.pseudo}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </section>

      {/* Posts Section */}
      <section className="search-posts-section">
        <h3 className="search-section-title">Posts</h3>
        {postsLoading && <p className="search-loading">Loading posts...</p>}
        {postsError && <p className="search-error">Error: {postsError}</p>}
        {!postsLoading && posts.length === 0 && <p className="search-no-results">No posts found</p>}
        
        {!postsLoading && posts.length > 0 && (
          <div className="search-flux-container">
            <Flux posts={posts} loading={false} error={null} onDelete={handlePostDeleted} />
          </div>
        )}
      </section>
    </main>
  );
}
