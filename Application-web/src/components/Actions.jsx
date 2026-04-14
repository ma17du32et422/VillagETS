/** Style imports */
import { useNavigate } from 'react-router-dom';
import { useState } from 'react'
import '.././assets/Actions.css'

/** Component imports */

/** Actions */
export default function Actions({ onPostCreated, user }){
  const navigate = useNavigate();
  /** Toggle menu visibility when in child tabs */
  const [isActionsOpen, setIsActionsOpen] = useState(true);

  return(

    <div className="actions-layout">{isActionsOpen &&
      <div className="actions">
        <button id="create-post" type="button" onClick={() => navigate('/create')} disabled={!user}>
          <p id="create-post-text">+ New post +</p>
        </button>
        <button id="menu" type="button">
          <p id="menu-text">Menu (TO CHANGE LATER)</p>
        </button>
      </div>}
    </div>
  );
}