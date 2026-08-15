import { navigate } from './router.js';
import { el } from './utils.js';
import { icon } from './icons.js';

const NAV_ITEMS = [
  { route: '#/dashboard', label: 'خانه', icon: 'home' },
  { route: '#/employees', label: 'پرسنل', icon: 'users' },
  { route: '#/quick-add', label: 'ثبت مورد', icon: 'plusCircle' },
  { route: '#/reports', label: 'گزارش‌ها', icon: 'barChart' },
  { route: '#/settings', label: 'تنظیمات', icon: 'settings' }
];

export function renderShell(container, { title, activeNav = null, showBack = false, backHash = '#/dashboard', fab = null }) {
  container.innerHTML = '';
  container.className = 'app-shell';

  // ---- Sidebar (desktop) ----
  const sidebar = el('aside', { class: 'sidebar' });
  sidebar.appendChild(el('div', { class: 'sidebar-brand' }, [
    el('span', { class: 'sidebar-brand-mark' }, 'پ'),
    el('span', { class: 'sidebar-brand-text' }, 'پرسنل‌یار')
  ]));
  const sidebarNav = el('nav', { class: 'sidebar-nav' });
  NAV_ITEMS.forEach(item => {
    const active = item.route === activeNav;
    sidebarNav.appendChild(el('div', {
      class: 'sidebar-item' + (active ? ' active' : ''),
      onclick: () => navigate(item.route)
    }, [
      el('span', { class: 'sidebar-icon', html: icon(item.icon, 19) }),
      el('span', {}, item.label)
    ]));
  });
  sidebar.appendChild(sidebarNav);
  container.appendChild(sidebar);

  // ---- Main column ----
  const mainCol = el('div', { class: 'main-col' });

  const topbar = el('div', { class: 'topbar' });
  const topbarInner = el('div', { class: 'topbar-inner' });
  if (showBack) {
    topbarInner.appendChild(el('span', {
      class: 'back-btn',
      html: icon('chevronRight', 20),
      onclick: () => navigate(backHash)
    }));
  }
  topbarInner.appendChild(el('h1', { class: 'topbar-title' }, title));
  topbar.appendChild(topbarInner);
  mainCol.appendChild(topbar);

  const content = el('div', { class: 'content' });
  mainCol.appendChild(content);
  container.appendChild(mainCol);

  if (fab) {
    container.appendChild(el('button', {
      class: 'fab',
      onclick: fab.onClick
    }, [el('span', { html: icon('plus', 18) }), el('span', {}, fab.label)]));
  }

  // ---- Bottom nav (mobile only) ----
  const nav = el('div', { class: 'bottom-nav' });
  NAV_ITEMS.forEach(item => {
    const active = item.route === activeNav;
    nav.appendChild(el('div', {
      class: 'nav-item' + (active ? ' active' : ''),
      onclick: () => navigate(item.route)
    }, [
      el('span', { class: 'icon', html: icon(item.icon, 21) }),
      el('span', {}, item.label)
    ]));
  });
  container.appendChild(nav);

  return content;
}
