import { useEffect, useState } from 'react';
import Post from './subcomponents/Post';

export default function Flux({ posts: propPosts, loading, error }) {
  const [posts, setPosts] = useState(propPosts ?? []);

  useEffect(() => setPosts(propPosts ?? []), [propPosts]);

  const handleDelete = (id) => setPosts(p => p.filter(post => post.id !== id));

  if (loading) return <p>Loading posts...</p>;
  if (error) return <p>Error: {error}</p>;

  return (
    <div id="feed">
      {posts.map(post => <Post key={post.id} post={post} onDelete={handleDelete} />)}
    </div>
  );
}