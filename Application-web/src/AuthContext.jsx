import { createContext, useContext, useState, useEffect, useCallback } from 'react'

const AuthContext = createContext(null)

/** Note: A null user means no one is currently logged in */

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = useCallback(async () => {
    try {
      const res = await fetch(`${getBaseUrl()}/me`, {
        credentials: 'include',
      })

      if (!res.ok) {
        setUser(null)
        return null
      }

      const userData = await res.json()
      setUser(userData)
      return userData
    } catch (err) {
      console.error('Auth check failed:', err)
      setUser(null)
      throw err
    }
  }, [])

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await refreshUser()
      } catch (err) {
        // refreshUser already logged and normalized auth state.
      } finally {
        setLoading(false)
      }
    }

    checkAuth()
  }, [refreshUser])

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
    <AuthContext.Provider value={{ user, login, logout, loading, refreshUser }}>
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
