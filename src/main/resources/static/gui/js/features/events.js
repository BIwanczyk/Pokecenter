import { formatPcLabel, safeJsonParse } from '../core/utils.js';
import { isOrganizer, isTrainer, requireLoggedIn } from '../core/roles.js';

export function initEvents(ctx) {
  const els = ctx.els;

  function openTournamentModal() { ctx.setHidden(els.tournamentBackdrop, false); }
  function closeTournamentModal() { ctx.setHidden(els.tournamentBackdrop, true); }

  els.btnTournament?.addEventListener('click', () => {
    const me = requireLoggedIn(ctx);
    if (!me) return;
    openTournamentModal();
  });
  els.tournamentClose?.addEventListener('click', closeTournamentModal);

  function normalizeEvent(ev, kind) {
    const pc = ev.pokecenter;
    const rawPcId = (ev.pokecenterId != null)
      ? ev.pokecenterId
      : ((pc && typeof pc === 'object') ? pc.id : pc);

    const pcId = rawPcId != null ? Number(rawPcId) : 0;

    return {
      id: Number(ev.id),
      kind,
      name: ev.eventName,
      date: ev.date,
      status: (ev.status || ev.eventStatus || 'CREATED'),
      maxParticipants: ev.maxParticipants,
      maxAudience: ev.maxAudience,
      pokecenterId: Number.isFinite(pcId) ? pcId : 0,
    };
  }

  function eventStatusLabel(status) {
    switch (String(status || '').toUpperCase()) {
      case 'CREATED': return 'Utworzone wydarzenie';
      case 'OPEN_FOR_REGISTRATION': return 'Otwarty na zapisy';
      case 'REGISTRATION_CLOSED': return 'Zamknięty na zapisy';
      case 'IN_PROGRESS': return 'W trakcie';
      case 'FINISHED': return 'Wydarzenie zakończone';
      case 'CANCELLED': return 'Anulowane';
      default: return status ? String(status) : '—';
    }
  }

  async function fetchEvents(kind) {
    const path = kind === 'private' ? '/private-events' : '/public-events';
    const list = await ctx.api(path);

    const normalized = Array.isArray(list) ? list.map(ev => normalizeEvent(ev, kind)) : [];

    //filtrowanie po wybranym ośrodku
    const selPc = ctx.getSelectedPokecenterId();
    const filtered = selPc ? normalized.filter(e => e.pokecenterId === selPc) : normalized;
    filtered.sort((a, b) => String(a.date || '').localeCompare(String(b.date || '')));
    return filtered;
  }

  const INSURANCE_EVENT_MSG = 'Kup ubezpieczenia aby brać udział w wydarzeniach Pokemon';
  let eventUnregisterCheckToken = 0;

  function setButtonBlocked(btn, blocked, message) {
    if (!btn) return;
    if (blocked) {
      btn.classList.add('is-blocked');
      btn.setAttribute('aria-disabled', 'true');
      btn.dataset.blockedMessage = message || '';
    } else {
      btn.classList.remove('is-blocked');
      btn.removeAttribute('aria-disabled');
      delete btn.dataset.blockedMessage;
    }
  }

  function updateEventRegisterAvailability() {
    if (!els.eventRegister) return;

    const hasSelection = !!els.eventSelect?.value;
    if (!hasSelection) {
      setButtonBlocked(els.eventRegister, false);
      els.eventRegister.disabled = true;
      return;
    }

    const me = ctx.getCurrentUser();
    const selOpt = els.eventSelect?.options?.[els.eventSelect.selectedIndex];
    const ev = selOpt?.dataset?.ev ? safeJsonParse(selOpt.dataset.ev, null) : null;
    const status = ev?.status;

    const statusOk = String(status || '').toUpperCase() === 'OPEN_FOR_REGISTRATION';
    if (!statusOk) {
      els.eventRegister.disabled = false;
      setButtonBlocked(els.eventRegister, true, 'Zapisy nie są otwarte dla tego wydarzenia.');
      return;
    }

    const blocked = !!me && isTrainer(me) && !me.hasInsurance;
    els.eventRegister.disabled = false;
    setButtonBlocked(els.eventRegister, blocked, INSURANCE_EVENT_MSG);
  }

  async function updateEventUnregisterAvailability() {
    if (!els.eventUnregister) return;

    const hasSelection = !!els.eventSelect?.value;
    if (!hasSelection) {
      setButtonBlocked(els.eventUnregister, false);
      els.eventUnregister.disabled = true;
      return;
    }

    const selOpt = els.eventSelect?.options?.[els.eventSelect.selectedIndex];
    const ev = selOpt?.dataset?.ev ? safeJsonParse(selOpt.dataset.ev, null) : null;
    const status = ev?.status;
    const statusOk = String(status || '').toUpperCase() === 'OPEN_FOR_REGISTRATION';

    if (!statusOk) {
      els.eventUnregister.disabled = false;
      setButtonBlocked(els.eventUnregister, true, 'Wypisywanie jest możliwe tylko gdy zapisy są otwarte.');
      return;
    }

    const me = ctx.getCurrentUser();
    if (!me || !isTrainer(me)) {
      setButtonBlocked(els.eventUnregister, false);
      els.eventUnregister.disabled = true;
      return;
    }

    const token = ++eventUnregisterCheckToken;

    setButtonBlocked(els.eventUnregister, false);
    els.eventUnregister.disabled = true;

    try {
      const list = await ctx.api(`/events/${selOpt.value}/participants`);
      if (token !== eventUnregisterCheckToken) return;

      const registered = Array.isArray(list) && list.some(p => String(p?.id) === String(me.id));
      els.eventUnregister.disabled = !registered;
    } catch {
      if (token !== eventUnregisterCheckToken) return;
      els.eventUnregister.disabled = false;
    }
  }

  //ładuje przyciski i wczytuje dane o eventach
  function openEventSelect(kind) {
    if (!els.eventSelectBackdrop) return;

    const me = ctx.getCurrentUser();
    const trainer = !!me && isTrainer(me);
    //ukrywanie przycisków w zależności od roli
    ctx.setHidden(els.eventRegister, !trainer);
    ctx.setHidden(els.eventUnregister, !trainer);

    //nagłówek
    els.eventSelectTitle.textContent = kind === 'private' ? 'Turnieje prywatne' : 'Turnieje publiczne';
    els.eventSelect.dataset.kind = kind;

    els.eventSelect.innerHTML = '<option disabled selected>Ładowanie…</option>';
    els.eventInfo.disabled = true;
    els.eventRegister.disabled = true;
    if (els.eventUnregister) els.eventUnregister.disabled = true;

    ctx.setHidden(els.eventSelectBackdrop, false);

    //get
    fetchEvents(kind).then(list => {
      //przygotowanie miejsca dla danych
      els.eventSelect.innerHTML = '';

      if (!list.length) {
        const opt = document.createElement('option');
        opt.disabled = true;
        opt.selected = true;
        opt.textContent = ctx.getSelectedPokecenterId() ? 'Brak eventów w wybranym ośrodku' : 'Brak dostępnych turniejów';
        els.eventSelect.appendChild(opt);

        els.eventInfo.disabled = true;
        els.eventRegister.disabled = true;
        if (els.eventUnregister) {
          setButtonBlocked(els.eventUnregister, false);
          els.eventUnregister.disabled = true;
        }
        return;
      }

      //znalazło eventy
      for (const ev of list) {
        const opt = document.createElement('option');
        opt.value = String(ev.id);

        //nazwa ośrodka przy evencie
        const pcLabel = ev.pokecenterId
          ? (formatPcLabel(ctx.pokecenters?.byId?.get(ev.pokecenterId)) || `Ośrodek #${ev.pokecenterId}`)
          : '—';

        const st = eventStatusLabel(ev.status);
        opt.textContent = `${ev.name} — ${ev.date || ''} (${pcLabel}) [${st}]`;
        opt.dataset.ev = JSON.stringify(ev);
        //koniec budowania listy
        els.eventSelect.appendChild(opt);
      }

      els.eventInfo.disabled = false;
      updateEventRegisterAvailability();
      updateEventUnregisterAvailability();
      //obsługa błedu ładowania
    }).catch(err => {
      els.eventSelect.innerHTML = '';
      const opt = document.createElement('option');
      opt.disabled = true;
      opt.selected = true;
      opt.textContent = 'Błąd wczytywania eventów';
      els.eventSelect.appendChild(opt);

      els.eventInfo.disabled = true;
      els.eventRegister.disabled = true;
      if (els.eventUnregister) {
        setButtonBlocked(els.eventUnregister, false);
        els.eventUnregister.disabled = true;
      }

      console.error(err);
    });
  }

  function closeEventSelect() { ctx.setHidden(els.eventSelectBackdrop, true); }
  els.eventSelectCancel?.addEventListener('click', closeEventSelect);

  els.btnTournamentPrivate?.addEventListener('click', () => { closeTournamentModal(); openEventSelect('private'); });
  els.btnTournamentPublic?.addEventListener('click', () => { closeTournamentModal(); openEventSelect('public'); });

  els.eventSelect?.addEventListener('change', () => {
    els.eventInfo.disabled = !els.eventSelect?.value;
    updateEventRegisterAvailability();
    updateEventUnregisterAvailability();
  });

  function openEventInfo() { ctx.setHidden(els.eventInfoBackdrop, false); }
  function closeEventInfo() { ctx.setHidden(els.eventInfoBackdrop, true); }
  els.eventInfoClose?.addEventListener('click', closeEventInfo);

  async function fetchEventDetails(kind, id) {
    const path = kind === 'private' ? `/private-events/${id}` : `/public-events/${id}`;
    return ctx.api(path);
  }

  async function resolvePokecenterLabelById(pcId) {
    if (!pcId) return '—';

    const cached = ctx.pokecenters?.byId?.get(Number(pcId));
    if (cached) return formatPcLabel(cached);

    try {
      const pc = await ctx.api(`/pokecenters/${pcId}`);
      if (pc && pc.id != null) ctx.pokecenters?.byId?.set(Number(pc.id), pc);
      return formatPcLabel(pc);
    } catch {
      return `Ośrodek #${pcId}`;
    }
  }

  els.eventInfo?.addEventListener('click', async () => {
    const sel = els.eventSelect?.options?.[els.eventSelect.selectedIndex];
    if (!sel || !sel.value) return alert('Wybierz turniej z listy.');

    const kind = els.eventSelect.dataset.kind || 'public';
    const id = Number(sel.value);

    const evHint = sel.dataset.ev ? safeJsonParse(sel.dataset.ev, null) : null;
    await renderEventInfoModal(kind, id, evHint);
  });

  //informacje o eventach
  async function renderEventInfoModal(kind, id, evHint) {
    els.eventInfoBody.innerHTML = '<div style="color:#94a3b8">Ładowanie…</div>';
    openEventInfo();

    let full = null;
    try { full = await fetchEventDetails(kind, id); } catch {}

    //składanie obiektu ev do renderowania
    const ev = full
      ? normalizeEvent(full, kind)
      : (evHint || { id, kind, name: '—', date: '—', status: 'CREATED', pokecenterId: 0 });

    //specyficzne pola dla rodzaju eventu
    if (full) {
      ev.maxAudience = full.maxAudience;
      ev.maxParticipants = full.maxParticipants;
    }

    // odśwież tekst w <select> po dociągnięciu pełnych danych (aspekt wizualny)
    try {
      const opt = [...(els.eventSelect?.options || [])].find(o => String(o.value) === String(id));
      if (opt) {
        opt.dataset.ev = JSON.stringify(ev);
        //bierze obiekt pokecenter z cache do nazwy
        const pcLabelShort = ev.pokecenterId
          ? (formatPcLabel(ctx.pokecenters?.byId?.get(ev.pokecenterId)) || `Ośrodek #${ev.pokecenterId}`)
          : '—';
        opt.textContent = `${ev.name} — ${ev.date || ''} (${pcLabelShort}) [${eventStatusLabel(ev.status)}]`;
      }
    } catch {}

    updateEventRegisterAvailability();
    updateEventUnregisterAvailability();

    const pcLabel = await resolvePokecenterLabelById(ev?.pokecenterId);

    //pobranie uczestników
    let participants = [];
    try { participants = await ctx.api(`/events/${id}/participants`); } catch { participants = []; }

    const rows = participants.length
      ? participants.map(p => `
          <tr>
            <td style="width:90px">${p.registrationNumber ?? '—'}</td>
            <td>${(p.name || '—')} ${(p.surname || '')}</td>
          </tr>
        `).join('')
      : `<tr><td colspan="2" style="color:#94a3b8">Brak zapisanych osób.</td></tr>`;

    const extra = kind === 'public'
      ? `<tr><th>Max widzów</th><td>${ev?.maxAudience ?? '—'}</td></tr>`
      : '';

    //sprawdza role
    const me = ctx.getCurrentUser();
    const isOrg = !!me && isOrganizer(me);
    const st = String(ev?.status || '').toUpperCase();

    const actions = (() => {
      if (!isOrg) return [];
      if (st === 'CREATED') {
        return [
          { action: 'open-registrations', label: 'Otwórz zapisy' },
          { action: 'cancel', label: 'Anuluj wydarzenie' },
        ];
      }
      if (st === 'OPEN_FOR_REGISTRATION') return [{ action: 'close-registrations', label: 'Zamknij zapisy' }];
      if (st === 'REGISTRATION_CLOSED') return [{ action: 'start', label: 'Rozpocznij wydarzenie' }];
      if (st === 'IN_PROGRESS') return [{ action: 'finish', label: 'Zakończ wydarzenie' }];
      return [];
    })();

    //html przycisków stanu
    const actionsHtml = actions.length
      ? `
        <h3 style="margin-top:14px">Zarządzanie stanem (organizator)</h3>
        <div class="actions" id="eventStateActions" style="justify-content:flex-end;flex-wrap:wrap">
          ${actions.map(a => `<button type="button" class="btn-primary" data-action="${a.action}">${a.label}</button>`).join('')}
        </div>
        <div style="margin-top:6px;color:var(--muted);font-size:12px">
          Aktualny stan: <strong>${eventStatusLabel(ev.status)}</strong>
        </div>
      `
      : '';

    //renderowanie całego modala
    els.eventInfoBody.innerHTML = `
      <table class="table">
        <tr><th>Data</th><td>${ev?.date ?? '—'}</td></tr>
        <tr><th>Rodzaj</th><td>${kind === 'private' ? 'Prywatny' : 'Publiczny'}</td></tr>
        <tr><th>Status</th><td>${eventStatusLabel(ev?.status)}</td></tr>
        <tr><th>Pokecenter</th><td>${pcLabel}</td></tr>
        <tr><th>Max uczestników</th><td>${ev?.maxParticipants ?? '—'}</td></tr>
        ${extra}
      </table>
      <h3 style="margin-top:14px">Osoby zapisane</h3>
      <table class="table">
        <thead><tr><th style="width:90px">Nr</th><th>Imię i nazwisko</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
      ${actionsHtml}
    `;

    //listener statmachine
    const actionsEl = document.getElementById('eventStateActions');
    if (actionsEl) {
      //szukanie kontenera na przyciski które mają data-action
      actionsEl.querySelectorAll('button[data-action]').forEach(btn => {
        btn.addEventListener('click', async () => {
          //odczytywanie akcji do wykoniani
          const action = btn.getAttribute('data-action');
          if (!action) return;

          const confirmMsg = action === 'cancel' ? 'Na pewno chcesz anulować wydarzenie?' : null;
          if (confirmMsg && !confirm(confirmMsg)) return;

          //przejście stanu
          try {
            await ctx.api(`/events/${id}/${action}`, { method: 'PUT' });
            await renderEventInfoModal(kind, id, ev);
          } catch (err) {
            alert('Nie udało się zmienić stanu: ' + err.message);
          }
        });
      });
    }
  }

  els.eventRegister?.addEventListener('click', async () => {
    const blockedMsg = els.eventRegister?.dataset?.blockedMessage;
    if (blockedMsg) {
      alert(blockedMsg);
      return;
    }

    const sel = els.eventSelect?.options?.[els.eventSelect.selectedIndex]; //numer wybranego eventu
    if (!sel || !sel.value) return alert('Wybierz turniej z listy.');

    const me = requireLoggedIn(ctx);
    if (!me) return;
    if (!isTrainer(me)) return alert('Zapisy na turniej są tylko dla trenera.');

    if (!me.hasInsurance) {
      alert(INSURANCE_EVENT_MSG);
      return;
    }

    try {
      const res = await ctx.api(`/events/${sel.value}/register`, {
        method: 'POST',
        body: JSON.stringify({ trainerId: me.id }),
      });

      if (res?.info === 'already-registered') {
        alert(`Jesteś już zapisany. Twój numer: ${res.registrationNumber}`);
      } else {
        alert(`Zapisano na turniej. \nTwój numer: ${res?.registrationNumber ?? '—'}`);
      }

      updateEventRegisterAvailability();
      updateEventUnregisterAvailability();

      if (els.eventInfoBackdrop && !els.eventInfoBackdrop.classList.contains('hidden')) {
        els.eventInfo?.click();
      }
    } catch (err) {
      const raw = String(err.message || '');
      const msg = raw.toLowerCase();
      if (msg.includes('insurance')) {
        alert(INSURANCE_EVENT_MSG);
        return;
      }
      if (msg.includes('limit uczestników')) {
        alert('Limit uczestników osiągnięty');
        return;
      }
      alert('Nie udało się zapisać: ' + raw);
    }
  });

  els.eventUnregister?.addEventListener('click', async () => {
    const blockedMsg = els.eventUnregister?.dataset?.blockedMessage;
    if (blockedMsg) {
      alert(blockedMsg);
      return;
    }

    const sel = els.eventSelect?.options?.[els.eventSelect.selectedIndex];
    if (!sel || !sel.value) return alert('Wybierz turniej z listy.');

    const me = requireLoggedIn(ctx);
    if (!me) return;
    if (!isTrainer(me)) return alert('Wypisywanie dotyczy tylko trenera.');
    if (!confirm('Na pewno chcesz się wypisać z tego wydarzenia?')) return;

    try {
      await ctx.api(`/events/${sel.value}/participants/${me.id}`, { method: 'DELETE' });
      alert('Wypisano z wydarzenia');

      if (els.eventInfoBackdrop && !els.eventInfoBackdrop.classList.contains('hidden')) {
        els.eventInfo?.click();
      }

      updateEventRegisterAvailability();
      updateEventUnregisterAvailability();
    } catch (err) {
      alert('Nie udało się wypisać: ' + err.message);
    }
  });

  ctx.events = { openEventSelect };
}
