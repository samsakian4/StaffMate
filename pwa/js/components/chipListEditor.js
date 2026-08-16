import { el } from '../utils.js';

/**
 * Editable chip/tag list — replaces raw comma-separated text inputs.
 * Each item is a removable pill; new items are added one at a time via
 * an input + button (or Enter). Calls onChange(csvString) after every
 * add/remove so the setting is saved immediately, without a separate
 * "save" step for this field.
 */
export function createChipListEditor({ initial, placeholder, onChange }) {
  let items = (initial || '').split(',').map(s => s.trim()).filter(Boolean);

  const wrap = el('div', { class: 'chip-editor' });
  const chipsRow = el('div', { class: 'chip-row' });
  const input = el('input', { type: 'text', placeholder: placeholder || 'مورد جدید...' });
  const addBtn = el('button', { class: 'btn btn-outline btn-sm', type: 'button' }, 'افزودن');
  const inputRow = el('div', { class: 'chip-input-row' }, [input, addBtn]);

  function render() {
    chipsRow.innerHTML = '';
    if (items.length === 0) {
      chipsRow.appendChild(el('span', { class: 'chip-empty' }, 'موردی ثبت نشده'));
    }
    items.forEach((item, idx) => {
      chipsRow.appendChild(el('span', { class: 'chip removable' }, [
        el('span', {}, item),
        el('span', {
          class: 'chip-remove',
          onclick: () => { items.splice(idx, 1); render(); onChange(items.join(',')); }
        }, '×')
      ]));
    });
  }

  function addItem() {
    const v = input.value.trim();
    if (!v || items.includes(v)) { input.value = ''; return; }
    items.push(v);
    input.value = '';
    render();
    onChange(items.join(','));
  }

  addBtn.addEventListener('click', addItem);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') { e.preventDefault(); addItem(); }
  });

  render();
  wrap.appendChild(chipsRow);
  wrap.appendChild(inputRow);
  return wrap;
}
