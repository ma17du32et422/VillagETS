using System.Diagnostics;

namespace srv
{
    public static class PerfLogger
    {
        public static bool Enabled { get; set; } = true;

        public static async Task<T> TimeAsync<T>(ILogger logger, string label, Func<Task<T>> action, long warnThresholdMs = 250)
        {
            if (!Enabled)
            {
                return await action();
            }

            var sw = Stopwatch.StartNew();
            try
            {
                return await action();
            }
            finally
            {
                Log(logger, label, sw.ElapsedMilliseconds, warnThresholdMs);
            }
        }

        public static async Task TimeAsync(ILogger logger, string label, Func<Task> action, long warnThresholdMs = 250)
        {
            if (!Enabled)
            {
                await action();
                return;
            }

            var sw = Stopwatch.StartNew();
            try
            {
                await action();
            }
            finally
            {
                Log(logger, label, sw.ElapsedMilliseconds, warnThresholdMs);
            }
        }

        private static void Log(ILogger logger, string label, long elapsedMs, long warnThresholdMs)
        {
            if (elapsedMs >= warnThresholdMs)
            {
                logger.LogWarning("DB {Label} took {ElapsedMs}ms", label, elapsedMs);
                return;
            }

            logger.LogInformation("DB {Label} took {ElapsedMs}ms", label, elapsedMs);
        }
    }
}
