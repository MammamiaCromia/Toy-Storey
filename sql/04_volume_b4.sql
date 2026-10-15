USE loja;

-- =====================================================================
-- ESTUDO DE OTIMIZAÇÃO DE CONSULTAS
-- =====================================================================
SET SESSION cte_max_recursion_depth = 1000000;

-- PARÂMETROS ----------------------------------------------------------
SET @qtd_clientes = 100000; 
SET @qtd_vendas   = 200000;

-- TABELA AUXILIAR
DROP TABLE IF EXISTS aux_numeros;
CREATE TABLE aux_numeros (i INT PRIMARY KEY);
INSERT INTO aux_numeros (i)
WITH RECURSIVE n AS (SELECT 1 AS i UNION ALL SELECT i + 1 FROM n WHERE i < 200000)
SELECT i FROM n;

-- 1) CLIENTES
INSERT INTO pessoa (id_pessoa, nome, status)
SELECT 100000 + i,
       CONCAT(
         ELT(1 + MOD(i, 20), 'Ana','Bruno','Carla','Daniel','Eduarda','Felipe','Gabriela','Heitor','Isabela','Joao',
                              'Karina','Lucas','Mariana','Nicolas','Otavio','Paulo','Queila','Rafael','Sofia','Thiago'),
         ' ',
         ELT(1 + MOD(FLOOR(i / 20), 20), 'Silva','Souza','Oliveira','Santos','Pereira','Costa','Rodrigues','Almeida','Nascimento','Lima',
                                          'Araujo','Fernandes','Carvalho','Gomes','Martins','Rocha','Ribeiro','Barbosa','Teixeira','Moura'),
         ' ',
         ELT(1 + MOD(FLOOR(i / 400), 20), 'Alves','Cardoso','Dias','Freitas','Melo','Nunes','Pinto','Ramos','Vieira','Campos',
                                           'Cavalcanti','Monteiro','Moreira','Batista','Borges','Duarte','Farias','Lopes','Machado','Reis')),
       IF(MOD(i, 20) = 0, 'Inativo', 'Ativo')
FROM aux_numeros
WHERE i <= @qtd_clientes;

INSERT INTO cliente (id_pessoa, cpf, data_nascimento, pontos_fidelidade)
SELECT 100000 + i,
       CONCAT('2', LPAD(i, 10, '0')),                       -- CPF fictício, não colide com os da carga
       DATE('1950-01-01') + INTERVAL MOD(i * 7919, 20000) DAY,
       MOD(i, 40)
FROM aux_numeros
WHERE i <= @qtd_clientes;

-- 2) VENDAS
INSERT INTO venda (id_venda, id_funcionario, id_cliente, data_hora, valor_total, status)
SELECT 1000 + i,
       1 + MOD(i * 7, 40),                                              -- funcionários 1..40 da carga
       IF(MOD(i, 7) = 0, NULL, 100001 + MOD(i * 13, @qtd_clientes)),    -- ~14% sem cliente (caso de contorno)
       TIMESTAMP('2024-01-01 08:00:00') + INTERVAL MOD(i * 7919, 1000) DAY + INTERVAL MOD(i * 104729, 36000) SECOND,
       0,
       IF(MOD(i, 25) = 0, 'Cancelada', 'Concluída')                     -- ~4% canceladas
FROM aux_numeros
WHERE i <= @qtd_vendas;

-- 3) GERAÇÃO DE PRODUTOS DISTINTOS EM CADA VENDA
INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_congelado)
SELECT 1000 + n.i,
       p.id_produto,
       1 + MOD(n.i + k.j, 3),
       p.preco_venda
FROM aux_numeros n
JOIN (SELECT 0 AS j UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3) k
  ON k.j < 1 + MOD(n.i, 4)
JOIN produto p
  ON p.id_produto = 1 + MOD(n.i + k.j * 17, 60)
WHERE n.i <= @qtd_vendas;

-- 4) DEFINIÇÃO DO VALOR TOTAL
UPDATE venda v
JOIN (SELECT id_venda, SUM(quantidade * preco_congelado) AS total
      FROM item_venda
      WHERE id_venda > 1000
      GROUP BY id_venda) s ON s.id_venda = v.id_venda
SET v.valor_total = s.total
WHERE v.id_venda > 1000;

-- 5) PAGAMENTOS
INSERT INTO pagamento (id_venda, forma_pagamento, valor_pago)
SELECT id_venda,
       ELT(1 + MOD(id_venda, 4), 'Dinheiro', 'Débito', 'Crédito', 'Pix'),
       valor_total
FROM venda
WHERE id_venda > 1000 AND status = 'Concluída' AND valor_total > 0;

-- 6) DEVOLUÇÕES
INSERT INTO devolucao (id_venda, data_troca, motivo, tipo_destino)
SELECT id_venda,
       data_hora + INTERVAL (1 + MOD(id_venda, 6)) DAY,
       ELT(1 + MOD(id_venda, 4), 'Cliente desistiu da compra', 'Brinquedo com defeito de fabricacao',
                                 'Peca quebrada na embalagem', 'Produto errado entregue no caixa'),
       IF(MOD(id_venda, 3) = 0, 'Avaria', 'Normal')
FROM venda
WHERE id_venda > 1000 AND status = 'Concluída' AND MOD(id_venda, 50) = 1;

-- LIMPEZA
DROP TABLE aux_numeros;
ANALYZE TABLE pessoa, cliente, venda, item_venda, pagamento, devolucao, produto;

-- CONFERÊNCIA DE VOLUME
SELECT 'pessoa' AS tabela, COUNT(*) AS linhas FROM pessoa
UNION ALL SELECT 'cliente',    COUNT(*) FROM cliente
UNION ALL SELECT 'produto',    COUNT(*) FROM produto
UNION ALL SELECT 'venda',      COUNT(*) FROM venda
UNION ALL SELECT 'item_venda', COUNT(*) FROM item_venda
UNION ALL SELECT 'pagamento',  COUNT(*) FROM pagamento
UNION ALL SELECT 'devolucao',  COUNT(*) FROM devolucao;