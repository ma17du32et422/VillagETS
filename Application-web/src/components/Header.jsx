/** Package imports */
import { useNavigate } from 'react-router-dom'

/** Style imports */
import '.././assets/Header.css'

/** Header */
export default function Header(){
  /** Redirection to the profile page */
  const navigate = useNavigate();
  const redirectProfilePage = () => {navigate('/ProfilePage')};
  return(
    <div class="head-container">
      <h2 id="app-name">VILLAGETS</h2>
      <input id="search" type="text" placeholder="Search"></input>
      <button id="profile" type='button' onClick={redirectProfilePage}>
        <p id="profile-text">Profile</p>
      </button>  
    </div>
  );
}