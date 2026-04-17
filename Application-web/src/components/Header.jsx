/** Package imports */
import {useEffect, useRef, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {useAuth} from '../AuthContext'

/** Style imports */
import '.././assets/Header.css'

/** Header */
export default function Header(){
  /** Redirection to the profile page based on login*/
  const {user} = useAuth()
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false)
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') ?? 'light')
  const menuRef = useRef(null)


  const redirectHome = () => {navigate('/')}
  const redirectSettingsPage = () => {navigate(user ? '/SettingsPage': '/LoginPage')};
  const redirectProfilePage = () => {
    if (!user) {
      navigate('/LoginPage')
      return
    }
    navigate(`/ProfilePage/${user.userId}`)
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target))
        setMenuOpen(false)
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape')
        setMenuOpen(false)
    }

    const handleThemeChanged = (event) => {
      setTheme(event.detail ?? (localStorage.getItem('theme') ?? 'light'))
    }

    document.addEventListener('mousedown', handleClickOutside)
    document.addEventListener('keydown', handleKeyDown)
    window.addEventListener('app:theme-changed', handleThemeChanged)

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleKeyDown)
      window.removeEventListener('app:theme-changed', handleThemeChanged)
    }
  }, [])

  const toggleTheme = () => {
    window.dispatchEvent(new CustomEvent('app:toggle-theme'))
    setMenuOpen(false)
  }
  
  return(
    <div className="head-container">
      <h2 id="app-name" onClick={redirectHome}>VILLAGETS</h2>
      <input id="search" type="text" placeholder="Search"></input>
      <div id="profile-button-wrapper">
        { user ? (
        <div id="profile-menu-wrap" ref={menuRef}>
          <button
            id="profile-menu-button"
            type="button"
            onClick={() => setMenuOpen((open) => !open)}
            aria-haspopup="menu"
            aria-expanded={menuOpen}
          >
            <img
              id="profile-button"
              src={user?.photoProfil || 'https://via.placeholder.com/34/ffffff/000000?text=U'}
              alt="Profile"
            />
          </button>

          {menuOpen && (
            <div id="profile-dropdown" role="menu">
              <button className="profile-dropdown-item" type="button" onClick={redirectProfilePage}>
                View Profile
              </button>
              <button className="profile-dropdown-item" type="button" onClick={redirectSettingsPage}>
                Profile Settings
              </button>
              <button className="profile-dropdown-item" type="button" onClick={toggleTheme}>
                {theme === 'light' ? 'Dark Mode' : 'Light Mode'}
              </button>
            </div>
          )}
        </div>
        ):(
        <button id="profile" type='button' onClick={redirectSettingsPage}>
          <p id="profile-text">Login</p>
        </button>
        )
      }
      </div>
    </div>
  );
}
