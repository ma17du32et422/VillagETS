using Supabase;

namespace srv
{
    public class SupabaseService
    {
        private static Supabase.Client? _client;

        public SupabaseService(string url, string? key, Supabase.SupabaseOptions options)
        {
            if (string.IsNullOrWhiteSpace(url))
                throw new ArgumentException("Supabase URL is required.", nameof(url));
            if (string.IsNullOrWhiteSpace(key))
                throw new ArgumentException("Supabase key is required.", nameof(key));

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
