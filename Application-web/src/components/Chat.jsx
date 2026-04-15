import React, { useEffect, useState, useRef } from 'react';
import { useChat } from '../ChatProvider';
import '../assets/Chat.css';

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
        <div className="chat-container">
            {/* Message List */}
            <div className="message-list">
                {messages.map((m, i) => (
                    <div key={i} className="message-wrapper">
                        <div className="message-bubble">
                            <small className="message-sender">
                                {m.envoyeurId === targetUserId ? "Them" : "Me"}
                            </small>
                            {m.contenu}
                        </div>
                    </div>
                ))}
                <div ref={scrollRef} />
            </div>

            {/* Entry Box */}
            <div className="entry-box">
                <input 
                    className="message-input"
                    value={text} 
                    onChange={(e) => setText(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                    placeholder="Type a message..."
                />
                <button 
                    className="send-button"
                    onClick={handleSend}
                >
                    SEND
                </button>
            </div>
        </div>
    );
};

export default Chat;