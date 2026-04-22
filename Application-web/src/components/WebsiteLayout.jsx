import { Outlet } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import Header from './Header'
import Actions from './Actions'
import '../assets/WebsiteLayout.css'

export default function WebsiteLayout() {
  const { user } = useAuth()

  return (
    <>
      <header id="header"><Header /></header>
      <main className="main-page">
        <aside className="sidebar-container">
          <div className="sidebar">
            <div className="sidebar-content">
              <Actions user={user} />
            </div>
          </div>
        </aside>
        <section className="page">
          <Outlet />
        </section>
      </main>
    </>
  )
}
