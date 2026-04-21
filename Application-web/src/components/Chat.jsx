import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useChat } from '../ChatProvider';
import { getBaseUrl } from '../API';
import '../assets/Chat.css';

const MAX_MESSAGES = 10;
const WINDOW_MS = 60 * 1000;

const Chat = ({ targetUserId }) => {
    const { lastMessage, sendMessage, isRateLimited: backendLimited } = useChat();
    const [messages, setMessages] = useState([]);
    const [text, setText] = useState("");
    const [error, setError] = useState("");
    const [rateLimitInfo, setRateLimitInfo] = useState({ blocked: false, remaining: MAX_MESSAGES, secondsLeft: 0 });
    const scrollRef = useRef();
    const timestampsRef = useRef([]);
    const countdownRef = useRef(null);
    const isSendingRef = useRef(false);

    const STORAGE_KEY = `chat_rate_limit_${targetUserId}`;
    const isBlocked = rateLimitInfo.blocked || rateLimitInfo.remaining === 0 || backendLimited;

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

    useEffect(() => {
        return () => {
            if (countdownRef.current) clearInterval(countdownRef.current);
        };
    }, []);

    const startCountdown = useCallback((secondsLeft) => {
        if (countdownRef.current) clearInterval(countdownRef.current);

        setRateLimitInfo(prev => ({ ...prev, blocked: true, secondsLeft, remaining: 0 }));

        countdownRef.current = setInterval(() => {
            setRateLimitInfo(prev => {
                const next = prev.secondsLeft - 1;
                if (next <= 0) {
                    clearInterval(countdownRef.current);
                    const now = Date.now();
                    timestampsRef.current = timestampsRef.current.filter(t => now - t < WINDOW_MS);
                    localStorage.setItem(STORAGE_KEY, JSON.stringify(timestampsRef.current));
                    return {
                        blocked: false,
                        remaining: MAX_MESSAGES - timestampsRef.current.length,
                        secondsLeft: 0
                    };
                }
                return { ...prev, secondsLeft: next };
            });
        }, 1000);
    }, [STORAGE_KEY]);

    useEffect(() => {
        const now = Date.now();
        const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
        const valid = saved.filter(t => now - t < WINDOW_MS);
        timestampsRef.current = valid;
        localStorage.setItem(STORAGE_KEY, JSON.stringify(valid));

        if (valid.length >= MAX_MESSAGES) {
            const oldest = valid[0];
            const secondsLeft = Math.ceil((oldest + WINDOW_MS - now) / 1000);
            startCountdown(secondsLeft);
        } else {
            setRateLimitInfo({ blocked: false, remaining: MAX_MESSAGES - valid.length, secondsLeft: 0 });
        }
    }, [STORAGE_KEY, startCountdown]);

    const checkRateLimit = useCallback(() => {
        const now = Date.now();

        timestampsRef.current = timestampsRef.current.filter(t => now - t < WINDOW_MS);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(timestampsRef.current));

        if (timestampsRef.current.length >= MAX_MESSAGES) {
            const oldest = timestampsRef.current[0];
            const secondsLeft = Math.ceil((oldest + WINDOW_MS - now) / 1000);
            startCountdown(secondsLeft);
            return false;
        }

        timestampsRef.current.push(now);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(timestampsRef.current));
        const remaining = MAX_MESSAGES - timestampsRef.current.length;
        setRateLimitInfo({ blocked: false, remaining, secondsLeft: 0 });
        return true;
    }, [startCountdown, STORAGE_KEY]);

    const handleSend = useCallback(() => {
        if (!text.trim()) return;
        if (isSendingRef.current) return;
        if (isBlocked) return;
        if (!checkRateLimit()) return;

        isSendingRef.current = true;
        setTimeout(() => { isSendingRef.current = false; }, 100);

        sendMessage(targetUserId, text);

        const myMessage = {
            envoyeurId: "ME",
            contenu: text,
            dateMsg: new Date().toISOString()
        };
        setMessages((prev) => [...prev, myMessage]);
        setText("");
    }, [text, checkRateLimit, sendMessage, targetUserId, isBlocked]);

    const counterLabel = rateLimitInfo.blocked
        ? `Limite de messages atteinte — réessayez dans ${rateLimitInfo.secondsLeft}s`
        : ' ';

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
                                {m.envoyeurId === targetUserId ? "Them" : "Moi"}
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
                    disabled={isBlocked}
                    placeholder={isBlocked ? "Limite atteinte..." : `Envoyer un message`}
                    onKeyDown={(e) => { if (e.key === 'Enter' && !e.repeat && !isBlocked) handleSend(); }}
                />
                <button
                    className={`send-button ${isBlocked ? 'disabled' : ''}`}
                    onClick={handleSend}
                    disabled={isBlocked}
                >
                    {isBlocked ? `${rateLimitInfo.secondsLeft}s` : 'ENVOYER'}
                </button>
            </div>
        </div>
    );
};

export default Chat;