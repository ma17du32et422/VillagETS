import { Outlet } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../AuthContext'
import Header from './Header'
import Actions from './Actions'
import '../assets/WebsiteLayout.css'

export default function WebsiteLayout() {
  const { user } = useAuth()
  const [sidebarExpanded, setSidebarExpanded] = useState(() => localStorage.getItem('sidebar-expanded') === 'true')
  
  useEffect(() => {
    localStorage.setItem('sidebar-expanded', String(sidebarExpanded))
  }, [sidebarExpanded])

  return (
    <>
      <header id="header"><Header /></header>
      <main className="main-page">
        <aside className="sidebar-container">
          <div className={`sidebar ${sidebarExpanded ? 'expanded' : 'collapsed'}`}>
            <div className="sidebar-content">
              <Actions user={user} />
            </div>
            <button
              className="sidebar-toggle"
              type="button"
              onClick={() => setSidebarExpanded((current) => !current)}
              aria-expanded={sidebarExpanded}
            >
              <span className="sidebar-toggle-icon-wrap">
                <span className="sidebar-toggle-icon" aria-hidden="true" />
              </span>
              <span className="sidebar-toggle-label">{sidebarExpanded ? 'Collapse' : 'Expand'}</span>
            </button>
          </div>
        </aside>
        <section className="page">
          <Outlet />
        </section>
      </main>
    </>
  )
}
