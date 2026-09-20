# Sistema de Controle de Vendas — Toy Storey

Projeto final da disciplina **Laboratório de Banco de Dados (GPE17M40083)** — Bacharelado em Engenharia de Software, Universidade Católica de Brasília.

- **Professor:** Samuel Novais Moura Júnior
- **Ano/semestre:** 2026/2

## Equipe

Integrantes

_[José Antônio Rodrigues Tozetti]_
_[Henrique Caio]_
_[Gustavo Teixeira]_

## Tema do projeto

Sistema de Venda para uma loja de brinquedos fictícia, a **Toy Storey**. O sistema cobre o ciclo completo de uma venda: cadastro de clientes e funcionários (generalização/especialização de PESSOA), catálogo de produtos organizado por categoria, composição de kits promocionais (autorrelacionamento em PRODUTO), registro de vendas com múltiplas formas de pagamento, devoluções/trocas e um programa de fidelidade por pontos.

## Estrutura do repositório

```
repositorio/
├── README.md
├── docs/
│   ├── relatorio-etapa1.pdf
│   ├── mer-conceitual.pdf
│   ├── modelo-logico.pdf
│   ├── normalicao.pdf
│   ├── regras-negocio.pdf
│   └── dicionario-dados.pdf
└── sql/
    ├── 01_ddl.sql
    ├── 02_carga.sql
    └── 03_consultas.sql
```

## Como executar (Etapa 1)

1. Crie um banco limpo no SGBD mysql.
2. Execute `sql/01_ddl.sql` do início ao fim — cria o banco e todas as tabelas, com todas as restrições nomeadas (`pk_`, `uq_`, `fk_`, `ck_`, `idx_`).
3. Execute `sql/02_carga.sql` em seguida, respeitando a ordem de dependência entre as tabelas.
4. As 15 consultas de verificação, cada uma comentada com a pergunta de negócio que responde, estão em `sql/03_consultas.sql`.

## Status do projeto

Esta entrega corresponde à **Etapa 1 (N1)** — projeto e construção do banco: modelo conceitual, modelo lógico, verificação de normalização, script físico, carga de dados e consultas. A **Etapa 2 (N2)** — aplicação com interface, transações, controle de concorrência, otimização de consultas, segurança e auditoria — será entregue em 22/11/2026.

## Uso de Inteligência Artificial

O uso de assistentes de IA foi declarado pela equipe, conforme exigido no item 9 do enunciado. A ferramenta foi utilizada como apoio em:

- **Assistência em código**, incluindo trechos de scripts SQL (DDL, carga e consultas);
- **Esclarecimento de dúvidas** sobre conceitos de modelagem, normalização e mapeamento ER-relacional ao longo do desenvolvimento do projeto;
- **Otimização da escrita** dos documentos entregáveis, revisando clareza e organização do texto;
- **Unificação dos documentos** produzidos separadamente pelos integrantes (regras de negócio, dicionário de dados, modelo lógico e verificação de normalização) em um único relatório consolidado (`relatorio-etapa1.pdf`).

Todo o conteúdo gerado ou revisado com apoio de IA foi verificado e validado pela equipe antes da entrega, que se responsabiliza integralmente pelo material submetido.
