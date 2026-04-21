import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { getBaseUrlWebsocket } from './API';
import { useAuth } from './AuthContext';

const ChatContext = createContext();

export const ChatProvider = ({ children }) => {
    const { user, loading } = useAuth();
    const [socket, setSocket] = useState(null);
    const [lastMessage, setLastMessage] = useState(null); // The most recent message received
    const [lastDeletedMessage, setLastDeletedMessage] = useState(null);
    const [lastActivity, setLastActivity] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const [isRateLimited, setIsRateLimited] = useState(false);

    const connect = useCallback(() => {
        if(user == null) return;
        // Pass token in query string since WS headers are limited in browser
        const ws = new WebSocket(`${getBaseUrlWebsocket()}/ws/chat`);

        ws.onopen = () => {
            console.log("Connected to Messaging Service");
            setIsConnected(true);
        };

        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);

            if (data.error === 'rate_limited') {
                setIsRateLimited(true);
                setTimeout(() => setIsRateLimited(false), 60000);
                console.log("limite de message atteinte");
                return;
            }

            if (data.type === 'message_deleted') {
                setLastDeletedMessage(data);
                return;
            }

            setIsRateLimited(false);
            setLastMessage(data);
            setLastActivity({ type: 'received', message: data, at: Date.now() });
        };

        ws.onclose = () => {
            setIsConnected(false);
            console.log("Disconnected!!! Retrying in 5s");
            setTimeout(connect, 5000);
        };

        setSocket(ws);
    }, [user]);

    useEffect(() => {
        if (loading) return;
        connect();
        return () => socket?.close();
    }, [connect, loading]);

    const sendMessage = (receiverId, content, media = []) => {
        if (!(socket && isConnected)) {
            return false;
        }

        socket.send(JSON.stringify({ receiverId, contenu: content, media }));
        setLastActivity({
            type: 'sent',
            message: {
                receiverId,
                contenu: content,
                media
            },
            at: Date.now()
        });
        return true;
    };

    return (
        <ChatContext.Provider value={{ isConnected, lastMessage, lastDeletedMessage, lastActivity, sendMessage, isRateLimited }}>
            {children}
        </ChatContext.Provider>
    );
};

export const useChat = () => useContext(ChatContext);
