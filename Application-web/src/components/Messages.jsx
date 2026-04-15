import { useEffect, useState } from 'react';
import Message from './subcomponents/Message';

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
    <div id="messages-sidebar" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>

      {/* Sidebar Header */}
      <div style={{
        padding: '15px',
        borderBottom: '2px solid #eee',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: '#fff'
      }}>
        <b style={{ fontSize: '11px', letterSpacing: '1px', textTransform: 'uppercase' }}>Discussions</b>
        <button
          onClick={() => setIsAdding(!isAdding)}
          style={{
            border: '1px solid #000',
            background: isAdding ? '#000' : 'none',
            color: isAdding ? '#fff' : '#000',
            cursor: 'pointer',
            padding: '0px 8px',
            fontSize: '18px'
          }}
        >
          {isAdding ? '×' : '+'}
        </button>
      </div>

      {/* Manual ID Input */}
      {isAdding && (
        <div style={{ padding: '10px', borderBottom: '1px solid #eee', backgroundColor: '#fafafa' }}>
          <input
            autoFocus
            placeholder="Paste UserID..."
            style={{
              width: '100%',
              padding: '8px',
              border: '1px solid #ddd',
              fontSize: '12px',
              marginBottom: '5px',
              outline: 'none'
            }}
            value={newUserId}
            onChange={(e) => setNewUserId(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleAddUser()}
          />
          <button
            onClick={handleAddUser}
            style={{
              width: '100%',
              padding: '6px',
              backgroundColor: '#000',
              color: '#fff',
              fontSize: '10px',
              fontWeight: 'bold',
              border: 'none',
              cursor: 'pointer'
            }}
          >
            START CHAT
          </button>
        </div>
      )}

      {/* Dynamic List */}
      <div style={{ flex: 1, overflowY: 'auto' }}>
        {users.length > 0 ? (
          users.map((user) => (
            <Message
              key={user.id}
              name={user.name}
              onClick={() => onSelectUser(user.id)}
            />
          ))
        ) : (
          <div style={{ padding: '20px', textAlign: 'center', color: '#ccc', fontSize: '12px' }}>
            No conversations yet.
          </div>
        )}
      </div>
    </div>
  );
}