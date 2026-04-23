import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useChat } from '../ChatProvider';
import { useAuth } from '../AuthContext';
import { getBaseUrl } from '../API';
import plusIcon from '../assets/icons/plus.svg';
import sendIcon from '../assets/icons/like.svg';
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

function formatMessageTimestamp(dateValue) {
    if (!dateValue) return '';

    const normalizedValue =
        typeof dateValue === 'string' && !/[zZ]|[+\-]\d{2}:\d{2}$/.test(dateValue)
            ? `${dateValue}Z`
            : dateValue;

    const date = new Date(normalizedValue);
    if (Number.isNaN(date.getTime())) return '';

    const now = new Date();
    const isSameDay =
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth() &&
        date.getDate() === now.getDate();

    if (isSameDay) {
        return new Intl.DateTimeFormat('fr-CA', {
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    }

    return new Intl.DateTimeFormat('fr-CA', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

const Chat = ({ targetUserId }) => {
    const { user } = useAuth();
    const { lastMessage, lastDeletedMessage, sendMessage, isRateLimited: backendLimited } = useChat();
    const [messages, setMessages] = useState([]);
    const [text, setText] = useState('');
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [error, setError] = useState('');
    const [isUploading, setIsUploading] = useState(false);
    const [isSending, setIsSending] = useState(false);
    const [targetPseudo, setTargetPseudo] = useState('');
    const [rateLimitInfo, setRateLimitInfo] = useState({ blocked: false, remaining: MAX_MESSAGES, secondsLeft: 0 });
    const scrollRef = useRef();
    const fileInputRef = useRef(null);
    const timestampsRef = useRef([]);
    const countdownRef = useRef(null);
    const isSendingRef = useRef(false);

    const STORAGE_KEY = `chat_rate_limit_${targetUserId}`;
    const isBlocked = rateLimitInfo.blocked || backendLimited;
    const isBusy = isUploading || isSending;

    const isImageFile = (file) => {
        return file.type.startsWith('image/');
    };
    const createFilePreview = (file) => {
        return URL.createObjectURL(file);
    };

    useEffect(() => {
        const fetchHistory = async () => {
            try {
                setError('');
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
                console.error('Failed to fetch history', err);
                setMessages([]);
                setError(err.message ?? 'Failed to load discussion.');
            }
        };

        fetchHistory();
    }, [targetUserId]);

    useEffect(() => {
        const fetchTargetUser = async () => {
            try {
                const res = await fetch(`${getBaseUrl()}/user/${targetUserId}`, {
                    method: 'GET',
                    credentials: 'include'
                });

                if (!res.ok) {
                    setTargetPseudo('');
                    return;
                }

                const data = await res.json();
                setTargetPseudo(data?.pseudo ?? '');
            } catch {
                setTargetPseudo('');
            }
        };

        if (!targetUserId) {
            setTargetPseudo('');
            return;
        }

        fetchTargetUser();
    }, [targetUserId]);

    useEffect(() => {
        if (!lastMessage) return;

        if (lastMessage.envoyeurId === targetUserId || lastMessage.receveurId === targetUserId) {
            setMessages((prev) => {
                if (lastMessage.id && prev.some((message) => message.id === lastMessage.id)) {
                    return prev;
                }
                return [...prev, lastMessage];
            });
        }
    }, [lastMessage, targetUserId]);

    useEffect(() => {
        if (!lastDeletedMessage?.id) return;

        setMessages((prev) => prev.filter((message) => message.id !== lastDeletedMessage.id));
    }, [lastDeletedMessage]);

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
        const initialSeconds = Math.max(1, secondsLeft);

        setRateLimitInfo((prev) => ({ ...prev, blocked: true, secondsLeft: initialSeconds, remaining: 0 }));

        countdownRef.current = setInterval(() => {
            setRateLimitInfo((prev) => {
                const next = prev.secondsLeft - 1;
                if (next <= 0) {
                    clearInterval(countdownRef.current);
                    const now = Date.now();
                    timestampsRef.current = timestampsRef.current.filter((t) => now - t < WINDOW_MS);
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
        const valid = saved.filter((t) => now - t < WINDOW_MS);
        timestampsRef.current = valid;
        localStorage.setItem(STORAGE_KEY, JSON.stringify(valid));

        if (valid.length >= MAX_MESSAGES) {
            const oldest = valid[0];
            const secondsLeft = Math.max(1, Math.ceil((oldest + WINDOW_MS - now) / 1000));
            startCountdown(secondsLeft);
        } else {
            setRateLimitInfo({ blocked: false, remaining: MAX_MESSAGES - valid.length, secondsLeft: 0 });
        }
    }, [STORAGE_KEY, startCountdown]);

    const checkRateLimit = useCallback(() => {
        const now = Date.now();

        timestampsRef.current = timestampsRef.current.filter((t) => now - t < WINDOW_MS);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(timestampsRef.current));

        if (timestampsRef.current.length >= MAX_MESSAGES) {
            const oldest = timestampsRef.current[0];
            const secondsLeft = Math.max(1, Math.ceil((oldest + WINDOW_MS - now) / 1000));
            startCountdown(secondsLeft);
            return false;
        }

        timestampsRef.current.push(now);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(timestampsRef.current));
        const remaining = MAX_MESSAGES - timestampsRef.current.length;

        if (remaining <= 0) {
            const oldest = timestampsRef.current[0];
            const secondsLeft = Math.max(1, Math.ceil((oldest + WINDOW_MS - now) / 1000));
            startCountdown(secondsLeft);
            return true;
        }

        setRateLimitInfo({ blocked: false, remaining, secondsLeft: 0 });
        return true;
    }, [startCountdown, STORAGE_KEY]);

    const refundRateLimitSlot = useCallback(() => {
        if (countdownRef.current) {
            clearInterval(countdownRef.current);
            countdownRef.current = null;
        }
        if (timestampsRef.current.length === 0) return;

        timestampsRef.current.pop();
        const now = Date.now();
        timestampsRef.current = timestampsRef.current.filter((t) => now - t < WINDOW_MS);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(timestampsRef.current));
        setRateLimitInfo({
            blocked: false,
            remaining: MAX_MESSAGES - timestampsRef.current.length,
            secondsLeft: 0
        });
    }, [STORAGE_KEY]);

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

    const handleRemoveSelectedFile = useCallback((indexToRemove) => {
        if (isBusy) return;

        setSelectedFiles((prev) => {
            const next = prev.filter((_, index) => index !== indexToRemove);

            if (fileInputRef.current) {
                const dataTransfer = new DataTransfer();
                next.forEach((file) => dataTransfer.items.add(file));
                fileInputRef.current.files = dataTransfer.files;
            }

            return next;
        });
    }, [isBusy]);

    const handleSend = useCallback(async () => {
        const trimmedText = text.trim();
        const hasFiles = selectedFiles.length > 0;

        if (!trimmedText && !hasFiles) return;
        if (isSendingRef.current || isBusy) return;
        if (!checkRateLimit()) return;

        isSendingRef.current = true;
        setIsSending(true);
        setIsUploading(true);

        try {
            const mediaUrls = await uploadSelectedFiles();
            const sent = sendMessage(targetUserId, trimmedText, mediaUrls);

            if (!sent) {
                throw new Error('Chat connection is unavailable.');
            }

            setText('');
            setSelectedFiles([]);
            setError('');

            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        } catch (err) {
            console.error('Failed to send message', err);
            refundRateLimitSlot();
            setError(err.message ?? 'Failed to send message.');
        } finally {
            isSendingRef.current = false;
            setIsSending(false);
            setIsUploading(false);
        }
    }, [text, selectedFiles, isBusy, checkRateLimit, uploadSelectedFiles, sendMessage, targetUserId, refundRateLimitSlot]);

    const handleDeleteMessage = useCallback(async (messageId) => {
        if (!messageId) return;

        try {
            setError('');
            const res = await fetch(`${getBaseUrl()}/chat/message/${messageId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (!res.ok) {
                const message = await res.text();
                throw new Error(message || 'Failed to delete message.');
            }

            setMessages((prev) => prev.filter((message) => message.id !== messageId));
        } catch (err) {
            console.error('Failed to delete message', err);
            setError(err.message ?? 'Failed to delete message.');
        }
    }, []);

    const counterLabel = rateLimitInfo.blocked
        ? `Limite de messages atteinte - reessayez dans ${rateLimitInfo.secondsLeft}s`
        : ' ';

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
                        key={m.id ?? `${m.envoyeurId}-${m.dateMsg}-${i}`}
                        className={`message-wrapper ${m.envoyeurId === targetUserId ? 'incoming' : 'outgoing'}`}
                    >
                        {m.envoyeurId === user?.userId && m.id && (
                            <button
                                type="button"
                                className="message-delete-button"
                                onClick={() => handleDeleteMessage(m.id)}
                                aria-label="Delete message"
                            >
                                ×
                            </button>
                        )}
                        <div className="message-bubble">
                            <small className="message-sender">
                                {m.envoyeurId === targetUserId ? targetPseudo : ''}
                            </small>
                            {m.contenu && !isImageAttachment(m.contenu) && (
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
                            {m.contenu && isImageAttachment(m.contenu) && (
                                <div className="message-attachments">
                                    <a href={m.contenu} target="_blank" rel="noreferrer" className="message-image-link">
                                        <img src={m.contenu} alt={getAttachmentLabel(m.contenu)} className="message-image" />
                                    </a>
                                </div>
                            )}
                            {m.dateMsg && (
                                <div className="message-timestamp">
                                    {formatMessageTimestamp(m.dateMsg)}
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
            {selectedFiles.map((file, index) => (
                <div key={`${file.name}-${file.size}`} className="file-preview-card">
                    {isImageFile(file) ? (
                        <div className="image-preview-wrapper">
                            <img 
                                src={createFilePreview(file)} 
                                alt={file.name}
                                className="preview-image"
                            />
                            <button
                                type="button"
                                className="preview-remove-btn"
                                onClick={() => handleRemoveSelectedFile(index)}
                                disabled={isBusy}
                                aria-label={`Remove ${file.name}`}
                            >
                                ×
                            </button>
                        </div>
                    ):(
                        <div className="file-chip-preview">
                            <div className="file-chip-icon">FILE</div>
                            <div className="file-chip-meta">
                                <span className="preview-file-name">{file.name}</span>
                                <span className="preview-file-size">
                                    {Math.max(1, Math.round(file.size / 1024))} KB
                                </span>
                            </div>
                            <button
                                type="button"
                                className="preview-remove-btn file-chip-remove"
                                onClick={() => handleRemoveSelectedFile(index)}
                                disabled={isBusy}
                                aria-label={`Remove ${file.name}`}
                            >
                                Ã—
                            </button>
                        </div>
                    )}
                    {isImageFile(file) && (
                        <div className="preview-file-caption">
                            {file.name}
                        </div>
                    )}
                </div>
            ))}
                </div>
            )}

            <div className="entry-box">
                <input
                    ref={fileInputRef}
                    type="file"
                    multiple
                    accept={MESSAGE_FILE_ACCEPT}
                    className="chat-file-input"
                    onChange={handleFileChange}
                />
                <div className="message-input-shell">
                    <button
                        type="button"
                        className="attach-button"
                        onClick={() => fileInputRef.current?.click()}
                        disabled={rateLimitInfo.blocked || isBusy}
                        aria-label="Attach files"
                    >
                        <img src={plusIcon} alt="" className="entry-action-icon" />
                    </button>
                    <input
                        className="message-input"
                        value={text}
                        onChange={(e) => setText(e.target.value)}
                        disabled={isBlocked || isBusy}
                        placeholder={isBlocked ? 'Limite atteinte...' : 'Envoyer un message'}
                        onKeyDown={(e) => { if (e.key === 'Enter' && !e.repeat && !isBlocked && !isBusy) handleSend(); }}
                    />
                </div>
                <button
                    className={`send-button ${isBlocked || isBusy ? 'disabled' : ''}`}
                    onClick={handleSend}
                    disabled={isBlocked || isBusy}
                    aria-label="Send message"
                >
                    <img src={sendIcon} alt="" className="entry-action-icon send-icon" />
                </button>
            </div>
        </div>
    );
};

export default Chat;
