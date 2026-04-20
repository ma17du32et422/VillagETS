import { Outlet } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../AuthContext'
import Header from './Header'
import Actions from './Actions'
import '../assets/WebsiteLayout.css'

export default function WebsiteLayout() {
  const { user } = useAuth()
  const [sidebarExpanded, setSidebarExpanded] = useState(() => localStorage.getItem('website-sidebar-expanded') === 'true')

  useEffect(() => {
    localStorage.setItem('website-sidebar-expanded', String(sidebarExpanded))
  }, [sidebarExpanded])

  return (
    <>
      <header id="header"><Header /></header>
      <main className="website-main">
        <aside className="website-sidebar-container">
          <div className={`website-sidebar ${sidebarExpanded ? 'expanded' : 'collapsed'}`}>
            <div className="website-sidebar-content">
              <Actions user={user} />
            </div>
            <button
              className="website-sidebar-toggle"
              type="button"
              onClick={() => setSidebarExpanded((current) => !current)}
              aria-expanded={sidebarExpanded}
            >
              <span className="website-sidebar-toggle-icon">{sidebarExpanded ? '<' : '>'}</span>
              <span className="website-sidebar-toggle-label">{sidebarExpanded ? 'Collapse' : 'Expand'}</span>
            </button>
          </div>
        </aside>
        <section className="website-page">
          <Outlet />
        </section>
      </main>
    </>
  )
}
