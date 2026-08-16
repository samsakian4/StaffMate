import { DB, getSetting } from '../db.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el, todayStr, parseDate, showToast, confirmDialog } from '../utils.js';

const STORE_MAP = {
  positive: 'positiveNotes',
  negative: 'negativeNotes',
  personality: 'personalityNotes',
  disciplinary: 'disciplinaryRecords'
};
const NEGATIVE_STATUSES = ['تکرار نشده', 'تکرار شده', 'برطرف شده'];
const SEVERITIES = ['کم', 'متوسط', 'زیاد', 'بسیار زیاد'];
const PERSONALITY_TYPES = ['مثبت', 'منفی'];
const TITLES = { positive: 'افزودن نکته مثبت', negative: 'افزودن نکته منفی', personality: 'افزودن ویژگی شخصیتی', disciplinary: 'افزودن مورد انضباطی' };
const OTHER = 'سایر (وارد دستی)';

function selectField(options, value) {
  const select = el('select', {});
  options.forEach(opt => {
    const o = el('option', { value: opt }, opt);
    if (opt === value) o.setAttribute('selected', 'selected');
    select.appendChild(o);
  });
  return select;
}

/** Dropdown of preset titles (managed in Settings) + a "سایر" fallback that reveals a free-text field. */
function createTitlePicker(options, initialValue) {
  const inList = initialValue && options.includes(initialValue);
  const allOptions = [...options, OTHER];
  const select = selectField(allOptions, inList ? initialValue : (initialValue ? OTHER : options[0]));
  const customInput = el('input', { type: 'text', placeholder: 'عنوان را وارد کنید *', value: inList ? '' : (initialValue || '') });
  customInput.style.display = select.value === OTHER ? '' : 'none';
  select.addEventListener('change', () => {
    customInput.style.display = select.value === OTHER ? '' : 'none';
    if (select.value === OTHER) customInput.focus();
  });
  const wrap = el('div', {}, [select, customInput]);
  return { element: wrap, getValue: () => (select.value === OTHER ? customInput.value.trim() : select.value) };
}

