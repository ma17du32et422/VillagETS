import { useState } from "react";
import Header from "../components/Header";
import Messages from "../components/Messages";
import Chat from "../components/Chat";

function MsgPage() {
  const [selectedUserId, setSelectedUserId] = useState(null);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      <Header />
      
      <main style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        {/* Sidebar - Left side */}
        <section style={{ width: '300px', borderRight: '1px solid #ccc', overflowY: 'auto' }}>
          <Messages onSelectUser={(id) => setSelectedUserId(id)} />
        </section>

        {/* Chat Area - Right side */}
        <section style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          {selectedUserId ? (
            <Chat targetUserId={selectedUserId} />
          ) : (
            <div style={{ margin: 'auto', color: '#888' }}>Select a user to start chatting</div>
          )}
        </section>
      </main>
    </div>
  );
}

export default MsgPage;