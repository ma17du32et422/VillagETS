import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { getBaseUrlWebsocket } from './API';
import { useAuth } from './AuthContext';

const ChatContext = createContext();

export const ChatProvider = ({ children }) => {
    const { user, loading } = useAuth();
    const [socket, setSocket] = useState(null);
    const [lastMessage, setLastMessage] = useState(null); // The most recent message received
    const [isConnected, setIsConnected] = useState(false);

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
            console.log(data);
            setLastMessage(data);
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

    const sendMessage = (receiverId, content) => {
        if (socket && isConnected) {
            socket.send(JSON.stringify({ receiverId: receiverId, contenu: content }));
        }
    };

    return (
        <ChatContext.Provider value={{ isConnected, lastMessage, sendMessage }}>
            {children}
        </ChatContext.Provider>
    );
};

export const useChat = () => useContext(ChatContext);