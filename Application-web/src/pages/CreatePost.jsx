import { useNavigate } from 'react-router-dom';
import CreatePost from '../components/CreatePost';
import '../assets/CreatePost.css';
import '../assets/App.css'
export default function CreatePostPage() {
  const navigate = useNavigate();

  return (
    <main id="main">
      <CreatePost
        onSuccess={() => navigate('/')}
        onCancel={() => navigate('/')}
      />
    </main>
  );
}
