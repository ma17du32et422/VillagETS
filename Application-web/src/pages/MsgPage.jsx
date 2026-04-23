import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Messages from "../components/Messages";
import Chat from "../components/Chat";
import '../assets/MsgPage.css'
import usePageTitle from "../utils/usePageTitle.js";

function MsgPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [selectedUserId, setSelectedUserId] = useState(searchParams.get('userId'));

  usePageTitle("💬");

  useEffect(() => {
    setSelectedUserId(searchParams.get('userId'));
  }, [searchParams]);

  const handleSelectUser = (id) => {
    setSelectedUserId(id);
    navigate(id ? `/MsgPage?userId=${encodeURIComponent(id)}` : '/MsgPage', { replace: true });
  };

  return (
    <div className="app-container">
      <main className="main-layout">
        {/* Sidebar - Left side */}
        <section className="msg-sidebar-container">
          <Messages selectedUserId={selectedUserId} onSelectUser={handleSelectUser} />
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
