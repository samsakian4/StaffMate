import { DB } from '../db.js';
import { renderShell } from '../layout.js';
import { navigate } from '../router.js';
import { el, formatDate } from '../utils.js';
import { calculateScore, totalRecordCount } from '../scoring.js';

const TABS = [
  { key: 'positive', label: 'مثبت', store: 'positiveNotes' },
  { key: 'negative', label: 'منفی', store: 'negativeNotes' },
  { key: 'personality', label: 'شخصیتی', store: 'personalityNotes' },
  { key: 'disciplinary', label: 'انضباطی', store: 'disciplinaryRecords' }
];

let currentTab = 'positive';

export async function renderEmployeeProfile(container, params) {
  const employeeId = Number(params.id);
  const emp = await DB.get('employees', employeeId);

  const content = renderShell(container, {
    title: emp ? `${emp.firstName} ${emp.lastName}` : 'پروفایل',
    showBack: true,
    backHash: '#/employees',
    fab: {
      label: 'افزودن سابقه',
      onClick: () => navigate(`#/note-form/${currentTab}/${employeeId}/new`)
    }
  });

  if (!emp) {
    content.appendChild(el('div', { class: 'empty-state' }, 'پرسنل یافت نشد.'));
    return;
  }

  content.appendChild(el('div', { style: 'color:var(--text-muted);margin-bottom:10px;font-size:13px;' },
    `کد: ${emp.personnelCode}  |  ${emp.position || '-'}  |  ${emp.workplace || '-'}  |  ${emp.shift || '-'}`));

  const [positives, negatives, personalities, disciplinaries] = await Promise.all([
    DB.getAllByIndex('positiveNotes', 'employeeId', employeeId),
    DB.getAllByIndex('negativeNotes', 'employeeId', employeeId),
    DB.getAllByIndex('personalityNotes', 'employeeId', employeeId),
    DB.getAllByIndex('disciplinaryRecords', 'employeeId', employeeId)
  ]);
  const sortByDateDesc = (a, b) => b.date - a.date;
  positives.sort(sortByDateDesc); negatives.sort(sortByDateDesc);
  personalities.sort(sortByDateDesc); disciplinaries.sort(sortByDateDesc);

  const total = await totalRecordCount(employeeId);
  const score = await calculateScore(employeeId);

  const summaryCard = el('div', { class: 'card' });
  summaryCard.appendChild(el('div', { style: 'font-weight:600;margin-bottom:6px;' }, 'خلاصه عملکرد'));
  if (total < 3) {
    summaryCard.appendChild(el('div', { style: 'color:var(--text-muted);' }, 'اطلاعات کافی برای ارزیابی وجود ندارد.'));
  } else {
    const cls = score > 0 ? 'score-positive' : (score < 0 ? 'score-negative' : 'score-neutral');
    summaryCard.appendChild(el('div', { class: cls, style: 'font-weight:600;font-size:16px;' }, `امتیاز فعلی: ${score}`));
  }
  summaryCard.appendChild(el('div', { style: 'font-size:12px;color:var(--text-muted);margin-top:6px;' },
    `مثبت: ${positives.length}   منفی: ${negatives.length}   شخصیتی: ${personalities.length}   انضباطی: ${disciplinaries.length}`));
  content.appendChild(summaryCard);

  const dataMap = { positive: positives, negative: negatives, personality: personalities, disciplinary: disciplinaries };

  const tabsEl = el('div', { class: 'tabs' });
  TABS.forEach(t => {
    tabsEl.appendChild(el('div', {
      class: 'tab' + (currentTab === t.key ? ' active' : ''),
      onclick: () => { currentTab = t.key; renderEmployeeProfile(container, params); }
    }, `${t.label} (${dataMap[t.key].length})`));
  });
  content.appendChild(tabsEl);

  const listEl = el('div', {});
  const items = dataMap[currentTab];
  if (items.length === 0) {
    listEl.appendChild(el('div', { class: 'empty-state' }, [
      el('div', { class: 'title' }, 'هنوز موردی ثبت نشده'),
      el('div', {}, 'با دکمه + یک مورد اضافه کنید')
    ]));
  } else {
    items.forEach(item => {
      let title, subtitle, body;
      if (currentTab === 'positive') {
        title = item.title; subtitle = `${formatDate(item.date)} · اهمیت: ${item.importance}`; body = item.description;
      } else if (currentTab === 'negative') {
        title = item.title; subtitle = `${formatDate(item.date)} · اهمیت: ${item.importance} · ${item.status}`; body = item.description;
      } else if (currentTab === 'personality') {
        title = `${item.title} (${item.type})`; subtitle = formatDate(item.date); body = item.description;
      } else {
        title = item.violationType; subtitle = `${formatDate(item.date)} · شدت: ${item.severity}`; body = item.description;
      }
      listEl.appendChild(el('div', {
        class: 'list-item',
        onclick: () => navigate(`#/note-form/${currentTab}/${employeeId}/${item.id}`)
      }, [
        el('div', { class: 'title' }, title),
        el('div', { class: 'subtitle' }, subtitle),
        body ? el('div', { class: 'body' }, body) : null
      ]));
    });
  }
  content.appendChild(listEl);
}
