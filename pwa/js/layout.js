import { navigate } from './router.js';
import { el } from './utils.js';

const NAV_ITEMS = [
  { route: '#/dashboard', label: 'خانه', icon: '🏠' },
  { route: '#/employees', label: 'پرسنل', icon: '👥' },
  { route: '#/quick-add', label: 'ثبت مورد', icon: '➕' },
  { route: '#/reports', label: 'گزارش‌ها', icon: '📊' },
  { route: '#/settings', label: 'تنظیمات', icon: '⚙️' }
];

export function renderShell(container, { title, activeNav = null, showBack = false, backHash = '#/dashboard', fab = null }) {
  container.innerHTML = '';

  const topbar = el('div', { class: 'topbar', style: 'display:flex;align-items:center;gap:10px;' });
  if (showBack) {
    topbar.appendChild(el('span', {
      style: 'cursor:pointer;font-size:20px;',
      onclick: () => navigate(backHash)
    }, '→'));
  }
  topbar.appendChild(el('span', {}, title));
  container.appendChild(topbar);

  const content = el('div', { class: 'content' });
  container.appendChild(content);

  if (fab) {
    container.appendChild(el('div', {
      class: 'fab',
      onclick: fab.onClick
    }, fab.label));
  }

  if (activeNav) {
    const nav = el('div', { class: 'bottom-nav' });
    NAV_ITEMS.forEach(item => {
      const active = item.route === activeNav;
      nav.appendChild(el('div', {
        class: 'nav-item' + (active ? ' active' : ''),
        onclick: () => navigate(item.route)
      }, [
        el('span', { class: 'icon' }, item.icon),
        el('span', {}, item.label)
      ]));
    });
    container.appendChild(nav);
  }

  return content;
}
