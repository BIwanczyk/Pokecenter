export function requireLoggedIn(ctx) {
  const me = ctx.getCurrentUser();
  if (!me || !me.id) {
    alert('Brak zalogowanego użytkownika.');
    return null;
  }
  return me;
}

export function isTrainer(me) {
  return (me?._role || 'Trainer') === 'Trainer';
}

export function isOrganizer(me) {
  return (me?._role || '') === 'EventOrganizer';
}
