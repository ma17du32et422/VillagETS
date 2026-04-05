import {Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import MsgPage from './pages/MsgPage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'


function App(){
  return (
    /** Import all pages created here */
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/MsgPage" element={<MsgPage />} />
      <Route path="/LoginPage" element={<LoginPage />}/>
      <Route path="/ProfilePage" element={<ProfilePage />} />
    </Routes>
  )
}

export default App