export async function renderNoteForm(container, params) {
  const { type, employeeId: empIdStr, noteId: noteIdStr } = params;
  const employeeId = Number(empIdStr);
  const isNew = noteIdStr === 'new';
  const noteId = isNew ? null : Number(noteIdStr);
  const store = STORE_MAP[type];

  const content = renderShell(container, {
    title: isNew ? TITLES[type] : 'ویرایش مورد',
    showBack: true,
    backHash: `#/employee/${employeeId}`
  });

  let existing = null;
  if (!isNew) existing = await DB.get(store, noteId);

  const importanceOptions = (await getSetting('importance_list', 'کم,متوسط,زیاد')).split(',').map(s => s.trim()).filter(Boolean);
  const violationOptions = (await getSetting('violation_types_list', 'غیبت,تاخیر,عدم رعایت ایمنی,نافرمانی,سایر')).split(',').map(s => s.trim()).filter(Boolean);
  const positiveTitleOptions = (await getSetting('positive_titles_list', '')).split(',').map(s => s.trim()).filter(Boolean);
  const negativeTitleOptions = (await getSetting('negative_titles_list', '')).split(',').map(s => s.trim()).filter(Boolean);
  const personalityTitleOptions = (await getSetting('personality_titles_list', '')).split(',').map(s => s.trim()).filter(Boolean);

  const dateInput = el('input', { type: 'text', placeholder: 'تاریخ (yyyy/MM/dd) *', value: existing ? formatIfNeeded(existing.date) : todayStr() });
  content.appendChild(el('label', { class: 'field-label' }, 'تاریخ'));
  content.appendChild(dateInput);

  let titlePicker, descInput, importanceSelect, statusSelect, typeSelect, violationSelect, severitySelect, actionInput, additionalInput;

  if (type === 'positive') {
    content.appendChild(el('label', { class: 'field-label' }, 'عنوان'));
    titlePicker = createTitlePicker(positiveTitleOptions, existing?.title);
    content.appendChild(titlePicker.element);
    descInput = el('textarea', { rows: 3, placeholder: 'توضیح' }, existing?.description || '');
    content.appendChild(descInput);
    content.appendChild(el('label', { class: 'field-label' }, 'میزان اهمیت'));
    importanceSelect = selectField(importanceOptions, existing?.importance || importanceOptions[1] || importanceOptions[0]);
    content.appendChild(importanceSelect);
  } else if (type === 'negative') {
    content.appendChild(el('label', { class: 'field-label' }, 'عنوان'));
    titlePicker = createTitlePicker(negativeTitleOptions, existing?.title);
    content.appendChild(titlePicker.element);
    descInput = el('textarea', { rows: 3, placeholder: 'توضیح' }, existing?.description || '');
    content.appendChild(descInput);
    content.appendChild(el('label', { class: 'field-label' }, 'میزان اهمیت'));
    importanceSelect = selectField(importanceOptions, existing?.importance || importanceOptions[1] || importanceOptions[0]);
    content.appendChild(importanceSelect);
    content.appendChild(el('label', { class: 'field-label' }, 'وضعیت'));
    statusSelect = selectField(NEGATIVE_STATUSES, existing?.status || NEGATIVE_STATUSES[0]);
    content.appendChild(statusSelect);
  } else if (type === 'personality') {
    content.appendChild(el('label', { class: 'field-label' }, 'نوع ویژگی'));
    typeSelect = selectField(PERSONALITY_TYPES, existing?.type || PERSONALITY_TYPES[0]);
    content.appendChild(typeSelect);
    content.appendChild(el('label', { class: 'field-label' }, 'عنوان ویژگی'));
    titlePicker = createTitlePicker(personalityTitleOptions, existing?.title);
    content.appendChild(titlePicker.element);
    descInput = el('textarea', { rows: 3, placeholder: 'توضیح' }, existing?.description || '');
    content.appendChild(descInput);
  } else if (type === 'disciplinary') {
    content.appendChild(el('label', { class: 'field-label' }, 'نوع تخلف'));
    violationSelect = selectField(violationOptions, existing?.violationType || violationOptions[0]);
    content.appendChild(violationSelect);
    descInput = el('textarea', { rows: 3, placeholder: 'توضیح' }, existing?.description || '');
    content.appendChild(descInput);
    content.appendChild(el('label', { class: 'field-label' }, 'شدت'));
    severitySelect = selectField(SEVERITIES, existing?.severity || SEVERITIES[0]);
    content.appendChild(severitySelect);
    actionInput = el('input', { type: 'text', placeholder: 'اقدام انجام‌شده', value: existing?.actionTaken || '' });
    content.appendChild(actionInput);
    additionalInput = el('textarea', { rows: 2, placeholder: 'توضیحات تکمیلی' }, existing?.additionalNotes || '');
    content.appendChild(additionalInput);
  }

  const errorEl = el('div', { style: 'color:var(--error);margin-bottom:10px;' }, '');
  content.appendChild(errorEl);

  content.appendChild(el('button', {
    class: 'btn btn-primary btn-block',
    onclick: async () => {
      const dateMillis = parseDate(dateInput.value.trim());
      const titleValue = titlePicker ? titlePicker.getValue() : '';
      if (type !== 'disciplinary' && !titleValue) {
        errorEl.textContent = 'عنوان الزامی است.';
        return;
      }
      let record = { employeeId, date: dateMillis, createdAt: existing?.createdAt || Date.now() };
      if (type === 'positive') {
        record = { ...record, title: titleValue, description: descInput.value.trim(), importance: importanceSelect.value };
      } else if (type === 'negative') {
        record = { ...record, title: titleValue, description: descInput.value.trim(), importance: importanceSelect.value, status: statusSelect.value };
      } else if (type === 'personality') {
        record = { ...record, type: typeSelect.value, title: titleValue, description: descInput.value.trim() };
      } else {
        record = { ...record, violationType: violationSelect.value, description: descInput.value.trim(), severity: severitySelect.value, actionTaken: actionInput.value.trim(), additionalNotes: additionalInput.value.trim() };
      }
      if (!isNew) record.id = noteId;
      await (isNew ? DB.add(store, record) : DB.put(store, record));
      showToast(isNew ? 'با موفقیت ثبت شد.' : 'تغییرات ذخیره شد.');
      navigate(`#/employee/${employeeId}`);
    }
  }, 'ذخیره'));

  if (!isNew) {
    content.appendChild(el('button', {
      class: 'btn btn-error btn-block',
      onclick: async () => {
        const ok = await confirmDialog({ title: 'حذف مورد', message: 'آیا از حذف این مورد مطمئن هستید؟' });
        if (ok) {
          await DB.delete(store, noteId);
          showToast('مورد حذف شد.');
          navigate(`#/employee/${employeeId}`);
        }
      }
    }, 'حذف'));
  }
}

function formatIfNeeded(millis) {
  const d = new Date(millis);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}/${m}/${day}`;
}
