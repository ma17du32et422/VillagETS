/** Component imports */

import Header from "./components/Header";
import Actions from "./components/Actions";
import Flux from "./components/Flux";
import Messages from "./components/Messages";

/** Styling */
import './assets/App.css'

/** Layout for the web application */
/** DO NOT TOUCH */
function App(){
  return(
    <>
      <header id="header"><Header /></header>

      <main id="main">

        <section id="actions" className="scrollable"><Actions /></section>

        <section id="flux" className="scrollable"><Flux /></section>

        <section id="messages" className="scrollable"><Messages /></section>

      </main>
    </>
  );
}

export default App;