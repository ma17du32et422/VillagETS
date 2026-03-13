/** Style imports */
import '../.././assets/Post.css'

/** Image imports 
 * TO CHANGE DYNAMICALLY LATER
*/
import image from '../.././assets/images/example.jpg'

/** Post */
export default function Post(){
  return(

    /** Post layout 
     * TO CHANGE DYNAMICALLY LATER
    */
    <article class="post">

      <h2 id="title">Title</h2>

      <img id="image" src={image}></img>

      <p id="contents">Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor 
        incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation 
        ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit 
        in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat 
        non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>

    </article>
  );
}