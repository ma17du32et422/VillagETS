import { useEffect, useState } from 'react';
import Message from './subcomponents/Message';
import '../assets/Messages.css'
export default function Messages({ onSelectUser }) {
  const [users, setUsers] = useState([]);

  const [isAdding, setIsAdding] = useState(false);
  const [newUserId, setNewUserId] = useState("");

  useEffect(() => {
    const fetchMyConvos = async () => {
      const res = await fetch('http://localhost:5000/chat/conversations', {
        credentials: 'include'
      });
      const data = await res.json();
      // 'data' is now an array of { conversationId, otherUser: { pseudo, photoProfil, etc } }
      setUsers(data.map(item => ({
        id: item.otherUser.id,
        name: item.otherUser.pseudo,
        pfp: item.otherUser.photoProfil
      })));
    };
    fetchMyConvos();
  }, []);

  const handleAddUser = () => {
    if (!newUserId.trim()) return;

    // Check if user already exists in our sidebar list
    const exists = users.find(u => u.id === newUserId.trim());

    if (!exists) {
      const newUser = {
        id: newUserId.trim(),
        name: `User ${newUserId.substring(0, 4)}...` // Short label for the ID
      };
      setUsers([newUser, ...users]);
    }

    // Select the user and reset input
    onSelectUser(newUserId.trim());
    setNewUserId("");
    setIsAdding(false);



  };

  return (
    <div id="messages-sidebar">

      {/* Sidebar Header */}
      <div className="sidebar-header">
        <b className="sidebar-title">Discussions</b>
        <button
          onClick={() => setIsAdding(!isAdding)}
          className={`add-toggle-btn ${isAdding ? 'active' : ''}`}
        >
          {isAdding ? '×' : '+'}
        </button>
      </div>

      {/* Manual ID Input */}
      {isAdding && (
        <div className="add-user-section">
          <input
            autoFocus
            placeholder="Paste UserID..."
            className="user-id-input"
            value={newUserId}
            onChange={(e) => setNewUserId(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleAddUser()}
          />
          <button
            onClick={handleAddUser}
            className="start-chat-btn"
          >
            START CHAT
          </button>
        </div>
      )}

      {/* Dynamic List */}
      <div className="users-list">
        {users.length > 0 ? (
          users.map((user) => (
            <Message
              key={user.id}
              name={user.name}
              onClick={() => onSelectUser(user.id)}
            />
          ))
        ) : (
          <div className="empty-list-msg">
            No conversations yet.
          </div>
        )}
      </div>
    </div>
  );
}