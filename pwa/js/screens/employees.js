import { DB } from '../db.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el, escapeHtml } from '../utils.js';

export async function renderEmployees(container) {
  const content = renderShell(container, {
    title: 'پرسنل',
    activeNav: '#/employees',
    fab: { label: '+ افزودن پرسنل', onClick: () => navigate('#/employee-form/new') }
  });

  const prefilled = sessionStorage.getItem('prefilledQuery') || '';
  sessionStorage.removeItem('prefilledQuery');

  const searchInput = el('input', { type: 'text', placeholder: 'جست‌وجو (نام یا کد پرسنلی)', value: prefilled });
  const activeCheckWrap = el('div', { class: 'checkbox-row' });
  const activeCheck = el('input', { type: 'checkbox', id: 'onlyActive', checked: 'checked' });
  activeCheckWrap.appendChild(activeCheck);
  activeCheckWrap.appendChild(el('label', { for: 'onlyActive' }, 'فقط پرسنل فعال'));

  const listEl = el('div', {});

  async function reload() {
    const q = searchInput.value.trim();
    const onlyActive = activeCheck.checked;
    let list = await DB.getAll('employees');
    if (onlyActive) list = list.filter(e => e.active);
    if (q) {
      list = list.filter(e =>
        e.firstName.includes(q) || e.lastName.includes(q) || e.personnelCode.includes(q)
      );
    }
    list.sort((a, b) => (a.firstName + a.lastName).localeCompare(b.firstName + b.lastName, 'fa'));

    listEl.innerHTML = '';
    if (list.length === 0) {
      listEl.appendChild(el('div', { class: 'empty-state' }, [
        el('div', { class: 'title' }, q ? 'نتیجه‌ای یافت نشد' : 'هنوز پرسنلی ثبت نشده'),
        el('div', {}, q ? 'عبارت جست‌وجو را تغییر دهید' : 'با دکمه + یک پرسنل جدید اضافه کنید')
      ]));
      return;
    }
    list.forEach(emp => {
      const item = el('div', {
        class: 'list-item',
        onclick: () => navigate(`#/employee/${emp.id}`)
      }, [
        el('div', { class: 'title' }, `${emp.firstName} ${emp.lastName}`),
        el('div', { class: 'subtitle' }, `کد: ${emp.personnelCode}  |  ${emp.position || '-'}  |  ${emp.shift || '-'}`),
      ]);
      if (!emp.active) item.appendChild(el('div', { class: 'chip', style: 'color:var(--error);margin-top:6px;' }, 'غیرفعال'));
      listEl.appendChild(item);
    });
  }

  searchInput.addEventListener('input', reload);
  activeCheck.addEventListener('change', reload);

  content.appendChild(searchInput);
  content.appendChild(activeCheckWrap);
  content.appendChild(listEl);

  await reload();
}
