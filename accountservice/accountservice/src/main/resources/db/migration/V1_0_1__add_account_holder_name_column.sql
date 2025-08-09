-- Add account_holder_name column to ACCOUNTS table
ALTER TABLE accounts 
ADD COLUMN IF NOT EXISTS account_holder_name VARCHAR(100) NOT NULL DEFAULT 'Unknown';

-- Update existing records with a default value if needed
-- Note: In a production environment, you would want to backfill this with actual data
-- from the Auth Service before making the column NOT NULL
-- For now, we're using a default value to satisfy the NOT NULL constraint
