import {Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import MsgPage from './pages/MsgPage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import TestSignup from './pages/TestSignup';
import Signup from './pages/Signup'
import CreatePost from './pages/CreatePost'


function App(){
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