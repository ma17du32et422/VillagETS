/** Subcomponent imports */
import Post from './subcomponents/Post'

/** Flux
 *  Posts should not be manually added to the flux, it will instead take from the database
 *  For the 1st sprint, we will manually add posts for the sake of presenting
 */
export default function Flux(){
  return(
    <div>
      <Post />
      <Post />
      <Post />
    </div>
  );
}