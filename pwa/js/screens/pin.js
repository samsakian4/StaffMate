import { getSetting } from '../db.js';
import { el, sha256 } from '../utils.js';
import { navigate } from '../router.js';

export async function renderPin(container) {
  container.innerHTML = '';
  const wrap = el('div', { style: 'display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;padding:24px;' });
  wrap.appendChild(el('div', { style: 'font-size:20px;font-weight:600;margin-bottom:16px;' }, 'ورود PIN'));

  const input = el('input', { type: 'password', inputmode: 'numeric', maxlength: '8', placeholder: 'کد PIN', style: 'max-width:220px;text-align:center;' });
  wrap.appendChild(input);
  const errorEl = el('div', { style: 'color:var(--error);margin-top:8px;' }, '');
  wrap.appendChild(errorEl);

  const btn = el('button', { class: 'btn btn-primary', style: 'margin-top:14px;' }, 'ورود');
  wrap.appendChild(btn);
  container.appendChild(wrap);

  let attempts = 0;

  async function tryLogin() {
    const hash = await getSetting('pin_hash', '');
    const entered = await sha256(input.value.trim());
    if (hash && entered === hash) {
      sessionStorage.setItem('pin_verified', '1');
      navigate('#/dashboard');
    } else {
      attempts++;
      input.value = '';
      if (attempts >= 3) {
        btn.disabled = true;
        input.disabled = true;
        errorEl.textContent = 'تعداد تلاش‌ها زیاد است. ۳۰ ثانیه صبر کنید.';
        setTimeout(() => {
          attempts = 0;
          btn.disabled = false;
          input.disabled = false;
          errorEl.textContent = '';
        }, 30000);
      } else {
        errorEl.textContent = 'PIN اشتباه است.';
      }
    }
  }

  btn.addEventListener('click', tryLogin);
  input.addEventListener('keydown', (e) => { if (e.key === 'Enter') tryLogin(); });
}
