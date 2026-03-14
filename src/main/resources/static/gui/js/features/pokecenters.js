import { formatPcLabel } from '../core/utils.js';

export function initPokecenters(ctx) {
  const els = ctx.els;

  let pokecenters = [];
  const pokecenterById = new Map();
  let lastPokecenterLoadError = null;

  const pcHeaderTitleEl = document.querySelector('.sidebar .header h1');
  const pcCountEl = els.countPc;

  let mainPcLabelEl = null;
  function ensureMainPcLabelEl() {
    if (mainPcLabelEl) return mainPcLabelEl;
    const title = document.querySelector('.menu-stack h1');
    if (!title) return null;

    let el = document.getElementById('selectedPcMain');
    if (!el) {
      el = document.createElement('div');
      el.id = 'selectedPcMain';
      el.style.color = 'var(--muted)';
      el.style.fontSize = '16px';
      el.style.fontWeight = '600';
      el.style.margin = '-10px 0 18px';
      el.style.textAlign = 'center';
      title.insertAdjacentElement('afterend', el);
    }
    mainPcLabelEl = el;
    return el;
  }

  function updateSelectedPcLabel() {
    const selectedId = ctx.getSelectedPokecenterId();
    const pc = selectedId ? pokecenterById.get(Number(selectedId)) : null;
    const label = pc ? formatPcLabel(pc) : (selectedId ? `#${selectedId}` : '(nie wybrano)');

    if (pcHeaderTitleEl) pcHeaderTitleEl.textContent = `Wybrany ośrodek - ${label}`;

    // usuń licznik dla czytelności
    if (pcCountEl) {
      pcCountEl.textContent = '';
      pcCountEl.style.display = 'none';
    }

    if (els.pcChooseOpen) {
      els.pcChooseOpen.textContent = selectedId ? 'Zmień ośrodek' : 'Wybierz ośrodek';
    }

    const mainEl = ensureMainPcLabelEl();
    if (mainEl) mainEl.textContent = `Wybrany ośrodek: ${label}`;
  }

  async function loadPokecenters() {
    try {
      const list = await ctx.api('/pokecenters');
      pokecenters = Array.isArray(list) ? list : [];
      lastPokecenterLoadError = null;
    } catch (err) {
      lastPokecenterLoadError = err;
      pokecenters = [];
    }

    pokecenterById.clear();
    for (const pc of pokecenters) pokecenterById.set(Number(pc.id), pc);

    const storedId = ctx.getSelectedPokecenterId();
    if (storedId && !pokecenterById.has(Number(storedId))) {
      ctx.setSelectedPokecenterId(0);
    }

    // auto-wybór jeśli jest tylko 1
    if (!ctx.getSelectedPokecenterId() && pokecenters.length === 1) {
      ctx.setSelectedPokecenterId(Number(pokecenters[0].id));
    }

    if (els.countPc) els.countPc.textContent = pokecenters.length ? `(${pokecenters.length})` : '';
    renderPcList();
  }

  function renderPcList() {
    if (!els.pcList) return;

    const q = (els.searchPc?.value || '').trim().toLowerCase();
    const selectedId = ctx.getSelectedPokecenterId();
    els.pcList.innerHTML = '';

    if (!pokecenters.length && lastPokecenterLoadError) {
      const div = document.createElement('div');
      div.className = 'item';
      const msg = String(lastPokecenterLoadError?.message || lastPokecenterLoadError || 'Nieznany błąd');
      div.innerHTML = `
        <div class="title">Nie udało się pobrać listy ośrodków</div>
        <div class="sub">API: ${ctx.API_BASE}</div>
        <div class="sub">Błąd: ${msg}</div>
        <div class="sub">Tip: uruchamiaj GUI przez Spring Boot.</div>
      `;
      els.pcList.appendChild(div);
      updateSelectedPcLabel();
      return;
    }

    const filtered = pokecenters.filter(pc => {
      if (!q) return true;
      const hay = `${pc.location || ''} ${pc.email || ''} ${pc.phoneNumber || ''}`.toLowerCase();
      return hay.includes(q);
    });

    for (const pc of filtered) {
      const div = document.createElement('div');
      div.className = 'item' + (Number(pc.id) === selectedId ? ' active' : '');
      div.dataset.id = String(pc.id);
      div.innerHTML = `
        <div class="title">${formatPcLabel(pc)}</div>
        <div class="sub">id ${pc.id}</div>
      `;
      div.addEventListener('click', () => {
        ctx.setSelectedPokecenterId(pc.id);
        renderPcList();

        // jeśli select z eventami jest otwarty – odśwież listę po zmianie ośrodka
        if (els.eventSelectBackdrop && !els.eventSelectBackdrop.classList.contains('hidden')) {
          const kind = els.eventSelect?.dataset?.kind || 'public';
          ctx.events?.openEventSelect?.(kind);
        }
      });
      els.pcList.appendChild(div);
    }

    updateSelectedPcLabel();
  }

  function populatePcSelect() {
    if (!els.pcSelect) return;
    els.pcSelect.innerHTML = '';

    if (!pokecenters.length) {
      const opt = document.createElement('option');
      opt.disabled = true;
      opt.selected = true;
      opt.textContent = 'Brak ośrodków';
      els.pcSelect.appendChild(opt);
      return;
    }

    for (const pc of pokecenters) {
      const opt = document.createElement('option');
      opt.value = String(pc.id);
      opt.textContent = `${formatPcLabel(pc)} — id ${pc.id}`;
      els.pcSelect.appendChild(opt);
    }

    const selectedId = ctx.getSelectedPokecenterId();
    if (selectedId) els.pcSelect.value = String(selectedId);
  }

  function openPcSelectModal() {
    populatePcSelect();
    updateSelectedPcLabel();
    ctx.setHidden(els.pcSelectBackdrop, false);
  }

  // listeners
  els.searchPc?.addEventListener('input', renderPcList);

  //wyświetlanie ośrodków do wybrania
  els.pcChooseOpen?.addEventListener('click', () => {
    populatePcSelect();
    updateSelectedPcLabel();
    ctx.setHidden(els.pcSelectBackdrop, false);
  });

  els.pcSelectCancel?.addEventListener('click', () => ctx.setHidden(els.pcSelectBackdrop, true));

  //wybór danego ośrodka
  els.pcSelectChoose?.addEventListener('click', () => {
    if (!els.pcSelect?.value) return;
    ctx.setSelectedPokecenterId(Number(els.pcSelect.value));
    ctx.setHidden(els.pcSelectBackdrop, true);
    renderPcList();
  });

  //wystawienie funkcji na zwenątrz żeby np. main.js je widizał
  ctx.pokecenters = {
    loadPokecenters,
    renderPcList,
    updateSelectedPcLabel,
    populatePcSelect,
    openPcSelectModal,

    getAll: () => pokecenters.slice(),
    getById: (id) => pokecenterById.get(Number(id)) || null,
    byId: pokecenterById,
  };
}
