// Shared utilities: date formatting, hashing, scoring, toast, dialogs.

export function todayStr() {
  const d = new Date();
  return formatDate(d.getTime());
}

export function formatDate(millis) {
  const d = new Date(millis);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}/${m}/${day}`;
}

export function parseDate(text) {
  const parts = (text || '').split('/').map(Number);
  if (parts.length === 3 && !parts.some(isNaN)) {
    const d = new Date(parts[0], parts[1] - 1, parts[2]);
    return d.getTime();
  }
  return Date.now();
}

export function daysAgoMillis(days) {
  const d = new Date();
  d.setDate(d.getDate() - days);
  d.setHours(0, 0, 0, 0);
  return d.getTime();
}

export async function sha256(text) {
  const enc = new TextEncoder().encode(text);
  const buf = await crypto.subtle.digest('SHA-256', enc);
  return Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('');
}

export function showToast(message) {
  const el = document.createElement('div');
  el.className = 'toast';
  el.textContent = message;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 2200);
}

export function confirmDialog({ title, message, confirmLabel = 'تأیید', cancelLabel = 'انصراف' }) {
  return new Promise((resolve) => {
    const backdrop = document.createElement('div');
    backdrop.className = 'dialog-backdrop';
    backdrop.innerHTML = `
      <div class="dialog">
        <h3>${escapeHtml(title)}</h3>
        <p>${escapeHtml(message)}</p>
        <div class="actions">
          <button class="btn btn-outline" data-act="cancel">${escapeHtml(cancelLabel)}</button>
          <button class="btn btn-primary" data-act="confirm">${escapeHtml(confirmLabel)}</button>
        </div>
      </div>`;
    document.body.appendChild(backdrop);
    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop) { backdrop.remove(); resolve(false); }
      const act = e.target.getAttribute('data-act');
      if (act === 'confirm') { backdrop.remove(); resolve(true); }
      if (act === 'cancel') { backdrop.remove(); resolve(false); }
    });
  });
}

export function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

export function el(tag, attrs = {}, children = []) {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === 'class') node.className = v;
    else if (k.startsWith('on') && typeof v === 'function') node.addEventListener(k.slice(2), v);
    else if (k === 'html') node.innerHTML = v;
    else node.setAttribute(k, v);
  }
  (Array.isArray(children) ? children : [children]).forEach(c => {
    if (c === null || c === undefined) return;
    node.appendChild(typeof c === 'string' ? document.createTextNode(c) : c);
  });
  return node;
}
