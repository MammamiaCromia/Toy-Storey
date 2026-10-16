USE loja_toystorey;

CREATE USER IF NOT EXISTS 'app_gerente'@'localhost'
IDENTIFIED BY 'Gerente@2026';

CREATE USER IF NOT EXISTS 'app_operador'@'localhost'
IDENTIFIED BY 'Operador@2026';

CREATE USER IF NOT EXISTS 'app_leitura'@'localhost'
IDENTIFIED BY 'Leitura@2026';

CREATE USER IF NOT EXISTS 'app_auth'@'localhost'
IDENTIFIED BY 'Auth@2026';

-- GERENTE
GRANT SELECT, INSERT, UPDATE, DELETE ON loja.* TO 'app_gerente'@'localhost';
REVOKE DELETE ON loja.* FROM 'app_gerente'@'localhost';       -- REVOKE: venda não se apaga, se cancela
GRANT DELETE ON loja.pessoa         TO 'app_gerente'@'localhost';
GRANT DELETE ON loja.cliente        TO 'app_gerente'@'localhost';
GRANT DELETE ON loja.funcionario    TO 'app_gerente'@'localhost';
GRANT DELETE ON loja.produto        TO 'app_gerente'@'localhost';
GRANT DELETE ON loja.categoria      TO 'app_gerente'@'localhost';
GRANT DELETE ON loja.kit_composicao TO 'app_gerente'@'localhost';

-- OPERADOR
GRANT SELECT ON loja.vw_cliente_atendimento TO 'app_operador'@'localhost';
GRANT SELECT (id_pessoa, status)                              ON loja.pessoa  TO 'app_operador'@'localhost';
GRANT SELECT (id_pessoa, data_nascimento, pontos_fidelidade)  ON loja.cliente TO 'app_operador'@'localhost';
GRANT UPDATE (pontos_fidelidade)                              ON loja.cliente TO 'app_operador'@'localhost';
GRANT SELECT                       ON loja.categoria      TO 'app_operador'@'localhost';
GRANT SELECT                       ON loja.kit_composicao TO 'app_operador'@'localhost';
GRANT SELECT, UPDATE (quantidade_estoque) ON loja.produto TO 'app_operador'@'localhost';
GRANT SELECT, INSERT, UPDATE (valor_total) ON loja.venda  TO 'app_operador'@'localhost';
GRANT SELECT, INSERT               ON loja.item_venda     TO 'app_operador'@'localhost';
GRANT SELECT, INSERT               ON loja.pagamento      TO 'app_operador'@'localhost';

-- LEITURA: só o relatório consolidado
GRANT SELECT ON loja.vw_resumo_vendas_diarias TO 'app_leitura'@'localhost';

-- AUTH: só consegue ler a view de login
GRANT SELECT ON loja.vw_login TO 'app_auth'@'localhost';

FLUSH PRIVILEGES;