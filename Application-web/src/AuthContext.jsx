import { createContext, useContext, useState, useEffect } from 'react'

const AuthContext = createContext(null)

/** Note: A null user means no one is currently logged in */

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const res = await fetch(`${getBaseUrl()}/me`, {
          credentials: 'include',
        })

        if (res.ok) {
          const userData = await res.json()
          setUser(userData)
        } else {
          setUser(null)
        }
      } catch (err) {
        console.error('Auth check failed:', err)
        setUser(null)
      } finally {
        setLoading(false)
      }
    }

    checkAuth()
  }, [])

  const login = (userData) => {
    setUser(userData);
  }

  const logout = async () => {
    try {
      await fetch(`${getBaseUrl()}/auth/logout`, {
        method: 'POST',
        credentials: 'include',
      })
    } catch (err) {
      console.error('Logout failed:', err)
    } finally {
      setUser(null);
    }
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)

/** DO NOT TOUCH */
export const getBaseUrl = () => {
  if (window.location.hostname === 'localhost') {
    return 'http://localhost:5000';
  }
  return 'https://apivillagets.lesageserveur.com';
};