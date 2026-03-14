export function setHidden(el, hidden) {
  if (!el) return;
  hidden ? el.classList.add('hidden') : el.classList.remove('hidden');
}

export function safeJsonParse(str, fallback = null) {
  try { return JSON.parse(str); } catch { return fallback; }
}

export function normalizeEmail(s) {
  return String(s || '').trim().toLowerCase();
}

export function formatPcLabel(pc) {
  if (!pc) return '—';
  return pc.location || pc.name || `Ośrodek #${pc.id ?? ''}`;
}
