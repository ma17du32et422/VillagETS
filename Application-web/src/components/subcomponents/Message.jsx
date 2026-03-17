/** Package imports */
import { useNavigate } from 'react-router-dom'

/** Style imports */
import '../.././assets/Message.css'

/** Image imports 
 * TO CHANGE DYNAMICALLY LATER
*/
import pfp from '../.././assets/images/example.jpg'

/** Post */
export default function Message(){
  /** Redirects the user to the messages page */
  const navigate = useNavigate();
  const redirectMsgPage = () => {navigate('/MsgPage')};

  return(

    /** Message layout
     * TO CHANGE DYNAMICALLY LATER
    */
    <div class="message" onClick={redirectMsgPage}>

      <div id="pfp-container">
        <img id="pfp" src={pfp}></img>
      </div>

      <div id="msg-container">
        <b id="user">RAM cat</b>
        <p id="msg">Pls don't sell me for RAM</p>
      </div>

    </div>
  );
}