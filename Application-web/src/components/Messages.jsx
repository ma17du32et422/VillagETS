import { useEffect, useState } from 'react';
import { getBaseUrl } from '../API';
import { useAuth } from '../AuthContext';
import { useChat } from '../ChatProvider';
import Message from './subcomponents/Message';
import '../assets/Messages.css'
export default function Messages({ selectedUserId, onSelectUser }) {
  const { user } = useAuth();
  const { lastMessage, lastActivity } = useChat();
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
  }, []);

  const upsertUser = (foundUser) => {
    if (!foundUser?.id) return;

    setUsers((currentUsers) => {
      const existingIndex = currentUsers.findIndex((entry) => entry.id === foundUser.id);
      if (existingIndex === 0) return currentUsers;
      if (existingIndex > 0) {
        const next = [...currentUsers];
        const [existing] = next.splice(existingIndex, 1);
        next.unshift({ ...existing, ...foundUser });
        return next;
      }
      return [foundUser, ...currentUsers];
    });
  };

  const fetchUserSummary = async (userId) => {
    const res = await fetch(`${getBaseUrl()}/user/${userId}`, {
      credentials: 'include'
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || 'User not found.');
    }

    const data = await res.json();
    return {
      id: data.userId,
      pseudo: data.pseudo,
      nom: data.nom,
      prenom: data.prenom,
      photoProfil: data.photoProfil
    };
  };

  const handleAddUser = async () => {
    const trimmedUserId = newUserId.trim();
    if (!trimmedUserId) return;

    setError("");

    try {
      const foundUser = await fetchUserSummary(trimmedUserId);
      upsertUser(foundUser);

      onSelectUser(foundUser.id);
      setNewUserId("");
      setIsAdding(false);
    } catch (err) {
      console.error('Failed to start discussion:', err);
      setError(err.message ?? 'Failed to start discussion.');
    }
  };

  useEffect(() => {
    if (!user?.userId || !lastActivity) return;

    let partnerUserId = null;
    if (lastActivity.type === 'sent') {
      partnerUserId = lastActivity.message?.receiverId ?? null;
    } else if (lastActivity.type === 'received') {
      const incoming = lastActivity.message;
      if (incoming?.envoyeurId === user.userId) {
        partnerUserId = incoming.receveurId ?? null;
      } else if (incoming?.receveurId === user.userId) {
        partnerUserId = incoming.envoyeurId ?? null;
      }
    }

    if (!partnerUserId || partnerUserId === user.userId) return;

    const existingIndex = users.findIndex((entry) => entry.id === partnerUserId);
    if (existingIndex === 0) {
      return;
    }

    if (existingIndex > 0) {
      setUsers((currentUsers) => {
        const target = currentUsers.find((entry) => entry.id === partnerUserId);
        if (!target) return currentUsers;
        const remaining = currentUsers.filter((entry) => entry.id !== partnerUserId);
        return [target, ...remaining];
      });
      return;
    }

    let cancelled = false;
    fetchUserSummary(partnerUserId)
      .then((foundUser) => {
        if (!cancelled) upsertUser(foundUser);
      })
      .catch((err) => {
        if (!cancelled) {
          console.error('Failed to refresh conversation partner:', err);
          fetchMyConvos();
        }
      });

    return () => {
      cancelled = true;
    };
  }, [lastActivity, user?.userId, users]);

  useEffect(() => {
    if (!user?.userId || !lastMessage) return;

    const isRelevant =
      lastMessage.envoyeurId === user.userId ||
      lastMessage.receveurId === user.userId;

    if (!isRelevant) return;
    fetchMyConvos();
  }, [lastMessage, user?.userId]);

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
