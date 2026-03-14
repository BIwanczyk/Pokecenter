import { formatPcLabel } from '../core/utils.js';
import { isOrganizer, requireLoggedIn } from '../core/roles.js';

export function initCreateEvent(ctx) {
  const els = ctx.els;

  function showCreateStep(step) {
    els.ceStepChoice?.classList.toggle('hidden', step !== 'choice');
    els.ceStepPublic?.classList.toggle('hidden', step !== 'public');
    els.ceStepPrivate?.classList.toggle('hidden', step !== 'private');
  }

  function openCreateEvent() {
    const me = requireLoggedIn(ctx);
    if (!me) return;
    if (!isOrganizer(me)) return alert('Brak dostępu. Musisz być organizatorem.');

    showCreateStep('choice');
    ctx.setHidden(els.createEventBackdrop, false);
  }

  function closeCreateEvent() {
    ctx.setHidden(els.createEventBackdrop, true);
    showCreateStep('choice');
  }

  function fillPokecenterSelect(selectEl) {
    if (!selectEl) return;

    //pobiera listę ośrodków z pamięci
    const pokecenters = ctx.pokecenters?.getAll?.() || [];
    selectEl.innerHTML = '<option value="" disabled selected>Wybierz pokecenter…</option>';

    for (const pc of pokecenters) {
      const opt = document.createElement('option');
      opt.value = String(pc.id);
      opt.textContent = formatPcLabel(pc);
      selectEl.appendChild(opt);
    }

    const selected = ctx.getSelectedPokecenterId();
    if (selected) selectEl.value = String(selected);
  }

  els.btnCreateEvent?.addEventListener('click', openCreateEvent);
  els.ceChoiceCancel?.addEventListener('click', closeCreateEvent);
  els.ceBackFromPublic?.addEventListener('click', () => showCreateStep('choice'));
  els.ceBackFromPrivate?.addEventListener('click', () => showCreateStep('choice'));

  els.ceChoosePublic?.addEventListener('click', () => {
    fillPokecenterSelect(els.cePubPC);
    showCreateStep('public');
  });

  els.ceChoosePrivate?.addEventListener('click', () => {
    fillPokecenterSelect(els.cePrivPC);
    showCreateStep('private');
  });

  // Tworzenie eventu publicznego
  els.ceStepPublic?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const me = requireLoggedIn(ctx);
    if (!me) return;
    if (!isOrganizer(me)) return alert('Brak dostępu. Musisz być organizatorem.');

    const payload = {
      eventName: (els.cePubName?.value || '').trim(),
      date: els.cePubDate?.value,
      maxParticipants: Number(els.cePubMaxP?.value || 0),
      maxAudience: Number(els.cePubAudience?.value || 0),
      pokecenterId: els.cePubPC?.value ? Number(els.cePubPC.value) : null,
    };

    if (!payload.eventName || !payload.date || !payload.pokecenterId) {
      return alert('Uzupełnij nazwę, datę i wybierz pokecenter.');
    }
    if (!Number.isFinite(payload.maxParticipants) || payload.maxParticipants < 1) {
      return alert('Ilość uczestników musi być ≥ 1');
    }
    if (!Number.isFinite(payload.maxAudience) || payload.maxAudience < 0) {
      return alert('Ilość widzów nie może być ujemna');
    }

    try {
      await ctx.api('/public-events', { method: 'POST', body: JSON.stringify(payload) });
      closeCreateEvent();
      alert('Utworzono event publiczny ');

      if (els.eventSelectBackdrop && !els.eventSelectBackdrop.classList.contains('hidden')) {
        ctx.events?.openEventSelect?.(els.eventSelect?.dataset?.kind || 'public');
      }
    } catch (err) {
      alert('Nie udało się utworzyć eventu publicznego: ' + err.message);
    }
  });

  // Tworzenie eventu prywatnego
  els.ceStepPrivate?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const me = requireLoggedIn(ctx);
    if (!me) return;
    if (!isOrganizer(me)) return alert('Brak dostępu. Musisz być organizatorem.');

    const payload = {
      eventName: (els.cePrivName?.value || '').trim(),
      date: els.cePrivDate?.value,
      maxParticipants: Number(els.cePrivMaxP?.value || 0),
      pokecenterId: els.cePrivPC?.value ? Number(els.cePrivPC.value) : null,
    };

    if (!payload.eventName || !payload.date || !payload.pokecenterId) {
      return alert('Uzupełnij nazwę, datę i wybierz pokecenter.');
    }
    if (!Number.isFinite(payload.maxParticipants) || payload.maxParticipants < 1) {
      return alert('Ilość uczestników musi być ≥ 1');
    }

    try {
      await ctx.api('/private-events', { method: 'POST', body: JSON.stringify(payload) });
      closeCreateEvent();
      alert('Utworzono event prywatny ');

      if (els.eventSelectBackdrop && !els.eventSelectBackdrop.classList.contains('hidden')) {
        ctx.events?.openEventSelect?.(els.eventSelect?.dataset?.kind || 'private');
      }
    } catch (err) {
      alert('Nie udało się utworzyć eventu prywatnego: ' + err.message);
    }
  });
}
