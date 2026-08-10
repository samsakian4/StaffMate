import { DB } from '../db.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el } from '../utils.js';

export async function renderQuickAdd(container) {
  const content = renderShell(container, { title: 'ثبت مورد جدید', activeNav: '#/quick-add' });

  const employees = (await DB.getAll('employees')).filter(e => e.active)
    .sort((a, b) => (a.firstName + a.lastName).localeCompare(b.firstName + b.lastName, 'fa'));

  const select = el('select', {});
  select.appendChild(el('option', { value: '' }, 'انتخاب پرسنل...'));
  employees.forEach(emp => {
    select.appendChild(el('option', { value: emp.id }, `${emp.firstName} ${emp.lastName} (${emp.personnelCode})`));
  });
  content.appendChild(select);

  content.appendChild(el('div', { style: 'font-weight:600;margin:14px 0 8px;' }, 'نوع مورد را انتخاب کنید:'));

  function typeButton(label, type) {
    return el('button', {
      class: 'btn btn-primary',
      style: 'flex:1;',
      onclick: () => {
        const empId = select.value;
        if (!empId) { select.focus(); return; }
        navigate(`#/note-form/${type}/${empId}/new`);
      }
    }, label);
  }

  content.appendChild(el('div', { class: 'row', style: 'margin-bottom:8px;' }, [
    typeButton('مثبت', 'positive'),
    typeButton('منفی', 'negative')
  ]));
  content.appendChild(el('div', { class: 'row' }, [
    typeButton('شخصیتی', 'personality'),
    typeButton('انضباطی', 'disciplinary')
  ]));
}
