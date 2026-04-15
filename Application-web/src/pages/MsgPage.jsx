import { useState } from "react";
import Header from "../components/Header";
import Messages from "../components/Messages";
import Chat from "../components/Chat";
import '../assets/MsgPage.css'

function MsgPage() {
  const [selectedUserId, setSelectedUserId] = useState(null);

  return (
    <div className="app-container">
      <Header />
      
      <main className="main-layout">
        {/* Sidebar - Left side */}
        <section className="sidebar-container">
          <Messages onSelectUser={(id) => setSelectedUserId(id)} />
        </section>

        {/* Chat Area - Right side */}
        <section className="chat-view-container">
          {selectedUserId ? (
            <Chat targetUserId={selectedUserId} />
          ) : (
            <div className="placeholder-text">
              Select a user to start chatting
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default MsgPage;