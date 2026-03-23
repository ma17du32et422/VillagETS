import { useState } from "react";

/** Component imports */
import Header from "../components/Header";
import Messages from "../components/Messages";
import Discussions from "../components/Discussions";

/** Styling */
import '../assets/App.css'
import '../assets/Discussion.css'
import '../assets/Message.css'


function MsgPage(){

  const [message, setMessage] = useState("");
  const handleSend = () => {
    if (!message.trim()) return;
    // do something with message here
    console.log(message);
    setMessage("");
  };

  return(
    <>
      <header id="header"><Header /></header>

      <main id="main">

        <section id="messages-container">
          <div id="messages"><Messages /></div>
        </section>

        <section id="discussion-container">
          <div id="discussion"><Discussions /></div>
          <input id="write" type="text" placeholder="Write something"
            value={message} 
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSend()}
          />
          
          <button id="send" type='button' onClick={handleSend}>
            <p id="send-text">send</p>
          </button>
        </section>
      </main>
    </>
  );
}

export default MsgPage;