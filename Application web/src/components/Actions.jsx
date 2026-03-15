/** Style imports */
import '.././assets/Actions.css'

/** Actions */
export default function Actions(){
  return(

    /** Actions layout 
     * The buttons do not return anything for now, nor they add a post to the flux
    */
    <div class="actions">
      <button id="create-post" type="button">
        <p id="create-post-text">+ New post +</p>
      </button>
      <button id="menu" type="button">
        <p id="menu-text">Menu (TO CHANGE LATER)</p>
      </button>
    </div>
  );
}