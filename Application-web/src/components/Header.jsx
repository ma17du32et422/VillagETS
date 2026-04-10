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
  const [profilePic, setProfilePic] = useState('')
  const navigate = useNavigate();

  useEffect(() => {
    const loadProfilePicture = async () => {
      if (!user?.userId) {
        setProfilePic('')
        return
      }

      try {
        const res = await fetch(`${getBaseUrl()}/Utilisateur`, {
          credentials: 'include',
        })
        if (!res.ok) return

        const users = await res.json()
        const currentUser = users.find((u) => u.id_utilisateur === user.userId)
        setProfilePic(currentUser?.photo_profil || '')
      } catch (err) {
        console.error('Could not load profile picture:', err)
      }
    }

    loadProfilePicture()
  }, [user])

  const redirectHome = () => {navigate('/')}
  const redirectProfilePage = () => {navigate(user ? '/ProfilePage': '/LoginPage')};
  
  return(
    <div className="head-container">
      <h2 id="app-name" onClick={redirectHome}>VILLAGETS</h2>
      <input id="search" type="text" placeholder="Search"></input>
      <div id="profile-button-wrapper">
        {profilePic && (
          <img id="profile-button-pic" src={profilePic} alt="Profile" />
        )}
        <button id="profile" type='button' onClick={redirectProfilePage}>
          <p id="profile-text">{user ? 'My Profile' : 'Login'}</p>
        </button>
      </div>
    </div>
  );
}