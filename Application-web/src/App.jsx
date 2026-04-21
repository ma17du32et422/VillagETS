import {Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import MsgPage from './pages/MsgPage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import UserProfilePage from './pages/UserProfilePage'
import TestSignup from './pages/TestSignup';
import Signup from './pages/Signup'
import CreatePost from './pages/CreatePost'
import SearchResults from './pages/SearchResults'
import { useEffect, useState } from 'react'
import WebsiteLayout from './components/WebsiteLayout'


function App(){
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') ?? 'light');

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
    window.dispatchEvent(new CustomEvent('app:theme-changed', { detail: theme }));
  }, [theme]);

  const toggleTheme = () => setTheme(t => t === 'light' ? 'dark' : 'light');

  useEffect(() => {
    const handleThemeToggle = () => {
      setTheme((prevTheme) => (prevTheme === 'light' ? 'dark' : 'light'));
    };

    window.addEventListener('app:toggle-theme', handleThemeToggle);

    return () => {
      window.removeEventListener('app:toggle-theme', handleThemeToggle);
    };
  }, []);
  
  // Listen for the "j" key press
  useEffect(() => {
    const handleKeyDown = (event) => {
      // Check if the key pressed is 'j' (case-insensitive)
      // Also ensure the user isn't typing in an input or textarea
      if (
        event.key.toLowerCase() === 'j' && 
        !['INPUT', 'TEXTAREA'].includes(document.activeElement.tagName)
      ) {
        setTheme((prevTheme) => (prevTheme === 'light' ? 'dark' : 'light'));
      }
    };

    window.addEventListener('keydown', handleKeyDown);

    // Clean up the event listener on unmount
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, []);
  
  return (
    /** Import all pages created here */
    <Routes>
      <Route element={<WebsiteLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/MsgPage" element={<MsgPage />} />
        <Route path="/Create" element={<CreatePost/>}/>
        <Route path="/Search" element={<SearchResults />} />
        <Route path="/SettingsPage" element={<ProfilePage />} />
        <Route path="/ProfilePage/:userId" element={<UserProfilePage />} />
      </Route>
      <Route path="/LoginPage" element={<LoginPage />}/>
      <Route path="/Signup" element={<Signup/>}/>
      <Route path="/test-signup" element={<TestSignup />} />
    </Routes>
  )
}

export default App
