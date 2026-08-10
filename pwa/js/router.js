const routes = [];

export function route(pattern, handler) {
  // pattern like '#/employee/:id' -> regex with named groups
  const paramNames = [];
  const regexStr = pattern.replace(/:[^/]+/g, (m) => {
    paramNames.push(m.slice(1));
    return '([^/]+)';
  });
  const regex = new RegExp('^' + regexStr + '$');
  routes.push({ regex, paramNames, handler });
}

export function navigate(hash) {
  window.location.hash = hash;
}

async function renderCurrent() {
  const hash = window.location.hash || '#/dashboard';
  for (const r of routes) {
    const match = hash.match(r.regex);
    if (match) {
      const params = {};
      r.paramNames.forEach((name, i) => { params[name] = decodeURIComponent(match[i + 1]); });
      const container = document.getElementById('app');
      window.scrollTo(0, 0);
      await r.handler(container, params);
      return;
    }
  }
  navigate('#/dashboard');
}

export function startRouter() {
  window.addEventListener('hashchange', renderCurrent);
  renderCurrent();
}
