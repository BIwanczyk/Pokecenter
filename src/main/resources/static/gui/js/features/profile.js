import { formatPcLabel, normalizeEmail } from '../core/utils.js';
import { isOrganizer, isTrainer, requireLoggedIn } from '../core/roles.js';

export function initProfile(ctx) {
  const els = ctx.els;

  function openProfile() { ctx.setHidden(els.profileBackdrop, false); }
  function closeProfile() { ctx.setHidden(els.profileBackdrop, true); }

  els.profileClose?.addEventListener('click', closeProfile);

  async function refreshCurrentUser() {
    const me = ctx.getCurrentUser();
    if (!me || !me.email) return me;

    const email = normalizeEmail(me.email);

    if (isOrganizer(me)) {
      try {
        const list = await ctx.api('/event-organizers');
        const found = Array.isArray(list) ? list.find(o => normalizeEmail(o.email) === email) : null;
        if (found) {
          const updated = { ...found, _role: 'EventOrganizer' };
          ctx.setCurrentUser(updated);
          return updated;
        }
      } catch {}
      return me;
    }

    try {
      const list = await ctx.api('/trainers');
      const found = Array.isArray(list) ? list.find(t => normalizeEmail(t.email) === email) : null;
      if (found) {
        const updated = { ...found, _role: 'Trainer' };
        ctx.setCurrentUser(updated);
        return updated;
      }
    } catch {}

    return me;
  }

  els.btnProfile?.addEventListener('click', async () => {
    const me0 = requireLoggedIn(ctx);
    if (!me0) return;

    els.profileBody.innerHTML = '<div style="color:#94a3b8">Ładowanie…</div>';
    openProfile();

    const me = await refreshCurrentUser();
    const safe = (v, d = '—') => (v === null || v === undefined || v === '' ? d : v);

    const role = isOrganizer(me) ? 'Organizator eventów' : 'Trener';
    const hasIns = (me.hasInsurance === true || me.hasInsurance === 'true') ? 'tak' : 'nie';

    const pcLabel = (() => {
      const pc = me.pokecenter;
      if (pc && typeof pc === 'object') return formatPcLabel(pc);
      return '—';
    })();

    let registrations = [];
    if (isTrainer(me)) {
      try {
        const list = await ctx.api(`/registrations?trainerId=${encodeURIComponent(me.id)}`);
        registrations = Array.isArray(list) ? list : [];
        registrations.sort((a, b) => String(a.registrationDate || '').localeCompare(String(b.registrationDate || '')));
      } catch {
        registrations = [];
      }
    }

    const regSection = (() => {
      if (!isTrainer(me)) return '';

      const rows = registrations.length
        ? registrations.map(r => {
          const pc = r.pokecenterLabel || r.pokecenterName || (r.pokecenterId ? `Ośrodek #${r.pokecenterId}` : '—');
          const dt = r.registrationDate || '—';
          return `<tr><td>${pc}</td><td>${dt}</td></tr>`;
        }).join('')
        : `<tr><td colspan="2" style="color:#94a3b8">Brak rejestracji (kup ubezpieczenie w wybranym ośrodku).</td></tr>`;

      return `
        <h3 style="margin-top:14px">Rejestracje w ośrodkach</h3>
        <table class="table">
          <thead><tr><th>Pokecenter</th><th>Data</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
      `;
    })();

    els.profileBody.innerHTML = `
      <table class="table">
        <tr><th>Rola</th><td>${role}</td></tr>
        <tr><th>Imię</th><td>${safe(me.name)}</td></tr>
        <tr><th>Nazwisko</th><td>${safe(me.surname)}</td></tr>
        <tr><th>Email</th><td>${safe(me.email)}</td></tr>
        <tr><th>Wiek</th><td>${safe(me.age)}</td></tr>
        <tr><th>Telefon</th><td>${safe(me.phoneNumber)}</td></tr>
        <tr><th>Ubezpieczenie</th><td>${hasIns}</td></tr>
        <tr><th>Odznaki</th><td>${safe(me.badgeCount, 0)}</td></tr>
        ${isOrganizer(me) ? `<tr><th>Pokecenter</th><td>${pcLabel}</td></tr>` : ''}
      </table>
      ${regSection}
    `;
  });

  ctx.profile = { refreshCurrentUser, openProfile, closeProfile };
}
