import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useChat } from '../ChatProvider';
import { getBaseUrl } from '../API';
import '../assets/Chat.css';

const MAX_MESSAGES = 10;
const WINDOW_MS = 60 * 1000;
const MESSAGE_FILE_ACCEPT = '.jpg,.jpeg,.png,.webp,.gif,.pdf,.txt,.csv,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip';
const imageAttachmentPattern = /\.(png|jpe?g|gif|webp)(?:$|[?#])/i;

function isImageAttachment(url) {
    return imageAttachmentPattern.test(url);
}

function getAttachmentLabel(url) {
    try {
        const pathname = new URL(url).pathname;
        return decodeURIComponent(pathname.split('/').pop() || 'Attachment');
    } catch {
        return 'Attachment';
    }
}

const Chat = ({ targetUserId }) => {
    const { lastMessage, sendMessage } = useChat();
    const [messages, setMessages] = useState([]);
    const [text, setText] = useState("");
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [error, setError] = useState("");
    const [isUploading, setIsUploading] = useState(false);
    const [rateLimitInfo, setRateLimitInfo] = useState({ blocked: false, remaining: MAX_MESSAGES, secondsLeft: 0 });
    const scrollRef = useRef();
    const fileInputRef = useRef(null);
    const timestampsRef = useRef([]);
    const countdownRef = useRef(null);
    const isSendingRef = useRef(false);

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

    useEffect(() => {
        if (!lastMessage) return;

        if (lastMessage.envoyeurId === targetUserId || lastMessage.receveurId === targetUserId) {
            setMessages((prev) => [...prev, lastMessage]);
        }
    }, [lastMessage, targetUserId]);

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

        setRateLimitInfo(prev => ({ ...prev, blocked: true, secondsLeft }));

        countdownRef.current = setInterval(() => {
            setRateLimitInfo(prev => {
                const next = prev.secondsLeft - 1;
                if (next <= 0) {
                    clearInterval(countdownRef.current);
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

        timestampsRef.current = timestampsRef.current.filter(t => now - t < WINDOW_MS);

        if (timestampsRef.current.length >= MAX_MESSAGES) {
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

    const refundRateLimitSlot = useCallback(() => {
        if (timestampsRef.current.length === 0) return;

        timestampsRef.current.pop();
        const now = Date.now();
        timestampsRef.current = timestampsRef.current.filter(t => now - t < WINDOW_MS);
        setRateLimitInfo({
            blocked: false,
            remaining: MAX_MESSAGES - timestampsRef.current.length,
            secondsLeft: 0
        });
    }, []);

    const uploadSelectedFiles = useCallback(async () => {
        if (selectedFiles.length === 0) return [];

        return Promise.all(selectedFiles.map(async (file) => {
            const form = new FormData();
            form.append('file', file);
            form.append('nom', file.name);
            form.append('type', file.type);
            form.append('scope', 'message');

            const res = await fetch(`${getBaseUrl()}/upload`, {
                method: 'POST',
                credentials: 'include',
                body: form,
            });

            if (!res.ok) {
                const message = await res.text();
                throw new Error(message || `Upload failed for ${file.name}`);
            }

            const data = await res.json();
            return data.url;
        }));
    }, [selectedFiles]);

    const handleFileChange = useCallback((event) => {
        setSelectedFiles(Array.from(event.target.files ?? []));
    }, []);

    const handleSend = useCallback(async () => {
        const trimmedText = text.trim();
        const hasFiles = selectedFiles.length > 0;

        if (!trimmedText && !hasFiles) return;
        if (isSendingRef.current || isUploading) return;
        if (!checkRateLimit()) return;

        isSendingRef.current = true;
        setIsUploading(true);

        try {
            const mediaUrls = await uploadSelectedFiles();
            const sent = sendMessage(targetUserId, trimmedText, mediaUrls);

            if (!sent) {
                throw new Error('Chat connection is unavailable.');
            }

            setMessages((prev) => [...prev, {
                envoyeurId: "ME",
                contenu: trimmedText,
                media: mediaUrls,
                dateMsg: new Date().toISOString()
            }]);
            setText("");
            setSelectedFiles([]);
            setError("");

            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        } catch (err) {
            console.error("Failed to send message", err);
            refundRateLimitSlot();
            setError(err.message ?? 'Failed to send message.');
        } finally {
            setIsUploading(false);
            setTimeout(() => { isSendingRef.current = false; }, 100);
        }
    }, [text, selectedFiles, isUploading, checkRateLimit, uploadSelectedFiles, sendMessage, targetUserId, refundRateLimitSlot]);

    const buttonLabel = rateLimitInfo.blocked
        ? `${rateLimitInfo.secondsLeft}s`
        : isUploading
            ? 'UPLOADING'
            : 'ENVOYER';

    const counterLabel = rateLimitInfo.blocked
        ? `Limite de messages atteinte - reessayez dans ${rateLimitInfo.secondsLeft}s`
        : ` `;

    const counterClass = rateLimitInfo.blocked
        ? 'rate-limit-counter blocked'
        : rateLimitInfo.remaining <= 3
            ? 'rate-limit-counter warning'
            : 'rate-limit-counter';

    return (
        <div className="chat-container">
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
                            {m.contenu && (
                                <div className="message-text">{m.contenu}</div>
                            )}
                            {(m.media ?? []).length > 0 && (
                                <div className="message-attachments">
                                    {m.media.map((url) => (
                                        isImageAttachment(url) ? (
                                            <a key={url} href={url} target="_blank" rel="noreferrer" className="message-image-link">
                                                <img src={url} alt={getAttachmentLabel(url)} className="message-image" />
                                            </a>
                                        ) : (
                                            <a key={url} href={url} target="_blank" rel="noreferrer" className="message-file-link">
                                                {getAttachmentLabel(url)}
                                            </a>
                                        )
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                ))}
                <div ref={scrollRef} />
            </div>

            <div className={counterClass}>
                {counterLabel}
            </div>

            {selectedFiles.length > 0 && (
                <div className="selected-files">
                    {selectedFiles.map((file) => (
                        <span key={`${file.name}-${file.size}`} className="selected-file-chip">
                            {file.name}
                        </span>
                    ))}
                </div>
            )}

            <div className="entry-box">
                <button
                    type="button"
                    className="attach-button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={rateLimitInfo.blocked || isUploading}
                >
                    FILES
                </button>
                <input
                    ref={fileInputRef}
                    type="file"
                    multiple
                    accept={MESSAGE_FILE_ACCEPT}
                    className="chat-file-input"
                    onChange={handleFileChange}
                />
                <input
                    className="message-input"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                    placeholder={rateLimitInfo.blocked || rateLimitInfo.remaining === 0
                        ? "Limite atteinte..."
                        : isUploading
                            ? "Uploading files..."
                            : `Envoyer un message (${rateLimitInfo.remaining}/${MAX_MESSAGES})`}
                    disabled={rateLimitInfo.blocked || isUploading}
                />
                <button
                    className={`send-button ${rateLimitInfo.blocked || isUploading ? 'disabled' : ''}`}
                    onClick={handleSend}
                    disabled={rateLimitInfo.blocked || isUploading}
                >
                    {buttonLabel}
                </button>
            </div>
        </div>
    );
};

export default Chat;
