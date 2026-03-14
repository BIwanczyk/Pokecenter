import { safeJsonParse } from './utils.js';

const KEY_USER = 'currentTrainer';
const KEY_PC = 'selectedPokecenterId';

export function getCurrentUser() {
  const raw = localStorage.getItem(KEY_USER);
  return raw ? safeJsonParse(raw, null) : null;
}

export function setCurrentUser(user) {
  localStorage.setItem(KEY_USER, JSON.stringify(user));
}

export function clearCurrentUser() {
  localStorage.removeItem(KEY_USER);
}

export function getSelectedPokecenterId() {
  const raw = localStorage.getItem(KEY_PC);
  const id = Number(raw || 0);
  return Number.isFinite(id) ? id : 0;
}

export function setSelectedPokecenterId(id) {
  const n = Number(id || 0);
  localStorage.setItem(KEY_PC, String(Number.isFinite(n) ? n : 0));
}
