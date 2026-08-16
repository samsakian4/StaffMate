// Supabase-backed data layer for StaffMate PWA.
// Keeps the exact same DB.* API surface the screens already use, so no
// screen code needs to change — only the storage backend moved from
// IndexedDB (local-only) to Supabase (online, synced across devices).
import { supabase } from './supabaseClient.js';

const STORE_TO_TABLE = {
  employees: 'employees',
  positiveNotes: 'positive_notes',
  negativeNotes: 'negative_notes',
  personalityNotes: 'personality_notes',
  disciplinaryRecords: 'disciplinary_records',
  settings: 'settings'
};

function msToDateStr(ms) {
  const d = new Date(ms);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}
function dateStrToMs(str) {
  if (!str) return Date.now();
  const [y, m, d] = str.split('-').map(Number);
  return new Date(y, m - 1, d).getTime();
}

function toDbRow(store, rec) {
  if (store === 'employees') {
    const row = {
      personnel_code: rec.personnelCode,
      first_name: rec.firstName,
      last_name: rec.lastName,
      position: rec.position || '',
      workplace: rec.workplace || '',
      shift: rec.shift || '',
      active: rec.active,
      updated_at: new Date().toISOString()
    };
    if (rec.id) row.id = rec.id;
    return row;
  }
  if (store === 'settings') {
    return { key: rec.key, value: rec.value };
  }
  const table = STORE_TO_TABLE[store];
  const base = { employee_id: rec.employeeId, date: msToDateStr(rec.date) };
  if (rec.id) base.id = rec.id;
  if (table === 'positive_notes') return { ...base, title: rec.title, description: rec.description || '', importance: rec.importance };
  if (table === 'negative_notes') return { ...base, title: rec.title, description: rec.description || '', importance: rec.importance, status: rec.status };
  if (table === 'personality_notes') return { ...base, type: rec.type, title: rec.title, description: rec.description || '' };
  if (table === 'disciplinary_records') return { ...base, violation_type: rec.violationType, description: rec.description || '', severity: rec.severity, action_taken: rec.actionTaken || '', additional_notes: rec.additionalNotes || '' };
  return rec;
}

function fromDbRow(store, row) {
  if (!row) return row;
  if (store === 'employees') {
    return {
      id: row.id,
      personnelCode: row.personnel_code,
      firstName: row.first_name,
      lastName: row.last_name,
      position: row.position || '',
      workplace: row.workplace || '',
      shift: row.shift || '',
      active: row.active,
      createdAt: row.created_at,
      updatedAt: row.updated_at
    };
  }
  if (store === 'settings') {
    return { key: row.key, value: row.value };
  }
  const table = STORE_TO_TABLE[store];
  const base = { id: row.id, employeeId: row.employee_id, date: dateStrToMs(row.date), createdAt: row.created_at };
  if (table === 'positive_notes') return { ...base, title: row.title, description: row.description || '', importance: row.importance };
  if (table === 'negative_notes') return { ...base, title: row.title, description: row.description || '', importance: row.importance, status: row.status };
  if (table === 'personality_notes') return { ...base, type: row.type, title: row.title, description: row.description || '' };
  if (table === 'disciplinary_records') return { ...base, violationType: row.violation_type, description: row.description || '', severity: row.severity, actionTaken: row.action_taken || '', additionalNotes: row.additional_notes || '' };
  return row;
}

function checkError(error) {
  if (error) throw new Error(error.message || 'خطای ارتباط با سرور');
}

