USE loja;

-- ============================================================
-- CONSULTAS BÁSICAS
-- ============================================================

-- CONSULTA 01
-- Pergunta de negócio: Quais são os produtos ativos cadastrados na loja?

SELECT
	id_produto, nome, codigo_barras, preco_venda, quantidade_estoque
	FROM produto
	WHERE status = 'Ativo'
ORDER BY nome;

-- CONSULTA 02
-- Pergunta de negócio: Quais produtos possuem preço de venda entre R$ 10,00 e R$ 100,00?

SELECT
	id_produto, nome, preco_custo, preco_venda
	FROM produto
	WHERE preco_venda BETWEEN 10.00 AND 100.00
ORDER BY preco_venda;

-- CONSULTA 03
-- Pergunta de negócio: Quais produtos possuem nomes que contêm a palavra "boneca"?

SELECT
	id_produto, nome, preco_venda, quantidade_estoque
	FROM produto
	WHERE nome LIKE '%boneca%'
ORDER BY nome;

-- CONSULTA 04
-- Pergunta de negócio: Quais vendas foram realizadas para clientes cujo cadastro ainda possui CPF registrado?

SELECT
	v.id_venda, v.data_hora, v.id_cliente, v.valor_total, v.status
	FROM venda AS v
	WHERE v.id_cliente IN (
	SELECT c.id_pessoa
	FROM cliente AS c
	WHERE c.cpf IS NOT NULL
	)
ORDER BY v.data_hora DESC;

-- CONSULTA 05
-- Pergunta de negócio:
-- Quais vendas não possuem cliente associado?
-- Isso permite identificar vendas realizadas sem cadastro de cliente.

SELECT
	id_venda, data_hora, valor_total, status
	FROM venda
	WHERE id_cliente IS NULL
ORDER BY data_hora DESC;

-- CONSULTA 06
-- Pergunta de negócio: Quais funcionários realizaram cada venda e qual foi o valor de cada venda?

SELECT
	v.id_venda, v.data_hora, p.nome AS funcionario, v.valor_total, v.status
	FROM venda AS v
	INNER JOIN funcionario AS f
		ON v.id_funcionario = f.id_pessoa
	INNER JOIN pessoa AS p
		ON f.id_pessoa = p.id_pessoa
ORDER BY v.data_hora DESC;

-- CONSULTA 07
-- Pergunta de negócio: Quais categorias possuem produtos cadastrados e qual é a quantidade de produtos em cada categoria?

SELECT
	c.id_categoria, c.nome AS categoria, COUNT(pr.id_produto) AS quantidade_produtos
	FROM categoria AS c
	LEFT JOIN produto AS pr
		ON c.id_categoria = pr.id_categoria
	GROUP BY
		c.id_categoria, c.nome
ORDER BY quantidade_produtos DESC;

-- CONSULTA 08
-- Pergunta de negócio: Quais produtos já foram vendidos e qual foi a quantidade total vendida de cada produto?

SELECT
	p.id_produto, p.nome, SUM(iv.quantidade) AS quantidade_vendida
	FROM produto AS p
	INNER JOIN item_venda AS iv
		ON p.id_produto = iv.id_produto
	INNER JOIN venda AS v
		ON iv.id_venda = v.id_venda
	WHERE v.status = 'Concluída'
	GROUP BY
		p.id_produto, p.nome
ORDER BY quantidade_vendida DESC;

-- CONSULTA 09
-- Pergunta de negócio:
-- Quais funcionários realizaram mais de 5 vendas concluídas?

SELECT
	f.id_pessoa AS id_funcionario, p.nome AS funcionario, COUNT(v.id_venda) AS quantidade_vendas
	FROM funcionario AS f
	INNER JOIN pessoa AS p
		ON f.id_pessoa = p.id_pessoa
	INNER JOIN venda AS v
		ON f.id_pessoa = v.id_funcionario
	WHERE v.status = 'Concluída'
	GROUP BY
		f.id_pessoa, p.nome
	HAVING COUNT(v.id_venda) > 5
ORDER BY quantidade_vendas DESC;

