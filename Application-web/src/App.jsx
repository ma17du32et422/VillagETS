import {Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import MsgPage from './pages/MsgPage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import TestSignup from './pages/TestSignup';
import Signup from './pages/Signup'
import CreatePost from './pages/CreatePost'
import { useEffect, useState } from 'react'


function App(){
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') ?? 'light');

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => setTheme(t => t === 'light' ? 'dark' : 'light');
  
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
      <Route path="/" element={<Home />} />
      <Route path="/MsgPage" element={<MsgPage />} />
      <Route path="/LoginPage" element={<LoginPage />}/>
      <Route path="/Signup" element={<Signup/>}/>
      <Route path="/Create" element={<CreatePost/>}/>
      <Route path="/ProfilePage" element={<ProfilePage />} />
      <Route path="/test-signup" element={<TestSignup />} />
    </Routes>
  )
}

export default App