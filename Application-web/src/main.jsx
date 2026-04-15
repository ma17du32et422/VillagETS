/** DO NOT TOUCH */

import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {BrowserRouter} from 'react-router-dom'
import {AuthProvider} from './AuthContext'
import './assets/main.css'
import App from './App.jsx'
import { ChatProvider } from './ChatProvider.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <ChatProvider>
          <App/>
        </ChatProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
