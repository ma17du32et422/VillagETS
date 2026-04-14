import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import CreatePost from '../components/CreatePost';
import '../assets/CreatePost.css';

export default function CreatePostPage() {
  const navigate = useNavigate();

  return (
    <>
      <header id="header"><Header /></header>
      <div id="create-post-page">
        <CreatePost
          onSuccess={() => navigate('/')}
          onCancel={() => navigate('/')}
        />
      </div>
    </>
  );
}