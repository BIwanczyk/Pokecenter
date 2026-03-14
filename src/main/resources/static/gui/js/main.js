import { createEls } from './core/dom.js';
import { createApi } from './core/api.js';
import {
  getCurrentUser,
  setCurrentUser,
  clearCurrentUser,
  getSelectedPokecenterId,
  setSelectedPokecenterId,
} from './core/storage.js';
import { initBgm } from './core/bgm.js';
import { setHidden } from './core/utils.js';

import { initAuth } from './features/auth.js';
import { initPokecenters } from './features/pokecenters.js';
import { initRolesUi } from './features/roles-ui.js';
import { initProfile } from './features/profile.js';
import { initInsurance } from './features/insurance.js';
import { initEvents } from './features/events.js';
import { initCreateEvent } from './features/create-event.js';
import { initShop } from './features/shop.js';

export function bootstrap() {
  const API_BASE = window.APP_CONFIG?.API_BASE || window.location.origin;
  const els = createEls();

  // Wymuszenie type="button" w modalu ubezpieczenia
  try { els.insuranceYes?.setAttribute('type', 'button'); } catch {}
  try { els.insuranceNo?.setAttribute('type', 'button'); } catch {}

  initBgm({ els });

  const api = createApi({ API_BASE });

  const ctx = {
    API_BASE,
    els,
    api,

    // storage
    getCurrentUser,
    setCurrentUser,
    clearCurrentUser,
    getSelectedPokecenterId,
    setSelectedPokecenterId,

    //ui
    setHidden,

    // app flow
    showApp: null,
    logoutToStart: null,

    // moduły
    pokecenters: null,
    rolesUi: null,
    profile: null,
    events: null,
  };

  ctx.showApp = () => showApp(ctx);
  ctx.logoutToStart = () => {
    ctx.clearCurrentUser();
    ctx.setHidden(ctx.els.appRoot, true);
    ctx.setHidden(ctx.els.choiceBackdrop, false);
  };

  initAuth(ctx);
  initPokecenters(ctx);
  initRolesUi(ctx);
  initProfile(ctx);
  initInsurance(ctx);
  initEvents(ctx);
  initCreateEvent(ctx);
  initShop(ctx);

  //Boot screen
  (async function init() {
    ctx.pokecenters?.updateSelectedPcLabel?.();

    const me = ctx.getCurrentUser();
    if (me && me.id) {
      await ctx.showApp();
    } else {
      ctx.setHidden(ctx.els.choiceBackdrop, false);
      ctx.setHidden(ctx.els.appRoot, true);
    }
  })();
}

export async function showApp(ctx) {
  ctx.setHidden(ctx.els.choiceBackdrop, true);
  ctx.setHidden(ctx.els.loginBackdrop, true);
  ctx.setHidden(ctx.els.registerBackdrop, true);
  ctx.setHidden(ctx.els.appRoot, false);

  await ctx.pokecenters.loadPokecenters();
  //gdy user nie ma roli
  await ctx.rolesUi.applyRoleUi();
}
