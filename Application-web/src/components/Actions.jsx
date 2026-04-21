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
        
        <button id="action" type="button" onClick={() => navigate('/Create')} disabled={!user}>
          <span className="action-icon-wrap">
            <span className="action-icon create-post-icon" aria-hidden="true" />
          </span>
          <span className="action-label">New post</span>
        </button>
        
        <button id="action" type="button" onClick={() => navigate('/MsgPage')} disabled={!user}>
          <span className="action-icon-wrap">
            <span className="action-icon message-icon" aria-hidden="true" />
          </span>
          <span className="action-label">Messages</span>
        </button>


      </div>
    </div>
  );
}
