/** Component imports */
import Header from "../components/Header";
import Actions from "../components/Actions";
import Flux from "../components/Flux";
import Messages from "../components/Messages";

/** Styling */
import '../assets/App.css'

/** Layout for the web application */
/** DO NOT TOUCH */
function App(){
  return(
    <>
      <header id="header"><Header /></header>

      <main id="main">

        <section id="actions-container">
          <div id="actions"><Actions /></div>
        </section>

        <section id="flux-container">
          <div id="flux"><Flux /></div>
        </section>

        <section id="messages-container">
          <div id="messages"><Messages /></div>
        </section>

      </main>
    </>
  );
}

export default App;