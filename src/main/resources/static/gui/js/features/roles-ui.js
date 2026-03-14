import { normalizeEmail } from '../core/utils.js';
import { isOrganizer, isTrainer } from '../core/roles.js';

export function initRolesUi(ctx) {
  async function isEmailOrganizer(email) {
    if (!email) return false;
    try {
      const list = await ctx.api('/event-organizers');
      return Array.isArray(list) && list.some(o => normalizeEmail(o.email) === normalizeEmail(email));
    } catch {
      return false;
    }
  }

  async function applyRoleUi() {
    const me = ctx.getCurrentUser();
    if (!me) return;

    if (!me._role && me.email) {
      me._role = (await isEmailOrganizer(me.email)) ? 'EventOrganizer' : 'Trainer';
      ctx.setCurrentUser(me);
    }

    const canCreate = isOrganizer(me);
    if (ctx.els.btnCreateEvent) {
      ctx.els.btnCreateEvent.title = canCreate ? '' : 'Brak dostępu. Musisz być organizatorem';
    }

    //podmiana przycisków w zależności od roli
    if (ctx.els.btnTournament) {
      ctx.els.btnTournament.textContent = isTrainer(me) ? 'Zapisz na turniej' : 'Turnieje';
      ctx.els.btnTournament.title = isTrainer(me)
        ? ''
        : 'Przeglądaj turnieje i zarządzaj ich stanem (Informacje)';
    }
  }

  ctx.rolesUi = { applyRoleUi };
}
