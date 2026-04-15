import React, { useEffect, useState, useRef } from 'react';
import { useChat } from '../ChatProvider';

const Chat = ({ targetUserId }) => {
    const { lastMessage, sendMessage } = useChat();
    const [messages, setMessages] = useState([]);
    const [text, setText] = useState("");
    const scrollRef = useRef();

    // Load History
    useEffect(() => {
        const fetchHistory = async () => {
            try {
                const res = await fetch(`http://localhost:5000/chat/history/${targetUserId}`, {
                    method: 'GET',
                    credentials: 'include'
                });
                const data = await res.json();
                setMessages(data || []);
            } catch (err) {
                console.error("Failed to fetch history", err);
            }
        };
        fetchHistory();
    }, [targetUserId]);

    // Listen for WebSocket updates
    useEffect(() => {
        if (lastMessage) {
            if (lastMessage.EnvoyeurId === targetUserId || lastMessage.ReceveurId === targetUserId) {
                setMessages((prev) => [...prev, lastMessage]);
            }
        }
    }, [lastMessage, targetUserId]);

    // Auto-scroll to bottom
    useEffect(() => {
        scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const handleSend = () => {
        if (!text.trim()) return;
        sendMessage(targetUserId, text);

        // Optimistic update
        const myMessage = {
            envoyeurId: "ME",
            contenu: text,
            dateMsg: new Date().toISOString()
        };
        setMessages((prev) => [...prev, myMessage]);
        setText("");
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
            {/* Message List */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '20px', backgroundColor: '#f9f9f9' }}>
                {messages.map((m, i) => (
                    <div key={i} style={{ display: 'flex', justifyContent: 'flex-start', marginBottom: '10px' }}>
                        <div style={{ 
                            padding: '8px 12px', 
                            border: '1px solid #ddd', 
                            backgroundColor: '#fff', 
                            fontSize: '14px',
                            maxWidth: '70%'
                        }}>
                            <small style={{ color: '#888', display: 'block' }}>
                                {m.envoyeurId === targetUserId ? "Them" : "Me"}
                            </small>
                            {m.contenu}
                        </div>
                    </div>
                ))}
                <div ref={scrollRef} />
            </div>

            {/* Entry Box */}
            <div style={{ padding: '15px', borderTop: '1px solid #ccc', display: 'flex', gap: '10px' }}>
                <input 
                    style={{ flex: 1, padding: '10px', border: '1px solid #ccc' }}
                    value={text} 
                    onChange={(e) => setText(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                    placeholder="Type a message..."
                />
                <button 
                    onClick={handleSend}
                    style={{ padding: '10px 20px', backgroundColor: '#000', color: '#fff', border: 'none', cursor: 'pointer' }}
                >
                    SEND
                </button>
            </div>
        </div>
    );
};

export default Chat;