export const DB = {
  async add(store, value) {
    const table = STORE_TO_TABLE[store];
    const row = toDbRow(store, value);
    delete row.id;
    if (store === 'settings') {
      const { error } = await supabase.from(table).upsert(row, { onConflict: 'owner_id,key' });
      checkError(error);
      return value.key;
    }
    const { data, error } = await supabase.from(table).insert(row).select().single();
    checkError(error);
    return data.id;
  },
  async put(store, value) {
    const table = STORE_TO_TABLE[store];
    const row = toDbRow(store, value);
    if (store === 'settings') {
      const { error } = await supabase.from(table).upsert(row, { onConflict: 'owner_id,key' });
      checkError(error);
      return value;
    }
    const { data, error } = await supabase.from(table).upsert(row, { onConflict: 'id' }).select().single();
    checkError(error);
    return fromDbRow(store, data);
  },
  async get(store, id) {
    const table = STORE_TO_TABLE[store];
    let query = supabase.from(table).select('*');
    query = store === 'settings' ? query.eq('key', id) : query.eq('id', id);
    const { data, error } = await query.maybeSingle();
    checkError(error);
    return data ? fromDbRow(store, data) : null;
  },
  async delete(store, id) {
    const table = STORE_TO_TABLE[store];
    let query = supabase.from(table).delete();
    query = store === 'settings' ? query.eq('key', id) : query.eq('id', id);
    const { error } = await query;
    checkError(error);
  },
  async getAll(store) {
    const table = STORE_TO_TABLE[store];
    const { data, error } = await supabase.from(table).select('*');
    checkError(error);
    return (data || []).map(r => fromDbRow(store, r));
  },
  async getAllByIndex(store, indexName, value) {
    const table = STORE_TO_TABLE[store];
    const column = indexName === 'employeeId' ? 'employee_id' : indexName;
    const { data, error } = await supabase.from(table).select('*').eq(column, value);
    checkError(error);
    return (data || []).map(r => fromDbRow(store, r));
  },
  async count(store) {
    const table = STORE_TO_TABLE[store];
    const { count, error } = await supabase.from(table).select('*', { count: 'exact', head: true });
    checkError(error);
    return count || 0;
  },
  async clearAll() {
    const stores = Object.keys(STORE_TO_TABLE);
    for (const store of stores) {
      const table = STORE_TO_TABLE[store];
      if (store === 'settings') {
        await supabase.from(table).delete().neq('key', '__never__');
      } else {
        await supabase.from(table).delete().neq('id', -1);
      }
    }
  },
  async exportAll() {
    const stores = Object.keys(STORE_TO_TABLE);
    const out = {};
    for (const store of stores) out[store] = await DB.getAll(store);
    out.__meta = { app: 'StaffMate', exportedAt: new Date().toISOString(), source: 'supabase' };
    return out;
  },
  async importAll(data) {
    await DB.clearAll();
    const idMap = {};
    const employees = data.employees || [];
    for (const emp of employees) {
      const clone = { ...emp };
      const oldId = clone.id;
      delete clone.id;
      const newId = await DB.add('employees', clone);
      idMap[oldId] = newId;
    }
    const noteStores = ['positiveNotes', 'negativeNotes', 'personalityNotes', 'disciplinaryRecords'];
    for (const store of noteStores) {
      const rows = data[store] || [];
      for (const row of rows) {
        const clone = { ...row };
        delete clone.id;
        clone.employeeId = idMap[clone.employeeId] ?? clone.employeeId;
        await DB.add(store, clone);
      }
    }
    const settingsRows = data.settings || [];
    for (const row of settingsRows) {
      await DB.put('settings', { key: row.key, value: row.value });
    }
  }
};

export const DEFAULT_SETTINGS = {
  weight_positive: '1',
  weight_negative: '-1',
  severity_low: '-1',
  severity_medium: '-3',
  severity_high: '-5',
  severity_very_high: '-8',
  shifts_list: 'صبح,عصر,شب',
  positions_list: 'اپراتور,سرشیفت,کارگر ساده,تکنسین',
  workplaces_list: 'خط تولید ۱,خط تولید ۲,انبار',
  violation_types_list: 'غیبت,تاخیر,عدم رعایت ایمنی,نافرمانی,سایر',
  importance_list: 'کم,متوسط,زیاد',
  positive_titles_list: 'همکاری خوب,رعایت ایمنی,کیفیت کار بالا,کمک به همکاران,وقت‌شناسی,پیشنهاد بهبود',
  negative_titles_list: 'تاخیر,غیبت,کیفیت پایین کار,عدم رعایت ایمنی,بی‌نظمی,عدم همکاری',
  personality_titles_list: 'مسئولیت‌پذیر,منظم,همکاری‌جو,کم‌حوصله,منفعل,پرانرژی,کم‌حرف',
  pin_enabled: '0',
  pin_hash: '',
  last_backup_date: ''
};

export async function ensureDefaultSettings() {
  const existing = await DB.getAll('settings');
  const existingKeys = new Set(existing.map(s => s.key));
  for (const [key, value] of Object.entries(DEFAULT_SETTINGS)) {
    if (!existingKeys.has(key)) await DB.put('settings', { key, value });
  }
}

export async function getSetting(key, fallback = '') {
  const row = await DB.get('settings', key);
  return row ? row.value : fallback;
}

export async function setSetting(key, value) {
  return DB.put('settings', { key, value });
}
