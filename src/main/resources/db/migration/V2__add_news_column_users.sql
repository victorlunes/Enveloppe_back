ALTER TABLE elp_users
    ADD COLUMN qty_input INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_entry_on DATE;
