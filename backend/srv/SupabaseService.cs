using Supabase;

namespace srv
{
    public class SupabaseService
    {
        private static Supabase.Client? _client;

        public SupabaseService(string url, string? key, Supabase.SupabaseOptions options)
        {

            _client = new Supabase.Client(url, key, options);
        }
        public async Task InitializeAsync()
        {
            if (_client == null)
                {   throw new Exception("Supabase client not initialized"); }
                
            await _client.InitializeAsync();
        }
        public static Supabase.Client GetClient()
        {
            if (_client == null) { throw new Exception("Supabase client not initialized"); }
            return _client;
        }
    }
}
