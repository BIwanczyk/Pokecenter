import { normalizeEmail } from '../core/utils.js';

export function initAuth(ctx) {
  const els = ctx.els;

  els.btnGoLogin?.addEventListener('click', () => {
    ctx.setHidden(els.choiceBackdrop, true);
    ctx.setHidden(els.loginBackdrop, false);
    els.loginEmail?.focus();
  });

  els.btnGoRegister?.addEventListener('click', () => {
    ctx.setHidden(els.choiceBackdrop, true);
    ctx.setHidden(els.registerBackdrop, false);
    els.firstName?.focus();
  });

  els.loginBack?.addEventListener('click', () => {
    ctx.setHidden(els.loginBackdrop, true);
    ctx.setHidden(els.choiceBackdrop, false);
  });

  els.registerBack?.addEventListener('click', () => {
    ctx.setHidden(els.registerBackdrop, true);
    ctx.setHidden(els.choiceBackdrop, false);
  });

  els.globalLogout?.addEventListener('click', () => ctx.logoutToStart?.());

  //login
  els.loginForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = els.loginEmail?.value?.trim();
    if (!email) return alert('Podaj email.');

    const user = await findUserByEmail(ctx, email);
    if (!user) return alert('Brak konta o takim emailu.');

    ctx.setCurrentUser(user);
    await ctx.showApp();
  });

  els.registerForm?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const payload = {
      name: els.firstName?.value?.trim(),
      surname: els.lastName?.value?.trim(),
      email: els.email?.value?.trim(),
      age: Number(els.age?.value || 18),
      phoneNumber: '',
      hasInsurance: false,
      badgeCount: Number(els.badgeCount?.value || 0),
    };

    const rawPhone = (els.phone?.value ?? '').trim();
    if (rawPhone) {
      if (!/^[0-9\s-]+$/.test(rawPhone)) {
        return alert('zły format numeru telefonu');
      }
      const digits = rawPhone.replace(/\D/g, '');
      if (digits.length !== 9) {
        return alert('zły format numeru telefonu');
      }
      payload.phoneNumber = `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
    } else {
      payload.phoneNumber = '000-000-000';
    }

    if (!payload.name || !payload.surname || !payload.email) {
      return alert('Uzupełnij imię, nazwisko i email.');
    }
    if (!Number.isFinite(payload.age) || payload.age < 1) {
      return alert('Wiek musi być ≥ 1');
    }

    try {
      const created = await ctx.api('/trainers', { method: 'POST', body: JSON.stringify(payload) });
      ctx.setCurrentUser({ ...created, _role: 'Trainer' });
      await ctx.showApp();
    } catch (err) {
      alert('Nie udało się utworzyć trenera: ' + err.message);
    }
  });
}

async function findUserByEmail(ctx, email) {
  const em = normalizeEmail(email);
  if (!em) return null;

  try {
    const list = await ctx.api('/trainers');
    if (Array.isArray(list)) {
      const found = list.find(t => normalizeEmail(t.email) === em);
      if (found) return { ...found, _role: 'Trainer' };
    }
  } catch {}

  try {
    const list = await ctx.api('/event-organizers');
    if (Array.isArray(list)) {
      const found = list.find(o => normalizeEmail(o.email) === em);
      if (found) return { ...found, _role: 'EventOrganizer' };
    }
  } catch {}

  return null;
}
