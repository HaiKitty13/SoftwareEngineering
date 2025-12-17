package edu.bluejack24_2.nasigoyeng.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient

object SupabaseService {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://btjoxzyeoviwpclqzywp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ0am94enllb3Zpd3BjbHF6eXdwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMzMjk0ODYsImV4cCI6MjA3ODkwNTQ4Nn0.ugmX_cnLApQyhvyBlOf5CmYEttTUraZGCjRT8hTWZ9w"
        ) {
            install(io.github.jan.supabase.storage.Storage)
        }
    }
}