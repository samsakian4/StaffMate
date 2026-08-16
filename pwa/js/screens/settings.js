import { DB, getSetting, setSetting } from '../db.js';
import { signOut } from '../auth.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el, sha256, showToast, confirmDialog } from '../utils.js';

export async function renderSettings(container) {
  const content = renderShell(container, { title: 'تنظیمات', activeNav: '#/settings' });

  const keys = ['weight_positive', 'weight_negative', 'severity_low', 'severity_medium', 'severity_high', 'severity_very_high',
    'shifts_list', 'positions_list', 'workplaces_list', 'violation_types_list', 'pin_enabled', 'last_backup_date'];
  const values = {};
  for (const k of keys) values[k] = await getSetting(k, '');

  content.appendChild(el('div', { style: 'font-weight:600;margin-bottom:8px;' }, 'امتیازدهی'));
  const wPos = el('input', { type: 'number', placeholder: 'وزن نکته مثبت', value: values.weight_positive });
  const wNeg = el('input', { type: 'number', placeholder: 'وزن نکته منفی', value: values.weight_negative });
  const sLow = el('input', { type: 'number', placeholder: 'وزن شدت کم', value: values.severity_low });
  const sMed = el('input', { type: 'number', placeholder: 'وزن شدت متوسط', value: values.severity_medium });
  const sHigh = el('input', { type: 'number', placeholder: 'وزن شدت زیاد', value: values.severity_high });
  const sVeryHigh = el('input', { type: 'number', placeholder: 'وزن شدت بسیار زیاد', value: values.severity_very_high });
  [wPos, wNeg, sLow, sMed, sHigh, sVeryHigh].forEach(i => content.appendChild(i));

  content.appendChild(el('hr', { class: 'divider' }));
  content.appendChild(el('div', { style: 'font-weight:600;margin-bottom:8px;' }, 'لیست‌های قابل تنظیم (با کاما جدا کنید)'));
  const shifts = el('input', { type: 'text', placeholder: 'شیفت‌ها', value: values.shifts_list });
  const positions = el('input', { type: 'text', placeholder: 'سمت‌ها', value: values.positions_list });
  const workplaces = el('input', { type: 'text', placeholder: 'محل‌های کاری', value: values.workplaces_list });
  const violations = el('input', { type: 'text', placeholder: 'انواع تخلف', value: values.violation_types_list });
  [shifts, positions, workplaces, violations].forEach(i => content.appendChild(i));

  content.appendChild(el('button', {
    class: 'btn btn-primary btn-block',
    onclick: async () => {
      await Promise.all([
        setSetting('weight_positive', wPos.value), setSetting('weight_negative', wNeg.value),
        setSetting('severity_low', sLow.value), setSetting('severity_medium', sMed.value),
        setSetting('severity_high', sHigh.value), setSetting('severity_very_high', sVeryHigh.value),
        setSetting('shifts_list', shifts.value), setSetting('positions_list', positions.value),
        setSetting('workplaces_list', workplaces.value), setSetting('violation_types_list', violations.value)
      ]);
      showToast('تنظیمات ذخیره شد.');
    }
  }, 'ذخیره تنظیمات'));

  content.appendChild(el('hr', { class: 'divider' }));
  content.appendChild(el('div', { style: 'font-weight:600;margin-bottom:8px;' }, 'امنیت'));
  const pinCheck = el('input', { type: 'checkbox', id: 'pinChk' });
  pinCheck.checked = values.pin_enabled === '1';
  content.appendChild(el('div', { class: 'checkbox-row' }, [pinCheck, el('label', { for: 'pinChk' }, 'فعال‌سازی قفل PIN')]));
  pinCheck.addEventListener('change', () => setSetting('pin_enabled', pinCheck.checked ? '1' : '0'));

  const pinInput = el('input', { type: 'password', placeholder: 'PIN جدید', maxlength: '8', inputmode: 'numeric' });
  content.appendChild(pinInput);
  content.appendChild(el('button', {
    class: 'btn btn-outline btn-block',
    onclick: async () => {
      if (!pinInput.value.trim()) return;
      await setSetting('pin_hash', await sha256(pinInput.value.trim()));
      pinInput.value = '';
      showToast('PIN ذخیره شد.');
    }
  }, 'ذخیره PIN'));

  content.appendChild(el('hr', { class: 'divider' }));
  content.appendChild(el('div', { style: 'font-weight:600;margin-bottom:8px;' }, 'Backup / Restore'));
  content.appendChild(el('div', { style: 'color:var(--text-muted);font-size:13px;margin-bottom:8px;' },
    `آخرین Backup: ${values.last_backup_date || '-'}`));

  content.appendChild(el('button', {
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
  }, 'تهیه Backup'));

  const fileInput = el('input', { type: 'file', accept: 'application/json', style: 'display:none;' });
  content.appendChild(fileInput);
  content.appendChild(el('button', {
    class: 'btn btn-outline btn-block',
    onclick: () => fileInput.click()
  }, 'بازیابی از Backup'));

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

      // automatic safety backup before restore
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

  content.appendChild(el('hr', { class: 'divider' }));
  content.appendChild(el('button', {
    class: 'btn btn-error btn-block',
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
