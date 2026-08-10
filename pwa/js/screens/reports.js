import { DB } from '../db.js';
import { renderShell } from '../layout.js';
import { el, formatDate } from '../utils.js';

export async function renderReports(container) {
  const content = renderShell(container, { title: 'گزارش‌ها', activeNav: '#/reports' });

  const employees = await DB.getAll('employees');
  employees.sort((a, b) => (a.firstName + a.lastName).localeCompare(b.firstName + b.lastName, 'fa'));

  const empSelect = el('select', {});
  empSelect.appendChild(el('option', { value: '' }, 'همه پرسنل'));
  employees.forEach(e => empSelect.appendChild(el('option', { value: e.id }, `${e.firstName} ${e.lastName}`)));
  content.appendChild(el('label', { class: 'field-label' }, 'پرسنل'));
  content.appendChild(empSelect);

  const typeSelect = el('select', {});
  ['همه', 'مثبت', 'منفی', 'شخصیتی', 'انضباطی'].forEach(t => typeSelect.appendChild(el('option', { value: t }, t)));
  content.appendChild(el('label', { class: 'field-label' }, 'نوع مورد'));
  content.appendChild(typeSelect);

  const fromInput = el('input', { type: 'text', placeholder: 'از تاریخ (yyyy/MM/dd) - اختیاری' });
  const toInput = el('input', { type: 'text', placeholder: 'تا تاریخ (yyyy/MM/dd) - اختیاری' });
  content.appendChild(fromInput);
  content.appendChild(toInput);

  const exportBtn = el('button', { class: 'btn btn-primary btn-block' }, 'Export CSV');
  content.appendChild(exportBtn);

  const listEl = el('div', { style: 'margin-top:10px;' });
  content.appendChild(listEl);

  let currentRows = [];

  function parseOrNull(text) {
    const t = text.trim();
    if (!t) return null;
    const parts = t.split('/').map(Number);
    if (parts.length === 3 && !parts.some(isNaN)) return new Date(parts[0], parts[1] - 1, parts[2]).getTime();
    return null;
  }

  async function buildRows() {
    const selectedEmpId = empSelect.value ? Number(empSelect.value) : null;
    const typeFilter = typeSelect.value;
    const from = parseOrNull(fromInput.value);
    const to = parseOrNull(toInput.value);
    const emps = selectedEmpId ? employees.filter(e => e.id === selectedEmpId) : employees;

    const rows = [];
    for (const emp of emps) {
      const name = `${emp.firstName} ${emp.lastName}`;
      if (typeFilter === 'همه' || typeFilter === 'مثبت') {
        (await DB.getAllByIndex('positiveNotes', 'employeeId', emp.id)).forEach(n =>
          rows.push({ name, type: 'مثبت', title: n.title, date: n.date, extra: `اهمیت: ${n.importance}` }));
      }
      if (typeFilter === 'همه' || typeFilter === 'منفی') {
        (await DB.getAllByIndex('negativeNotes', 'employeeId', emp.id)).forEach(n =>
          rows.push({ name, type: 'منفی', title: n.title, date: n.date, extra: `${n.importance} / ${n.status}` }));
      }
      if (typeFilter === 'همه' || typeFilter === 'شخصیتی') {
        (await DB.getAllByIndex('personalityNotes', 'employeeId', emp.id)).forEach(n =>
          rows.push({ name, type: 'شخصیتی', title: n.title, date: n.date, extra: n.type }));
      }
      if (typeFilter === 'همه' || typeFilter === 'انضباطی') {
        (await DB.getAllByIndex('disciplinaryRecords', 'employeeId', emp.id)).forEach(n =>
          rows.push({ name, type: 'انضباطی', title: n.violationType, date: n.date, extra: `شدت: ${n.severity}` }));
      }
    }
    let filtered = rows;
    if (from !== null) filtered = filtered.filter(r => r.date >= from);
    if (to !== null) filtered = filtered.filter(r => r.date <= to);
    filtered.sort((a, b) => b.date - a.date);
    return filtered;
  }

  async function reload() {
    currentRows = await buildRows();
    listEl.innerHTML = '';
    if (currentRows.length === 0) {
      listEl.appendChild(el('div', { class: 'empty-state' }, [
        el('div', { class: 'title' }, 'موردی مطابق فیلترها یافت نشد'),
        el('div', {}, 'فیلترها را تغییر دهید یا ابتدا سوابقی ثبت کنید')
      ]));
      return;
    }
    currentRows.forEach(r => {
      listEl.appendChild(el('div', { class: 'list-item' }, [
        el('div', { class: 'title' }, `${r.name} — ${r.type}`),
        el('div', { class: 'subtitle' }, `${r.title}   ${formatDate(r.date)}   ${r.extra}`)
      ]));
    });
  }

  [empSelect, typeSelect].forEach(elm => elm.addEventListener('change', reload));
  [fromInput, toInput].forEach(elm => elm.addEventListener('input', reload));

  exportBtn.addEventListener('click', () => {
    const header = 'پرسنل,نوع,عنوان,تاریخ,توضیحات\n';
    const body = currentRows.map(r =>
      `"${r.name}","${r.type}","${(r.title || '').replace(/"/g, '""')}","${formatDate(r.date)}","${(r.extra || '').replace(/"/g, '""')}"`
    ).join('\n');
    const csv = '\uFEFF' + header + body;
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `گزارش_${Date.now()}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  });

  await reload();
}
