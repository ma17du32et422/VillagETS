/** Style imports */
import '../.././assets/CreatePost.css'

/** Create post popup */
export default function CreatePost(){
  return(
    <div id="form-container">
        <h2>New post</h2>
        <form>
            {/** Post details */}
            <input id="form-title" placeholder="Title"></input>
            <textarea id="form-text" placeholder="Add text" rows={12}></textarea>

            {/** Confirmation buttons 
             * In either case the popup closes once you press them
             * If the form does not have a title or text at minimum the post submission fails
            */}
            <button id="form-post" type="button">
                <p id="form-post-text">Post</p>
            </button>
            <button id="form-cancel" type="button">
                <p id="form-cancel-text">Cancel</p>
            </button>
        </form>
    </div>
  );
}