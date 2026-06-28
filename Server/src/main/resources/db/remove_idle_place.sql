-- Existing databases must run this once before starting the updated backend.
-- The platform is campus-scoped, so item-level city data is no longer stored.
ALTER TABLE sh_idle_item DROP COLUMN IF EXISTS idle_place;
