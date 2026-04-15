import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { getBaseUrlWebsocket } from './API';
import { useAuth } from './AuthContext';

const ChatContext = createContext();

export const ChatProvider = ({ children }) => {
    const { user } = useAuth();
    const [socket, setSocket] = useState(null);
    const [lastMessage, setLastMessage] = useState(null); // The most recent message received
    const [isConnected, setIsConnected] = useState(false);

    const connect = useCallback(() => {
        console.log("[Check 2] connect() function triggered");
        //if(user == null) return;
console.log("[Check 2] connect() function triggered");
        // Pass token in query string since WS headers are limited in browser
        const ws = new WebSocket(`${getBaseUrlWebsocket()}/ws/chat`);

        ws.onopen = () => {
            console.log("Connected to Messaging Service");
            setIsConnected(true);
        };

        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            setLastMessage(data); // Update global state
        };

        ws.onclose = () => {
            setIsConnected(false);
            console.log("Disconnected. Retrying in 5s...");
            setTimeout(connect, 5000); // Simple auto-reconnect
        };

        setSocket(ws);
    }, []);

    useEffect(() => {
        connect();
        return () => socket?.close();
    }, [connect]);

    const sendMessage = (receiverId, content) => {
        if (socket && isConnected) {
            socket.send(JSON.stringify({ ReceiverId: receiverId, Content: content }));
        }
    };

    return (
        <ChatContext.Provider value={{ isConnected, lastMessage, sendMessage }}>
            {children}
        </ChatContext.Provider>
    );
};

export const useChat = () => useContext(ChatContext);