/** Style imports */
import { useState } from 'react'
import '.././assets/Actions.css'

/** Component imports */
import CreatePost from './subcomponents/CreatePost'

/** Actions */
export default function Actions(){
  /** Create post form */
  const [isFormOpen, setIsFormOpen] = useState(false);
  const toggleForm = () => setIsFormOpen(!isFormOpen);
  return(
    /** Actions layout 
     * The buttons do not return anything for now, nor they add a post to the flux
    */
    <div class="actions">
      <button id="create-post" type="button" onClick={toggleForm}>
        <p id="create-post-text">+ New post +</p>
      </button>
      <button id="menu" type="button">
        <p id="menu-text">Menu (TO CHANGE LATER)</p>
      </button>
      <div>{isFormOpen && <CreatePost />}</div>
    </div>
  );
}