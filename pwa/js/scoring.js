import { DB, getSetting } from './db.js';

export async function calculateScore(employeeId, sinceMillis = 0) {
  const wPos = parseInt(await getSetting('weight_positive', '1')) || 1;
  const wNeg = parseInt(await getSetting('weight_negative', '-1')) || -1;
  const sevLow = parseInt(await getSetting('severity_low', '-1')) || -1;
  const sevMed = parseInt(await getSetting('severity_medium', '-3')) || -3;
  const sevHigh = parseInt(await getSetting('severity_high', '-5')) || -5;
  const sevVeryHigh = parseInt(await getSetting('severity_very_high', '-8')) || -8;

  const positives = (await DB.getAllByIndex('positiveNotes', 'employeeId', employeeId)).filter(n => n.date >= sinceMillis);
  const negatives = (await DB.getAllByIndex('negativeNotes', 'employeeId', employeeId)).filter(n => n.date >= sinceMillis);
  const disciplinary = (await DB.getAllByIndex('disciplinaryRecords', 'employeeId', employeeId)).filter(n => n.date >= sinceMillis);

  let score = positives.length * wPos + negatives.length * wNeg;
  for (const rec of disciplinary) {
    switch (rec.severity) {
      case 'کم': score += sevLow; break;
      case 'متوسط': score += sevMed; break;
      case 'زیاد': score += sevHigh; break;
      case 'بسیار زیاد': score += sevVeryHigh; break;
    }
  }
  return score;
}

export async function totalRecordCount(employeeId) {
  const positives = await DB.getAllByIndex('positiveNotes', 'employeeId', employeeId);
  const negatives = await DB.getAllByIndex('negativeNotes', 'employeeId', employeeId);
  const disciplinary = await DB.getAllByIndex('disciplinaryRecords', 'employeeId', employeeId);
  return positives.length + negatives.length + disciplinary.length;
}
