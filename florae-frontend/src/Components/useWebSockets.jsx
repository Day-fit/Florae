import useWebSocket from 'react-use-websocket';

export function useFloraWebSocket({ onMessage }) {
  const { sendMessage, lastMessage } = useWebSocket('ws://localhost:8080/ws/floralink', {
    onOpen: () => console.log("Flora WebSocket Connected"),
    onMessage: (event) => {
      const data = JSON.parse(event.data);
      onMessage && onMessage(data);
    },
    onError: (event) => console.error("Flora WebSocket Error:", event),
    shouldReconnect: () => true,
  });

  return { sendMessage, lastMessage };
}