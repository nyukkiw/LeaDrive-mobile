package com.example.leadrive

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://djgznqnnekcjoafbzfcr.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRqZ3pucW5uZWtjam9hZmJ6ZmNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA5MDc0MTIsImV4cCI6MjA3NjQ4MzQxMn0.sd_27c9ArpIfrbYOe3kNQyvB8pwlh70LqiJk8KsOSHc"


    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}
