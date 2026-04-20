import { useState } from 'react'
import '../assets/UserSearch.css'

export default function UserSearch(){
  const [searchQuery, setSearchQuery] = useState('')

  const handleSearch = (e) => {
    setSearchQuery(e.target.value)
  }

  return(
    <div className="user-search-container">
      <div className="user-search-content">
        <h3 className="user-search-title">Find Users</h3>
        <input
          type="text"
          className="user-search-input"
          placeholder="Search for users..."
          value={searchQuery}
          onChange={handleSearch}
        />
        <div className="user-search-filters">
          {/* Filters will be added here later */}
        </div>
      </div>
    </div>
  )
}