/** Style imports */
import '../.././assets/Discussion.css'

/** Image imports 
 * TO CHANGE DYNAMICALLY LATER
*/
import pfp from '../.././assets/images/example.jpg'
import pfp2 from '../.././assets/images/example2.jpg'

/** Post */
export default function Discussion(){
  return(
    <div className="discussions-list">
      {/* message 1 */}
      <div className="discussion">
        <div id="pfp-disc-container">
          <img id="pfp-discussion" src={pfp2}></img>
        </div>
        <div id="disc-container">
          <b id="user">Stare cat</b>
          <p id="disc">He will totally sell you.
                      He really needs the RAM.
                      It is way more useful than you are.
          </p>
        </div>
      </div>

      {/* message 2 */}
      <div className="discussion">
        <div id="pfp-disc-container"> 
          <img id="pfp-discussion" src={pfp}></img>
        </div>
        <div id="disc-container">
          <b id="user">RAM cat</b>
          <p id="disc">Pls don't sell me for RAM</p>
        </div>
      </div>
    </div>
  );
}