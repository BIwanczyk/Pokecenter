

(function () {
  const isFile = window.location.protocol === 'file:';
  const host = window.location.hostname;
  const port = window.location.port;

  let apiBase = window.location.origin;

  if (isFile) {
    apiBase = 'http://localhost:8080';
  } else if ((host === 'localhost' || host === '127.0.0.1') && port && port !== '8080') {

    apiBase = 'http://localhost:8080';
  }

  window.APP_CONFIG = { API_BASE: apiBase };
})();
