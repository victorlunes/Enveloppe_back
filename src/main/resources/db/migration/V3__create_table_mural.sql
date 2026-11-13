CREATE TABLE
    elp_mural (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES elp_users (id) ON DELETE CASCADE,
        title VARCHAR(255) NOT NULL,
        type VARCHAR(50) NOT NULL CONSTRAINT elp_mural_type_check CHECK (
            type IN (
                'CARREIRA',
                'CASA',
                'BENS',
                'DINHEIRO',
                'ESTUDOS',
                'SAUDE',
                'FAMILIA',
                'FE',
                'VIAGEM',
                'OUTRO'
            )
        ),
        observation TEXT,
        achieved_in TIMESTAMP,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_elp_mural_user_id ON elp_mural (user_id);
