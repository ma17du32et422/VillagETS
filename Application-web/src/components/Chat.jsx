import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useChat } from '../ChatProvider';
import { getBaseUrl } from '../API';
import '../assets/Chat.css';

const MAX_MESSAGES = 10;
const WINDOW_MS = 60 * 1000; // 1 minute

const Chat = ({ targetUserId }) => {
    const { lastMessage, sendMessage } = useChat();
    const [messages, setMessages] = useState([]);
    const [text, setText] = useState("");
    const [error, setError] = useState("");
    const [rateLimitInfo, setRateLimitInfo] = useState({ blocked: false, remaining: MAX_MESSAGES, secondsLeft: 0 });
    const scrollRef = useRef();
    const timestampsRef = useRef([]); // historique des envois
    const countdownRef = useRef(null);

    // Load History
    useEffect(() => {
        const fetchHistory = async () => {
            try {
                setError("");
                const res = await fetch(`${getBaseUrl()}/chat/history/${targetUserId}`, {
                    method: 'GET',
                    credentials: 'include'
                });
                if (!res.ok) {
                    const message = await res.text();
                    throw new Error(message || 'Failed to load discussion.');
                }
                const data = await res.json();
                setMessages(data || []);
            } catch (err) {
                console.error("Failed to fetch history", err);
                setMessages([]);
                setError(err.message ?? 'Failed to load discussion.');
            }
        };
        fetchHistory();
    }, [targetUserId]);

    // Listen for WebSocket updates
    useEffect(() => {
        if (lastMessage) {
            if (lastMessage.envoyeurId === targetUserId || lastMessage.receveurId === targetUserId) {
                setMessages((prev) => [...prev, lastMessage]);
            }
        }
    }, [lastMessage, targetUserId]);

    // Auto-scroll to bottom
    useEffect(() => {
        scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // Nettoyage du countdown au démontage
    useEffect(() => {
        return () => {
            if (countdownRef.current) clearInterval(countdownRef.current);
        };
    }, []);

    const startCountdown = useCallback((secondsLeft) => {
        if (countdownRef.current) clearInterval(countdownRef.current);

        setRateLimitInfo(prev => ({ ...prev, blocked: true, secondsLeft }));

        countdownRef.current = setInterval(() => {
            setRateLimitInfo(prev => {
                const next = prev.secondsLeft - 1;
                if (next <= 0) {
                    clearInterval(countdownRef.current);
                    // Recalculer le remaining réel au déblocage
                    const now = Date.now();
                    timestampsRef.current = timestampsRef.current.filter(t => now - t < WINDOW_MS);
                    return {
                        blocked: false,
                        remaining: MAX_MESSAGES - timestampsRef.current.length,
                        secondsLeft: 0
                    };
                }
                return { ...prev, secondsLeft: next };
            });
        }, 1000);
    }, []);

    const checkRateLimit = useCallback(() => {
        const now = Date.now();

        // Purger les timestamps hors fenêtre
        timestampsRef.current = timestampsRef.current.filter(t => now - t < WINDOW_MS);

        if (timestampsRef.current.length >= MAX_MESSAGES) {
            // Calculer le temps avant que le plus vieux expire
            const oldest = timestampsRef.current[0];
            const secondsLeft = Math.ceil((oldest + WINDOW_MS - now) / 1000);
            startCountdown(secondsLeft);
            return false;
        }

        timestampsRef.current.push(now);
        const remaining = MAX_MESSAGES - timestampsRef.current.length;
        setRateLimitInfo({ blocked: false, remaining, secondsLeft: 0 });
        return true;
    }, [startCountdown]);

    const handleSend = () => {
        if (!text.trim()) return;
        if (!checkRateLimit()) return; // bloqué

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

    // Label du bouton selon l'état
    const buttonLabel = rateLimitInfo.blocked
        ? `${rateLimitInfo.secondsLeft}s`
        : 'SEND';

    // Label du compteur
    const counterLabel = rateLimitInfo.blocked
        ? `Limite atteinte — réessayez dans ${rateLimitInfo.secondsLeft}s`
        : `${rateLimitInfo.remaining}/${MAX_MESSAGES} messages restants`;

    const counterClass = rateLimitInfo.blocked
        ? 'rate-limit-counter blocked'
        : rateLimitInfo.remaining <= 3
            ? 'rate-limit-counter warning'
            : 'rate-limit-counter';

    return (
        <div className="chat-container">
            {/* Message List */}
            <div className="message-list">
                {error && (
                    <div className="placeholder-text">
                        {error}
                    </div>
                )}
                {messages.map((m, i) => (
                    <div
                        key={i}
                        className={`message-wrapper ${m.envoyeurId === targetUserId ? 'incoming' : 'outgoing'}`}
                    >
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

            {/* Rate limit counter */}
            <div className={counterClass}>
                {counterLabel}
            </div>

            {/* Entry Box */}
            <div className="entry-box">
                <input
                    className="message-input"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                    placeholder={rateLimitInfo.blocked ? "Limite atteinte..." : "Type a message..."}
                    disabled={rateLimitInfo.blocked}
                />
                <button
                    className={`send-button ${rateLimitInfo.blocked ? 'disabled' : ''}`}
                    onClick={handleSend}
                    disabled={rateLimitInfo.blocked}
                >
                    {buttonLabel}
                </button>
            </div>
        </div>
    );
};

export default Chat;