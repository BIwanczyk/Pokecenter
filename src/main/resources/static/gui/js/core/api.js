export function createApi({ API_BASE }) {
  return async function api(path, opts = {}) {
    const res = await fetch(API_BASE + path, {
      headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) },
      ...opts,
    });

    const ct = (res.headers.get('content-type') || '').toLowerCase();
    const body = ct.includes('application/json')
      ? await res.json().catch(() => null)
      : await res.text().catch(() => '');

    if (!res.ok) {
      const msg = (typeof body === 'string' && body)
        ? body
        : (body?.message || body?.error || `HTTP ${res.status}`);
      throw new Error(msg);
    }

    return body;
  };
}
