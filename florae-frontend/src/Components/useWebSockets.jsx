import useWebSocket from 'react-use-websocket';

export function useFloraWebSocket({ onMessage }) {
  useWebSocket('/ws/fanout', {
    onOpen: () => console.log('Flora WebSocket Connected'),
    onMessage: (event) => {
      const data = JSON.parse(event.data);
      onMessage && onMessage(data);
    },
    onError: (event) => console.error('Flora WebSocket Error:', event),
    shouldReconnect: () => true,
  });
}
