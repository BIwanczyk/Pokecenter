const KEY_BGM = 'bgmAllowed';

export function initBgm({ els }) {
  const audio = els?.bgm;
  if (!audio) return;

  try { audio.volume = 0.03; } catch {}

  const tryPlay = () => {
    audio.muted = false;
    const p = audio.play();
    if (p && typeof p.then === 'function') {
      p.then(() => localStorage.setItem(KEY_BGM, '1')).catch(() => {});
    }
  };

  tryPlay();

  const onInteract = () => {
    tryPlay();
    window.removeEventListener('pointerdown', onInteract);
    window.removeEventListener('keydown', onInteract);
    window.removeEventListener('touchstart', onInteract);
  };

  window.addEventListener('pointerdown', onInteract, { once: true });
  window.addEventListener('keydown', onInteract, { once: true });
  window.addEventListener('touchstart', onInteract, { once: true });

  if (localStorage.getItem(KEY_BGM) === '1') {
    setTimeout(tryPlay, 250);
  }
}
