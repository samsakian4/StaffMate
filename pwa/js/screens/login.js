import { signIn, signUp } from '../auth.js';
import { el, showToast } from '../utils.js';
import { navigate } from '../router.js';

export async function renderLogin(container) {
  container.innerHTML = '';
  const wrap = el('div', { style: 'display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;padding:24px;' });
  wrap.appendChild(el('div', { style: 'font-size:22px;font-weight:700;margin-bottom:4px;' }, 'پرسنل‌یار'));
  wrap.appendChild(el('div', { style: 'color:var(--text-muted);margin-bottom:20px;font-size:13px;' }, 'ورود برای همگام‌سازی داده روی همه دستگاه‌ها'));

  const box = el('div', { style: 'width:100%;max-width:320px;' });
  const emailInput = el('input', { type: 'email', placeholder: 'ایمیل', autocomplete: 'email' });
  const passInput = el('input', { type: 'password', placeholder: 'رمز عبور', autocomplete: 'current-password' });
  const errorEl = el('div', { style: 'color:var(--error);margin:6px 0;font-size:13px;' }, '');
  const infoEl = el('div', { style: 'color:var(--primary);margin:6px 0;font-size:13px;' }, '');
  box.appendChild(emailInput);
  box.appendChild(passInput);
  box.appendChild(errorEl);
  box.appendChild(infoEl);

  let busy = false;

  function setBusy(v) {
    busy = v;
    loginBtn.disabled = v;
    signupBtn.disabled = v;
    loginBtn.textContent = v ? 'در حال ورود...' : 'ورود';
  }

  function friendlyError(e) {
    const msg = (e && e.message) ? e.message : String(e);
    if (/timeout|پاسخی از سرور/i.test(msg)) return msg;
    if (/Failed to fetch|NetworkError|network/i.test(msg)) return 'اتصال به سرور برقرار نشد. اینترنت را بررسی کنید.';
    if (/Invalid login credentials/i.test(msg)) return 'ایمیل یا رمز عبور اشتباه است.';
    return msg;
  }

  const loginBtn = el('button', {
    class: 'btn btn-primary btn-block',
    onclick: async () => {
      if (busy) return;
      errorEl.textContent = ''; infoEl.textContent = '';
      if (!emailInput.value.trim() || !passInput.value) {
        errorEl.textContent = 'ایمیل و رمز عبور را وارد کنید.';
        return;
      }
      setBusy(true);
      try {
        const { error } = await signIn(emailInput.value.trim(), passInput.value);
        if (error) { errorEl.textContent = 'ورود ناموفق: ' + friendlyError(error); return; }
        navigate('#/dashboard');
      } catch (e) {
        errorEl.textContent = friendlyError(e);
      } finally {
        setBusy(false);
      }
    }
  }, 'ورود');

  const signupBtn = el('button', {
    class: 'btn btn-outline btn-block',
    onclick: async () => {
      if (busy) return;
      errorEl.textContent = ''; infoEl.textContent = '';
      if (!emailInput.value.trim() || passInput.value.length < 6) {
        errorEl.textContent = 'ایمیل معتبر و رمز حداقل ۶ کاراکتری وارد کنید.';
        return;
      }
      setBusy(true);
      try {
        const { error } = await signUp(emailInput.value.trim(), passInput.value);
        if (error) { errorEl.textContent = 'ثبت‌نام ناموفق: ' + friendlyError(error); return; }
        infoEl.textContent = 'حساب ساخته شد. اگر تأیید ایمیل لازم بود، لینک را در ایمیل خود بزنید، سپس وارد شوید.';
        showToast('حساب ساخته شد.');
      } catch (e) {
        errorEl.textContent = friendlyError(e);
      } finally {
        setBusy(false);
      }
    }
  }, 'ساخت حساب جدید (فقط بار اول)');

  box.appendChild(loginBtn);
  box.appendChild(signupBtn);

  wrap.appendChild(box);
  container.appendChild(wrap);
}
