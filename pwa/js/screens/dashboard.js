import { DB } from '../db.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el } from '../utils.js';

export async function renderDashboard(container) {
  const content = renderShell(container, {
    title: 'پرسنل‌یار',
    activeNav: '#/dashboard',
    fab: { label: '+ ثبت مورد', onClick: () => navigate('#/quick-add') }
  });

  const [employees, positives, negatives, disciplinary] = await Promise.all([
    DB.getAll('employees'),
    DB.count('positiveNotes'),
    DB.count('negativeNotes'),
    DB.count('disciplinaryRecords')
  ]);
  const activeCount = employees.filter(e => e.active).length;

  const searchWrap = el('input', {
    type: 'text',
    placeholder: 'جست‌وجوی سریع پرسنل...'
  });
  searchWrap.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && searchWrap.value.trim()) {
      sessionStorage.setItem('prefilledQuery', searchWrap.value.trim());
      navigate('#/employees');
    }
  });
  content.appendChild(searchWrap);

  function statCard(label, value, icon, colorVar) {
    return el('div', { class: 'stat-card' }, [
      el('div', { class: 'stat-icon', style: `background:color-mix(in srgb, var(${colorVar}) 18%, transparent); color:var(${colorVar});` }, icon),
      el('div', { class: 'stat-value' }, String(value)),
      el('div', { class: 'stat-label' }, label)
    ]);
  }

  const grid1 = el('div', { class: 'stat-grid' }, [
    statCard('پرسنل فعال', activeCount, '👥', '--primary'),
    statCard('مثبت', positives, '👍', '--positive')
  ]);
  const grid2 = el('div', { class: 'stat-grid' }, [
    statCard('منفی', negatives, '👎', '--negative'),
    statCard('انضباطی', disciplinary, '⚖️', '--secondary')
  ]);
  content.appendChild(grid1);
  content.appendChild(grid2);

  content.appendChild(el('button', {
    class: 'btn btn-outline btn-block',
    onclick: () => navigate('#/employees')
  }, 'مشاهده لیست پرسنل'));
}
