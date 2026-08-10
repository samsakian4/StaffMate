import { DB } from '../db.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el, showToast } from '../utils.js';

export async function renderEmployeeForm(container, params) {
  const isNew = params.id === 'new';
  const id = isNew ? null : Number(params.id);

  const content = renderShell(container, {
    title: isNew ? 'افزودن پرسنل' : 'ویرایش پرسنل',
    showBack: true,
    backHash: isNew ? '#/employees' : `#/employee/${id}`
  });

  let existing = null;
  if (!isNew) existing = await DB.get('employees', id);

  const codeInput = el('input', { type: 'text', placeholder: 'کد پرسنلی *', value: existing?.personnelCode || '' });
  const firstInput = el('input', { type: 'text', placeholder: 'نام *', value: existing?.firstName || '' });
  const lastInput = el('input', { type: 'text', placeholder: 'نام خانوادگی *', value: existing?.lastName || '' });
  const positionInput = el('input', { type: 'text', placeholder: 'سمت', value: existing?.position || '' });
  const workplaceInput = el('input', { type: 'text', placeholder: 'محل کار', value: existing?.workplace || '' });
  const shiftInput = el('input', { type: 'text', placeholder: 'شیفت', value: existing?.shift || '' });
  const activeCheck = el('input', { type: 'checkbox', id: 'activeChk' });
  activeCheck.checked = existing ? existing.active : true;

  const errorEl = el('div', { style: 'color:var(--error);margin-bottom:10px;' }, '');

  content.appendChild(codeInput);
  content.appendChild(firstInput);
  content.appendChild(lastInput);
  content.appendChild(positionInput);
  content.appendChild(workplaceInput);
  content.appendChild(shiftInput);
  const chkRow = el('div', { class: 'checkbox-row' }, [activeCheck, el('label', { for: 'activeChk' }, 'فعال')]);
  content.appendChild(chkRow);
  content.appendChild(errorEl);

  content.appendChild(el('button', {
    class: 'btn btn-primary btn-block',
    onclick: async () => {
      const code = codeInput.value.trim();
      const first = firstInput.value.trim();
      const last = lastInput.value.trim();
      if (!code || !first || !last) {
        errorEl.textContent = 'کد پرسنلی، نام و نام خانوادگی الزامی است.';
        return;
      }
      // duplicate personnel code check
      const all = await DB.getAll('employees');
      const dup = all.find(e => e.personnelCode === code && e.id !== id);
      if (dup) {
        errorEl.textContent = 'کد پرسنلی تکراری است.';
        return;
      }
      const now = Date.now();
      const record = {
        personnelCode: code,
        firstName: first,
        lastName: last,
        position: positionInput.value.trim(),
        workplace: workplaceInput.value.trim(),
        shift: shiftInput.value.trim(),
        active: activeCheck.checked,
        createdAt: existing?.createdAt || now,
        updatedAt: now
      };
      if (!isNew) record.id = id;
      if (isNew) {
        const newId = await DB.add('employees', record);
        showToast('پرسنل ثبت شد.');
        navigate(`#/employee/${newId}`);
      } else {
        await DB.put('employees', record);
        showToast('تغییرات ذخیره شد.');
        navigate(`#/employee/${id}`);
      }
    }
  }, 'ذخیره'));
}
