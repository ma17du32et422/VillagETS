import { useEffect, useState } from 'react';
import { getBaseUrl } from '../API';
import Message from './subcomponents/Message';
import '../assets/Messages.css'
export default function Messages({ selectedUserId, onSelectUser }) {
  const [users, setUsers] = useState([]);
  const [isAdding, setIsAdding] = useState(false);
  const [newUserId, setNewUserId] = useState("");
  const [error, setError] = useState("");

  const fetchMyConvos = async () => {
    try {
      setError("");
      const res = await fetch(`${getBaseUrl()}/chat/conversations`, {
        credentials: 'include'
      });
      if (!res.ok) {
        throw new Error('Failed to load discussions.');
      }
      const data = await res.json();
      setUsers(data.map(item => item.otherUser).filter(Boolean));
    } catch (err) {
      console.error('Failed to load discussions:', err);
      setError(err.message ?? 'Failed to load discussions.');
    }
  };

  useEffect(() => {
    fetchMyConvos();
  }, [selectedUserId]);

  const handleAddUser = async () => {
    const trimmedUserId = newUserId.trim();
    if (!trimmedUserId) return;

    setError("");

    try {
      const res = await fetch(`${getBaseUrl()}/user/${trimmedUserId}`, {
        credentials: 'include'
      });

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || 'User not found.');
      }

      const data = await res.json();
      const foundUser = {
        id: data.userId,
        pseudo: data.pseudo,
        nom: data.nom,
        prenom: data.prenom,
        photoProfil: data.photoProfil
      };

      setUsers((currentUsers) => (
        currentUsers.some((entry) => entry.id === foundUser.id)
          ? currentUsers
          : [foundUser, ...currentUsers]
      ));

      onSelectUser(foundUser.id);
      setNewUserId("");
      setIsAdding(false);
    } catch (err) {
      console.error('Failed to start discussion:', err);
      setError(err.message ?? 'Failed to start discussion.');
    }
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

      {error && (
        <div className="messages-error">
          {error}
        </div>
      )}

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
              user={user}
              selected={selectedUserId === user.id}
              onClick={() => onSelectUser(user.id)}
            />
          ))
        ) : !error && (
          <div className="empty-list-msg">
            No conversations yet.
          </div>
        )}
      </div>
    </div>
  );
}
