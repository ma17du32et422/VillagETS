/** Style imports */
import '../.././assets/Post.css'

/** Post */
export default function Post({post}){
  return(

    /** Post layout 
     * TO CHANGE DYNAMICALLY LATER
    */
    <article className="post">

      <div id="post-header">
        <h2 id="title">{post.title}</h2>

        <div id="op-info">
          <p id="op-name">{post.op}</p>
          <p id="datetime">{post.datetime}</p>
        </div>
      </div>

      <div id="image-container">
        <img id="image" src={post.imageUrl}></img>
      </div>

      <p id="contents">{post.contents}</p>

    </article>
  );
}