import { signIn, signUp } from '../auth.js';
import { el, showToast } from '../utils.js';
import { navigate } from '../router.js';

export async function renderLogin(container) {
  container.innerHTML = '';
  const wrap = el('div', { style: 'display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;padding:24px;' });
  wrap.appendChild(el('div', { style: 'font-size:22px;font-weight:700;margin-bottom:4px;' }, 'پرسنل‌یار'));
  wrap.appendChild(el('div', { style: 'color:var(--on-surface-variant);margin-bottom:20px;font-size:13px;' }, 'ورود برای همگام‌سازی داده روی همه دستگاه‌ها'));

  const box = el('div', { style: 'width:100%;max-width:320px;' });
  const emailInput = el('input', { type: 'email', placeholder: 'ایمیل' });
  const passInput = el('input', { type: 'password', placeholder: 'رمز عبور' });
  const errorEl = el('div', { style: 'color:var(--error);margin:6px 0;font-size:13px;' }, '');
  box.appendChild(emailInput);
  box.appendChild(passInput);
  box.appendChild(errorEl);

  box.appendChild(el('button', {
    class: 'btn btn-primary btn-block',
    onclick: async () => {
      errorEl.textContent = '';
      const { error } = await signIn(emailInput.value.trim(), passInput.value);
      if (error) { errorEl.textContent = 'ورود ناموفق: ' + error.message; return; }
      navigate('#/dashboard');
    }
  }, 'ورود'));

  box.appendChild(el('button', {
    class: 'btn btn-outline btn-block',
    onclick: async () => {
      errorEl.textContent = '';
      if (!emailInput.value.trim() || passInput.value.length < 6) {
        errorEl.textContent = 'ایمیل معتبر و رمز حداقل ۶ کاراکتری وارد کنید.';
        return;
      }
      const { error } = await signUp(emailInput.value.trim(), passInput.value);
      if (error) { errorEl.textContent = 'ثبت‌نام ناموفق: ' + error.message; return; }
      showToast('حساب ساخته شد. اگر تأیید ایمیل لازم بود، لینک را در ایمیل خود بزنید، سپس وارد شوید.');
    }
  }, 'ساخت حساب جدید (فقط بار اول)'));

  wrap.appendChild(box);
  container.appendChild(wrap);
}
