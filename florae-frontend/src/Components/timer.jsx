import { useEffect, useRef, useState } from 'react';

/**
 * useTimer
 * @param {number} seconds - Countdown time in seconds.
 * @param {boolean} active - Should timer start immediately?
 * @returns {[remaining, isRunning, start, reset]}
 *   remaining: seconds left,
 *   isRunning: timer status,
 *   start: function to start timer,
 *   reset: function to reset timer
 */
export function useTimer(seconds, active = false) {
  const [remaining, setRemaining] = useState(seconds);
  const [isRunning, setIsRunning] = useState(active);
  const initialSeconds = useRef(seconds);

  // Start timer
  const start = () => {
    setRemaining(initialSeconds.current);
    setIsRunning(true);
  };
  // Reset timer
  const reset = () => {
    setRemaining(initialSeconds.current);
    setIsRunning(false);
  };

  useEffect(() => {
    if (!isRunning) return;
    if (remaining <= 0) {
      setIsRunning(false);
      return;
    }
    const id = setInterval(() => {
      setRemaining((sec) => sec - 1);
    }, 1000);
    return () => clearInterval(id);
  }, [isRunning, remaining]);

  return [remaining, isRunning, start, reset];
}

export function formatTime(secs) {
  const mm = String(Math.floor(secs / 60)).padStart(2, '0');
  const ss = String(secs % 60).padStart(2, '0');
  return `${mm}:${ss}`;
}
