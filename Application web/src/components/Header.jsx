/** Style imports */
import '.././assets/Header.css'

/** Header */
export default function Header(){
  return(
    <div class="head-container">
      <h2 id="app-name">VILLAGETS</h2>
      <input id="search" type="text" placeholder="Search"></input>
      <button id="profile" type='button'>
        <p id="profile-text">Profile</p>
      </button>  
    </div>
  );
}