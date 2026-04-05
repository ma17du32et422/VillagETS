/** Package imports */
import {useNavigate} from 'react-router-dom'
import {useAuth} from '../AuthContext'

/** Style imports */
import '.././assets/Header.css'

/** Header */
export default function Header(){
  /** Redirection to the profile page based on login*/
  const {user} = useAuth()
  const navigate = useNavigate();

  const redirectProfilePage = () => {navigate(user ? '/ProfilePage': '/LoginPage')};
  
  return(
    <div class="head-container">
      <h2 id="app-name">VILLAGETS</h2>
      <input id="search" type="text" placeholder="Search"></input>
      <button id="profile" type='button' onClick={redirectProfilePage}>
        <p id="profile-text">{user ? 'My Profile' : 'Login'}</p>
      </button>  
    </div>
  );
}