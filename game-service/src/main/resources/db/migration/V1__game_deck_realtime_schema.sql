CREATE TABLE IF NOT EXISTS game_snapshots (
    id BIGSERIAL PRIMARY KEY,
    game_id UUID NOT NULL,
    version BIGINT NOT NULL,
    payload TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_game_snapshots_game_version UNIQUE (game_id, version)
);

CREATE INDEX IF NOT EXISTS idx_game_snapshots_game_version
    ON game_snapshots (game_id, version DESC);

CREATE TABLE IF NOT EXISTS game_action_logs (
    id BIGSERIAL PRIMARY KEY,
    game_id UUID NOT NULL,
    entry TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_game_action_logs_game_created
    ON game_action_logs (game_id, created_at ASC);

CREATE TABLE IF NOT EXISTS decks (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_decks_player
    ON decks (player_id);

CREATE TABLE IF NOT EXISTS deck_cards (
    id BIGSERIAL PRIMARY KEY,
    deck_id UUID NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    card_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    set_id VARCHAR(32) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    type VARCHAR(64) NOT NULL,
    subtype VARCHAR(128),
    basic_energy BOOLEAN NOT NULL,
    basic_pokemon BOOLEAN NOT NULL,
    ace_spec BOOLEAN NOT NULL,
    hp INTEGER,
    attack_damage INTEGER,
    attack_required_energy INTEGER
);

CREATE INDEX IF NOT EXISTS idx_deck_cards_deck
    ON deck_cards (deck_id);

CREATE TABLE IF NOT EXISTS cached_cards (
    card_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    set_id VARCHAR(32) NOT NULL,
    type VARCHAR(64) NOT NULL,
    subtype VARCHAR(128),
    basic_energy BOOLEAN NOT NULL,
    basic_pokemon BOOLEAN NOT NULL,
    ace_spec BOOLEAN NOT NULL,
    hp INTEGER,
    attack_damage INTEGER,
    attack_required_energy INTEGER,
    cached_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cached_cards_set
    ON cached_cards (set_id);
