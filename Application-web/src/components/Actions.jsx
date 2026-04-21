/** Style imports */
import { useNavigate, useLocation } from 'react-router-dom';
import '.././assets/Actions.css'

/** Component imports */

/** Actions */
export default function Actions({ onPostCreated, user }){
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path) => {
    if (path === '/' && location.pathname === '/') return true;
    if (path !== '/' && location.pathname === path) return true;
    return false;
  };

  return(
    <div className="actions-layout">
      <div className="actions">
        
        <button id="action" type="button" onClick={() => navigate('/')} className={isActive('/') ? 'active' : ''}>
          <span className="action-icon-wrap">
            <span className="action-icon home-icon" aria-hidden="true" />
          </span>
          <span className="action-label">Home</span>
        </button>

        <button id="action" type="button" onClick={() => navigate('/Marketplace')} className={isActive('/Marketplace') ? 'active' : ''}>
          <span className="action-icon-wrap">
            <span className="action-icon marketplace-icon" aria-hidden="true" />
          </span>
          <span className="action-label">Marketplace</span>
        </button>
        
        <button id="action" type="button" onClick={() => navigate('/Create')} disabled={!user} className={isActive('/Create') ? 'active' : ''}>
          <span className="action-icon-wrap">
            <span className="action-icon create-post-icon" aria-hidden="true" />
          </span>
          <span className="action-label">New post</span>
        </button>
        
        <button id="action" type="button" onClick={() => navigate('/MsgPage')} disabled={!user} className={isActive('/MsgPage') ? 'active' : ''}>
          <span className="action-icon-wrap">
            <span className="action-icon message-icon" aria-hidden="true" />
          </span>
          <span className="action-label">Messages</span>
        </button>

      </div>
    </div>
  );
}
