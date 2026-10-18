USE loja_toystorey;

CREATE OR REPLACE VIEW vw_resumo_vendas_diarias AS
SELECT DATE(v.data_hora)                              AS dia,
       COUNT(DISTINCT v.id_venda)                     AS qtd_vendas,
       SUM(iv.quantidade)                             AS itens_vendidos,
       SUM(iv.quantidade * iv.preco_congelado)        AS faturamento,
       ROUND(SUM(iv.quantidade * iv.preco_congelado)
             / COUNT(DISTINCT v.id_venda), 2)         AS ticket_medio
FROM venda v
INNER JOIN item_venda iv ON iv.id_venda = v.id_venda
WHERE v.status = 'Concluída'
GROUP BY DATE(v.data_hora);

CREATE OR REPLACE VIEW vw_login AS
SELECT f.id_pessoa, f.login, f.senha_hash, f.cargo, p.nome
FROM funcionario f
INNER JOIN pessoa p ON p.id_pessoa = f.id_pessoa
WHERE p.status = 'Ativo';

CREATE OR REPLACE VIEW vw_cliente_atendimento AS
SELECT c.id_pessoa, p.nome,
       CONCAT('***.***.', SUBSTRING(c.cpf, 7, 3), '-**') AS cpf_mascarado,
       c.pontos_fidelidade
FROM cliente c
INNER JOIN pessoa p ON p.id_pessoa = c.id_pessoa
WHERE p.status = 'Ativo';


CREATE USER IF NOT EXISTS 'app_gerente'@'localhost'  IDENTIFIED BY 'Gerente@2026';
CREATE USER IF NOT EXISTS 'app_operador'@'localhost' IDENTIFIED BY 'Operador@2026';
CREATE USER IF NOT EXISTS 'app_leitura'@'localhost'  IDENTIFIED BY 'Leitura@2026';
CREATE USER IF NOT EXISTS 'app_auth'@'localhost'     IDENTIFIED BY 'Auth@2026';

ALTER USER 'app_gerente'@'localhost'  IDENTIFIED BY 'Gerente@2026';
ALTER USER 'app_operador'@'localhost' IDENTIFIED BY 'Operador@2026';
ALTER USER 'app_leitura'@'localhost'  IDENTIFIED BY 'Leitura@2026';
ALTER USER 'app_auth'@'localhost'     IDENTIFIED BY 'Auth@2026';


REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app_gerente'@'localhost';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app_operador'@'localhost';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app_leitura'@'localhost';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app_auth'@'localhost';


GRANT SELECT, INSERT, UPDATE ON loja_toystorey.pessoa         TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT, UPDATE ON loja_toystorey.categoria      TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT, UPDATE ON loja_toystorey.cliente        TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT, UPDATE ON loja_toystorey.funcionario    TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT, UPDATE ON loja_toystorey.produto        TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT, UPDATE ON loja_toystorey.kit_composicao TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT, UPDATE ON loja_toystorey.venda          TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT, UPDATE ON loja_toystorey.devolucao      TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT         ON loja_toystorey.item_venda     TO 'app_gerente'@'localhost';
GRANT SELECT, INSERT         ON loja_toystorey.pagamento      TO 'app_gerente'@'localhost';
GRANT SELECT                 ON loja_toystorey.auditoria      TO 'app_gerente'@'localhost';


GRANT DELETE ON loja_toystorey.pessoa         TO 'app_gerente'@'localhost';
GRANT DELETE ON loja_toystorey.cliente        TO 'app_gerente'@'localhost';
GRANT DELETE ON loja_toystorey.funcionario    TO 'app_gerente'@'localhost';
GRANT DELETE ON loja_toystorey.produto        TO 'app_gerente'@'localhost';
GRANT DELETE ON loja_toystorey.categoria      TO 'app_gerente'@'localhost';
GRANT DELETE ON loja_toystorey.kit_composicao TO 'app_gerente'@'localhost';


GRANT SELECT ON loja_toystorey.vw_cliente_atendimento TO 'app_operador'@'localhost';
GRANT SELECT (id_pessoa, status)                             ON loja_toystorey.pessoa  TO 'app_operador'@'localhost';
GRANT SELECT (id_pessoa, data_nascimento, pontos_fidelidade) ON loja_toystorey.cliente TO 'app_operador'@'localhost';
GRANT UPDATE (pontos_fidelidade)                             ON loja_toystorey.cliente TO 'app_operador'@'localhost';
GRANT SELECT ON loja_toystorey.categoria      TO 'app_operador'@'localhost';
GRANT SELECT ON loja_toystorey.kit_composicao TO 'app_operador'@'localhost';
GRANT SELECT, UPDATE (quantidade_estoque)  ON loja_toystorey.produto TO 'app_operador'@'localhost';
GRANT SELECT, INSERT, UPDATE (valor_total) ON loja_toystorey.venda   TO 'app_operador'@'localhost';
GRANT SELECT, INSERT ON loja_toystorey.item_venda TO 'app_operador'@'localhost';
GRANT SELECT, INSERT ON loja_toystorey.pagamento  TO 'app_operador'@'localhost';


GRANT SELECT ON loja_toystorey.vw_resumo_vendas_diarias TO 'app_leitura'@'localhost';


GRANT SELECT ON loja_toystorey.vw_login TO 'app_auth'@'localhost';

FLUSH PRIVILEGES;
