/** Style imports */
import { useState } from 'react';
import '../.././assets/CreatePost.css'

/** Create post popup */
export default function CreatePost(){
    /** Form variable use states */
    const [title, setTitle] = useState("");
    const [textContent, setTextContent] = useState("");

    /** Form submission */
    const submit = (event) => {
        event.preventDefault();
    }; 
    const handleTitle = (event) => {setTitle(event.target.value);};
    const handleTextContent = (event) => {setTextContent(event.target.value);};

  return(
    <div id="form-container">
        <h2>New post</h2>
        <form id="create-post-form" onSubmit={submit}>
            {/** Post details */}
            <input id="form-title" placeholder="Title" value={title} onChange={handleTitle}></input>
            <textarea id="form-text" placeholder="Add text" rows={12} 
            value={textContent} onChange={handleTextContent}></textarea>

            {/** Confirmation buttons 
             * In either case the popup closes once you press them
             * If the form does not have a title or text at minimum the post submission fails
            */}
            <button id="form-post" type="submit">
                <p id="form-post-text">Post</p>
            </button>
            <button id="form-cancel" type="button">
                <p id="form-cancel-text">Cancel</p>
            </button>
        </form>
    </div>
  );
}