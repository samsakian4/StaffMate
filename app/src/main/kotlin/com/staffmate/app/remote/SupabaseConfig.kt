package com.staffmate.app.remote

// Same Supabase project used by the PWA — this is how data is shared
// between the Android app and the web app. The anon key is safe to ship
// in the app; real protection comes from Row Level Security (owner_id = auth.uid()).
object SupabaseConfig {
    const val URL = "https://icaqywcbdvszvaprpxoy.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImljYXF5d2NiZHZzenZhcHJweG95Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODYyODI2OTUsImV4cCI6MjEwMTg1ODY5NX0.6MiASrFfNJU-j2yLDnjJ_DAP2_gdrA8WVP4Pzn8EQqU"
    const val REST_URL = "$URL/rest/v1"
    const val AUTH_URL = "$URL/auth/v1"
}
