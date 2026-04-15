/** Package imports */
import {useNavigate} from 'react-router-dom'
import {useAuth} from '../AuthContext'
import {useEffect, useState} from 'react'
import { getBaseUrl } from '../API'

/** Style imports */
import '.././assets/Header.css'

/** Header */
export default function Header(){
  /** Redirection to the profile page based on login*/
  const {user} = useAuth()
  const navigate = useNavigate();


  const redirectHome = () => {navigate('/')}
  const redirectProfilePage = () => {navigate(user ? '/ProfilePage': '/LoginPage')};
  
  return(
    <div className="head-container">
      <h2 id="app-name" onClick={redirectHome}>VILLAGETS</h2>
      <input id="search" type="text" placeholder="Search"></input>
      <div id="profile-button-wrapper">
        { user ? (<img
          id="profile-button"
          src={user?.photoProfil || 'https://via.placeholder.com/34/ffffff/000000?text=U'}
          alt="Profile"
          onClick={redirectProfilePage}
          
        />):(
        <button id="profile" type='button' onClick={redirectProfilePage}>
          <p id="profile-text">Login</p>
        </button>
        )
      }
      </div>
    </div>
  );
}