import { DB, getSetting, setSetting } from '../db.js';
import { signOut } from '../auth.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el, sha256, showToast, confirmDialog } from '../utils.js';
import { createChipListEditor } from '../components/chipListEditor.js';

function sectionCard(title, description, children) {
  const card = el('div', { class: 'card' });
  card.appendChild(el('div', { style: 'font-weight:700;font-size:14.5px;margin-bottom:2px;' }, title));
  if (description) {
    card.appendChild(el('div', { style: 'font-size:12.5px;color:var(--text-muted);margin-bottom:14px;' }, description));
  } else {
    card.appendChild(el('div', { style: 'margin-bottom:10px;' }));
  }
  (Array.isArray(children) ? children : [children]).forEach(c => card.appendChild(c));
  return card;
}

function numberField(label, value, onInput) {
  const wrap = el('div', {});
  wrap.appendChild(el('label', { class: 'field-label' }, label));
  const input = el('input', { type: 'number', value });
  input.addEventListener('input', () => onInput(input.value));
  wrap.appendChild(input);
  return wrap;
}

export async function renderSettings(container) {
  const content = renderShell(container, { title: 'تنظیمات', activeNav: '#/settings' });

  const keys = ['weight_positive', 'weight_negative', 'severity_low', 'severity_medium', 'severity_high', 'severity_very_high',
    'shifts_list', 'positions_list', 'workplaces_list', 'violation_types_list', 'importance_list',
    'positive_titles_list', 'negative_titles_list', 'personality_titles_list', 'pin_enabled', 'last_backup_date'];
  const values = {};
  for (const k of keys) values[k] = await getSetting(k, '');

  const weights = { ...values };
  let weightsDirty = false;

  content.appendChild(el('div', { class: 'section-label' }, 'امتیازدهی'));
  const weightsGrid = el('div', { class: 'field-grid' }, [
    numberField('وزن نکته مثبت', values.weight_positive, v => { weights.weight_positive = v; weightsDirty = true; }),
    numberField('وزن نکته منفی', values.weight_negative, v => { weights.weight_negative = v; weightsDirty = true; }),
    numberField('وزن شدت کم', values.severity_low, v => { weights.severity_low = v; weightsDirty = true; }),
    numberField('وزن شدت متوسط', values.severity_medium, v => { weights.severity_medium = v; weightsDirty = true; }),
    numberField('وزن شدت زیاد', values.severity_high, v => { weights.severity_high = v; weightsDirty = true; }),
    numberField('وزن شدت بسیار زیاد', values.severity_very_high, v => { weights.severity_very_high = v; weightsDirty = true; })
  ]);
  const saveWeightsBtn = el('button', {
    class: 'btn btn-primary btn-block',
    style: 'margin-top:14px;',
    onclick: async () => {
      await Promise.all([
        setSetting('weight_positive', weights.weight_positive), setSetting('weight_negative', weights.weight_negative),
        setSetting('severity_low', weights.severity_low), setSetting('severity_medium', weights.severity_medium),
        setSetting('severity_high', weights.severity_high), setSetting('severity_very_high', weights.severity_very_high)
      ]);
      weightsDirty = false;
      showToast('امتیازدهی ذخیره شد.');
    }
  }, 'ذخیره امتیازدهی');
  content.appendChild(sectionCard('وزن‌های امتیازدهی', 'برای هر نوع سابقه، امتیاز اثرگذار روی نمره کلی را تعیین کنید.', [weightsGrid, saveWeightsBtn]));

  content.appendChild(el('div', { class: 'section-label' }, 'لیست‌های قابل انتخاب'));
  content.appendChild(sectionCard('شیفت‌ها', 'در فرم افزودن/ویرایش پرسنل قابل انتخاب است.',
    createChipListEditor({ initial: values.shifts_list, placeholder: 'مثلاً عصر', onChange: (csv) => setSetting('shifts_list', csv) })
  ));
  content.appendChild(sectionCard('سمت‌ها', null,
    createChipListEditor({ initial: values.positions_list, placeholder: 'مثلاً سرشیفت', onChange: (csv) => setSetting('positions_list', csv) })
  ));
  content.appendChild(sectionCard('محل‌های کاری', null,
    createChipListEditor({ initial: values.workplaces_list, placeholder: 'مثلاً انبار', onChange: (csv) => setSetting('workplaces_list', csv) })
  ));
  content.appendChild(sectionCard('انواع تخلف', 'در فرم ثبت مورد انضباطی قابل انتخاب است.',
    createChipListEditor({ initial: values.violation_types_list, placeholder: 'مثلاً تاخیر', onChange: (csv) => setSetting('violation_types_list', csv) })
  ));
  content.appendChild(sectionCard('سطوح اهمیت', 'در فرم نکات مثبت و منفی قابل انتخاب است.',
    createChipListEditor({ initial: values.importance_list, placeholder: 'مثلاً زیاد', onChange: (csv) => setSetting('importance_list', csv) })
  ));

  content.appendChild(el('div', { class: 'section-label' }, 'عناوین آماده سوابق'));
  content.appendChild(sectionCard('عناوین نکات مثبت', 'در فرم ثبت نکته مثبت به‌عنوان گزینه آماده نمایش داده می‌شود.',
    createChipListEditor({ initial: values.positive_titles_list, placeholder: 'مثلاً همکاری خوب', onChange: (csv) => setSetting('positive_titles_list', csv) })
  ));
  content.appendChild(sectionCard('عناوین نکات منفی', null,
    createChipListEditor({ initial: values.negative_titles_list, placeholder: 'مثلاً تاخیر', onChange: (csv) => setSetting('negative_titles_list', csv) })
  ));
  content.appendChild(sectionCard('عناوین ویژگی شخصیتی', null,
    createChipListEditor({ initial: values.personality_titles_list, placeholder: 'مثلاً مسئولیت‌پذیر', onChange: (csv) => setSetting('personality_titles_list', csv) })
  ));

  content.appendChild(el('div', { class: 'section-label' }, 'امنیت'));
  const pinCheck = el('input', { type: 'checkbox', id: 'pinChk' });
  pinCheck.checked = values.pin_enabled === '1';
  const pinInputWrap = el('div', { style: pinCheck.checked ? '' : 'display:none;' });
  const pinInput = el('input', { type: 'password', placeholder: 'PIN جدید', maxlength: '8', inputmode: 'numeric' });
  pinInputWrap.appendChild(pinInput);
  pinInputWrap.appendChild(el('button', {
    class: 'btn btn-outline btn-block',
    onclick: async () => {
      if (!pinInput.value.trim()) return;
      await setSetting('pin_hash', await sha256(pinInput.value.trim()));
      pinInput.value = '';
      showToast('PIN ذخیره شد.');
    }
  }, 'ذخیره PIN'));
  pinCheck.addEventListener('change', () => {
    setSetting('pin_enabled', pinCheck.checked ? '1' : '0');
    pinInputWrap.style.display = pinCheck.checked ? '' : 'none';
  });
  content.appendChild(sectionCard('قفل PIN', 'برای جلوگیری از دسترسی افراد دیگر به این دستگاه.', [
    el('div', { class: 'checkbox-row' }, [pinCheck, el('label', { for: 'pinChk' }, 'فعال‌سازی قفل PIN')]),
    pinInputWrap
  ]));

  content.appendChild(el('div', { class: 'section-label' }, 'پشتیبان‌گیری'));
  content.appendChild(sectionCard('Backup / Restore', `آخرین Backup: ${values.last_backup_date || '-'}`, [
    el('button', {
      class: 'btn btn-primary btn-block',
      onclick: async () => {
        const data = await DB.exportAll();
        const json = JSON.stringify(data, null, 2);
        const blob = new Blob([json], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const now = new Date();
        const stamp = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
        const a = document.createElement('a');
        a.href = url;
        a.download = `StaffMate_Backup_${stamp}.json`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        const nowStr = now.toLocaleString('fa-IR');
        await setSetting('last_backup_date', nowStr);
        showToast('Backup با موفقیت ذخیره شد.');
      }
    }, 'تهیه Backup'),
    (() => {
      const fileInput = el('input', { type: 'file', accept: 'application/json', style: 'display:none;' });
      const restoreBtn = el('button', { class: 'btn btn-outline btn-block', onclick: () => fileInput.click() }, 'بازیابی از Backup');
      fileInput.addEventListener('change', async () => {
        const file = fileInput.files[0];
        if (!file) return;
        try {
          const text = await file.text();
          const data = JSON.parse(text);
          if (!data.employees || !Array.isArray(data.employees)) {
            showToast('فایل Backup نامعتبر یا خراب است.');
            return;
          }
          const ok = await confirmDialog({
            title: 'هشدار بازیابی',
            message: 'تمام اطلاعات فعلی با اطلاعات فایل Backup جایگزین می‌شود. ادامه می‌دهید؟'
          });
          if (!ok) return;
          const current = await DB.exportAll();
          localStorage.setItem('staffmate_auto_backup_before_restore', JSON.stringify(current));
          await DB.importAll(data);
          showToast('بازیابی با موفقیت انجام شد.');
          navigate('#/dashboard');
        } catch (e) {
          showToast('خطا در خواندن فایل Backup.');
        } finally {
          fileInput.value = '';
        }
      });
      const wrap = el('div', {});
      wrap.appendChild(fileInput);
      wrap.appendChild(restoreBtn);
      return wrap;
    })()
  ]));

  content.appendChild(el('button', {
    class: 'btn btn-error btn-block',
    style: 'margin-top:8px;',
    onclick: async () => {
      const ok = await confirmDialog({ title: 'خروج از حساب', message: 'از حساب کاربری خارج می‌شوید. برای ورود مجدد به رمز نیاز دارید.' });
      if (ok) {
        await signOut();
        sessionStorage.clear();
        navigate('#/login');
      }
    }
  }, 'خروج از حساب'));
}
