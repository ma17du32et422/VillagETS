import { useNavigate } from 'react-router-dom';
import CreatePost from '../components/CreatePost';
import '../assets/CreatePost.css';
import '../assets/App.css'
import usePageTitle from "../utils/usePageTitle";

export default function CreatePostPage() {
  const navigate = useNavigate();
  usePageTitle("Exprimez-vous");
  return (
    <main id="main">
      <CreatePost
        onSuccess={() => navigate('/')}
        onCancel={() => navigate('/')}
      />
    </main>
  );
}
