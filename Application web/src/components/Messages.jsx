/** Messages */ 

/**
 * this function show fake messages for now
 */
export default function Messages(){
  return(
    <div>
      Messages
      <p>John<br></br>
      I'm not not a bot.</p>
      <p>Steve<br></br>
      The cake is a lie.</p>
      <p>Alice<br></br>
      Have you tried turning it off and on again?</p>
      <p>Bob<br></br>
      It works on my machine.</p>
      <p>Marie<br></br>
      Ce n'est pas un bug, c'est une fonctionnalité.</p>
      <p>Carlos<br></br>
      I'll fix it tomorrow. (said every day for 3 weeks)</p>
      <p>Priya<br></br>
      The documentation says otherwise.</p>
      <p>Kenji<br></br>
      Why use one line when you can use forty?</p>
      <p>Fatima<br></br>
      I deployed on a Friday. I regret nothing.</p>
      <p>Zoe<br></br>
      undefined is not a function.</p>
      <p>Marcus<br></br>
      It's not a memory leak, it's a memory... collection.</p>
    </div>
  );
}

/**Banque de données temporaire en attendant que la bd soit disponible */
let liste = [
    {
        name: "John",
        message: "I'm not not a bot."
    },
    {
        name: "Steve",
        message: "The cake is a lie."
    },
    {
        name: "Alice",
        message: "Have you tried turning it off and on again?"
    },
    {
        name: "Bob",
        message: "It works on my machine."
    },
    {
        name: "Marie",
        message: "Ce n'est pas un bug, c'est une fonctionnalité."
    },
    {
        name: "Carlos",
        message: "I'll fix it tomorrow. (said every day for 3 weeks)"
    },
    {
        name: "Priya",
        message: "The documentation says otherwise."
    },
    {
        name: "Kenji",
        message: "Why use one line when you can use forty?"
    },
    {
        name: "Fatima",
        message: "Ship it. We'll patch it later."
    },
    {
        name: "Luca",
        message: "I deployed on a Friday. I regret nothing."
    },
    {
        name: "Zoe",
        message: "undefined is not a function."
    },
    {
        name: "Marcus",
        message: "It's not a memory leak, it's a memory... collection."
    },
]