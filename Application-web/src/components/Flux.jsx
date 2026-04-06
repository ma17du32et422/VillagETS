/** Subcomponent imports */
import Post from './subcomponents/Post'

/** Flux
 *  Posts should not be manually added to the flux, it will instead take from the database
 *  For the 1st sprint, we will manually add posts for the sake of presenting
 */
export default function Flux(){
  return(
    <div id="feed">
      {posts.map(post => (<Post key={post.id} post={post} />))}
    </div>
  );
}

const posts = [
  {
    "id": 1,
    "title": "Hellaur this is a JSON test :D",
    "author": "RAM Cat",
    "datetime": "2026-04-05 23:15",
    "imageUrl": "/src/assets/images/example.jpg",
    "contents": "Too lazy to write smth here icl"
  },
  {
    "id": 2,
    "title": "The nefarious creature",
    "author": "Evil Cat",
    "datetime": "2026-04-05 23:30",
    "imageUrl": "/src/assets/images/example2.jpg",
    "contents": "The person that sells your cat for RAM"
  }
]