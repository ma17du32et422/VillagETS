/** Style imports */
import { useNavigate } from 'react-router-dom';
import '.././assets/Actions.css'

/** Component imports */

/** Actions */
export default function Actions({ onPostCreated, user }){
  const navigate = useNavigate();

  return(
    <div className="actions-layout">
      <div className="actions">
        <button id="create-post" type="button" onClick={() => navigate('/create')} disabled={!user}>
          <span className="create-post-icon">+</span>
          <span className="create-post-label">New post</span>
        </button>
      </div>
    </div>
  );
}
