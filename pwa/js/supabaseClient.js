import { SUPABASE_URL, SUPABASE_ANON_KEY } from './config.js';

// detectSessionInUrl is OFF on purpose: this app uses email/password auth
// only (no OAuth/magic-link redirects), and the router also uses the URL
// hash (#/login, #/dashboard, ...) for navigation. Leaving it on makes
// supabase-js try to parse every route change as an auth callback, which
// can silently interfere with the sign-in flow.
export const supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: false
  }
});
