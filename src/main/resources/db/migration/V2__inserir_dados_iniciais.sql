INSERT INTO setor (nome, sigla) VALUES
    ('Tecnologia da Informação', 'COTIC'),
    ('Manutenção', 'MANUT');

INSERT INTO categoria (nome, descricao, setor_id) VALUES
    ('Rede', 'Problemas de conexão', (SELECT id FROM setor WHERE sigla = 'COTIC')),
    ('Computadores', 'Problemas em computadores', (SELECT id FROM setor WHERE sigla = 'COTIC')),
    ('Elétrica', 'Problemas elétricos', (SELECT id FROM setor WHERE sigla = 'MANUT')),
    ('Hidráulica', 'Problemas hidráulicos', (SELECT id FROM setor WHERE sigla = 'MANUT'));

INSERT INTO usuario (nome, email, matricula, tipo, setor_id) VALUES
    ('Ana', 'ana@example.com', '1001', 'SOLICITANTE', NULL),
    ('Bruno', 'bruno@example.com', '1002', 'SOLICITANTE', NULL),
    ('Carlos', 'carlos@example.com', '1003', 'TECNICO',
        (SELECT id FROM setor WHERE sigla = 'COTIC')),
    ('Daniela', 'daniela@example.com', '1004', 'TECNICO',
        (SELECT id FROM setor WHERE sigla = 'MANUT'));