-- CONSULTA 10
-- Pergunta de negócio:
-- Quais clientes realizaram compras e qual foi o valor total gasto por cada um?

SELECT
	c.id_pessoa AS id_cliente, p.nome AS cliente,
	COUNT(v.id_venda) AS quantidade_compras,
	SUM(v.valor_total) AS valor_total_gasto
	FROM cliente AS c
	INNER JOIN pessoa AS p
		ON c.id_pessoa = p.id_pessoa
	INNER JOIN venda AS v
		ON c.id_pessoa = v.id_cliente
	WHERE v.status = 'Concluída'
	GROUP BY
		c.id_pessoa, p.nome
ORDER BY valor_total_gasto DESC;

-- CONSULTA 11
-- A subconsulta utiliza a categoria do produto da consulta externa, caracterizando uma subconsulta correlacionada.

SELECT
	pr.id_produto, pr.nome, pr.preco_venda, pr.id_categoria
	FROM produto AS pr
	WHERE pr.preco_venda > (
	SELECT AVG(pr2.preco_venda)
	FROM produto AS pr2
	WHERE pr2.id_categoria = pr.id_categoria
	)
ORDER BY pr.id_categoria, pr.preco_venda DESC;

-- CONSULTA 12

-- Pergunta de negócio: Quais clientes já realizaram pelo menos uma venda concluída?

SELECT
	c.id_pessoa AS id_cliente, p.nome AS cliente, c.pontos_fidelidade
	FROM cliente AS c
	INNER JOIN pessoa AS p
		ON c.id_pessoa = p.id_pessoa
	WHERE EXISTS (
		SELECT 1
		FROM venda AS v
		WHERE v.id_cliente = c.id_pessoa
		AND v.status = 'Concluída'
		)
ORDER BY p.nome;

-- CONSULTA 13

-- Pergunta de negócio: Quais produtos estão abaixo ou no estoque mínimo e, portanto, precisam de reposição?

SELECT
	p.id_produto, p.nome, c.nome AS categoria, p.quantidade_estoque,
    p.estoque_minimo, 
    p.estoque_minimo - p.quantidade_estoque AS quantidade_para_repor
	FROM produto AS p
	INNER JOIN categoria AS c
		ON p.id_categoria = c.id_categoria
	WHERE p.quantidade_estoque <= p.estoque_minimo
	AND p.status = 'Ativo'
ORDER BY quantidade_para_repor DESC;

-- CONSULTA 14

-- A consulta identifica clientes cujo consumo está acima da média geral dos clientes compradores.

SELECT
	c.id_pessoa AS id_cliente, p.nome AS cliente,
	SUM(v.valor_total) AS total_gasto
	FROM cliente AS c
	INNER JOIN pessoa AS p
		ON c.id_pessoa = p.id_pessoa
	INNER JOIN venda AS v
		ON c.id_pessoa = v.id_cliente
	WHERE v.status = 'Concluída'
	GROUP BY
		c.id_pessoa, p.nome
	HAVING SUM(v.valor_total) > (
		SELECT AVG(total_cliente)
		FROM (
			SELECT
				id_cliente,
				SUM(valor_total) AS total_cliente
				FROM venda
			WHERE status = 'Concluída'
				AND id_cliente IS NOT NULL
			GROUP BY id_cliente
			) AS totais
		)
ORDER BY total_gasto DESC;

-- CONSULTA 15

-- Essa consulta auxilia a identificar kits cuja montagem pode ser prejudicada pela falta de componentes.

SELECT DISTINCT
	kit.id_produto AS id_kit, kit.nome AS kit,
	componente.id_produto AS id_componente,
	componente.nome AS componente,
	componente.quantidade_estoque,
	componente.estoque_minimo
	FROM kit_composicao AS kc
	INNER JOIN produto AS kit
		ON kc.id_kit = kit.id_produto
	INNER JOIN produto AS componente
		ON kc.id_componente = componente.id_produto
	WHERE kit.tipo = 'Kit'
		AND componente.quantidade_estoque <= componente.estoque_minimo
ORDER BY kit.nome, componente.nome;
