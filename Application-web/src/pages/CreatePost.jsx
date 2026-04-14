import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import CreatePost from '../components/CreatePost';
import '../assets/CreatePost.css';
import '../assets/App.css'
export default function CreatePostPage() {
  const navigate = useNavigate();

  return (
    <>
      <header id="header"><Header /></header>
      <main id="main">
        <CreatePost
          onSuccess={() => navigate('/')}
          onCancel={() => navigate('/')}
        />
      </main>
    </>
  );
}