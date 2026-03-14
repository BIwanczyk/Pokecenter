 import { formatPcLabel } from '../core/utils.js';
import { isTrainer, requireLoggedIn } from '../core/roles.js';

export function initInsurance(ctx) {
  const els = ctx.els;

  let pendingInsurancePcId = 0;
  let pendingInsurancePcLabel = '';

  function openInsuranceModal() {
    const me = requireLoggedIn(ctx);
    if (!me) return;
    if (!isTrainer(me)) return alert('Ta akcja dotyczy tylko trenera.');
    if (me.hasInsurance) return alert('Masz już aktywne ubezpieczenie.');

    const pcId = ctx.getSelectedPokecenterId();
    if (!pcId) {
      //odpala okienko wyboru pokecenter
      alert('Najpierw wybierz ośrodek, w którym kupujesz ubezpieczenie.');
      ctx.pokecenters?.openPcSelectModal?.();
      return;
    }

    const pc = ctx.pokecenters?.getById?.(pcId);
    const label = pc ? formatPcLabel(pc) : `Ośrodek #${pcId}`;

    pendingInsurancePcId = Number(pcId);
    pendingInsurancePcLabel = label;

    //wstawienie nazwy ośrodka do modala
    if (els.insuranceConfirmPcName) els.insuranceConfirmPcName.textContent = label;
    if (els.insuranceConfirmText) {
      els.insuranceConfirmText.textContent = 'Czy chcesz aktywować ubezpieczenie dla swojego konta trenerskiego?';
    }

    ctx.setHidden(els.insuranceConfirmBackdrop, false);
  }

  function closeInsuranceModal() {
    pendingInsurancePcId = 0;
    pendingInsurancePcLabel = '';
    ctx.setHidden(els.insuranceConfirmBackdrop, true);
  }

  els.btnBuyInsurance?.addEventListener('click', openInsuranceModal);
  els.insuranceNo?.addEventListener('click', closeInsuranceModal);

  //zaakceptowanie kupna ubezpieczenia
  els.insuranceYes?.addEventListener('click', async (e) => {
    e?.preventDefault?.();
    e?.stopPropagation?.();

    const me = requireLoggedIn(ctx);
    if (!me) return;
    if (!isTrainer(me)) { closeInsuranceModal(); return alert('Ta akcja dotyczy tylko trenera.'); }
    if (me.hasInsurance) { closeInsuranceModal(); return alert('Masz już aktywne ubezpieczenie.'); }

    const pcId = pendingInsurancePcId;
    if (!pcId) {
      closeInsuranceModal();
      alert('Brak wybranego ośrodka. Wybierz ośrodek i spróbuj ponownie.');
      ctx.pokecenters?.openPcSelectModal?.();
      return;
    }

    const withPc = `?pokecenterId=${encodeURIComponent(pcId)}`;

    try {
      await ctx.api(`/trainers/${me.id}/insurance/purchase${withPc}`, { method: 'POST' });

      me.hasInsurance = true;
      me._role = 'Trainer';
      ctx.setCurrentUser(me);
      //generowanie GETA
      await ctx.profile?.refreshCurrentUser?.();

      const where = pendingInsurancePcLabel || `Ośrodek #${pcId}`;
      closeInsuranceModal();

      let regDate = null;
      //GET po to żeby pokazać datę rejestracji w alercie
      try {
        const regs = await ctx.api(`/registrations?trainerId=${encodeURIComponent(me.id)}&pokecenterId=${encodeURIComponent(pcId)}`);
        if (Array.isArray(regs) && regs.length) regDate = regs[0]?.registrationDate || regs[0]?.date || null;
      } catch {}

      alert(
        regDate
          ? `Ubezpieczenie aktywne\n\nKupione w: ${where}\nData rejestracji: ${regDate}`
          : `Ubezpieczenie aktywne\n\nKupione w: ${where}`
      );
    } catch (err) {
      closeInsuranceModal();
      alert('Nie udało się kupić ubezpieczenia: ' + err.message);
    }
  });
}
