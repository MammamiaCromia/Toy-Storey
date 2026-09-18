
CREATE DATABASE IF NOT EXISTS loja;
USE loja;

CREATE TABLE pessoa (
    id_pessoa   INT auto_increment,
    nome        VARCHAR(100) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'Ativo',
    CONSTRAINT pk_pessoa PRIMARY KEY (id_pessoa),
	CONSTRAINT ck_pessoa_status CHECK (status IN ('Ativo', 'Inativo'))
);
CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT,
    nome         VARCHAR(100) NOT NULL,
    percentual_reajuste_padrao   DECIMAL(5,2),
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nome UNIQUE (nome)
);
CREATE TABLE cliente (
    id_pessoa    INT,
    cpf          CHAR (11) NOT NULL,
    data_nascimento  DATE NOT NULL,
    pontos_fidelidade INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_cliente PRIMARY KEY (id_pessoa),
    CONSTRAINT uq_cliente_cpf UNIQUE (cpf),
    CONSTRAINT fk_cliente_pessoa FOREIGN KEY (id_pessoa) 
	REFERENCES pessoa (id_pessoa)
	ON DELETE CASCADE
    ON UPDATE CASCADE,
	CONSTRAINT ck_cliente_pontos_fidelidade CHECK (pontos_fidelidade >= 0)
);
CREATE TABLE funcionario(
    id_pessoa    INT,
    login        VARCHAR (100) NOT NULL,
    senha_hash   VARCHAR (255) NOT NULL,
    cargo        VARCHAR(20) NOT NULL,
    CONSTRAINT pk_funcionario PRIMARY KEY (id_pessoa),
    CONSTRAINT uq_funcionario_login UNIQUE (login),
    CONSTRAINT fk_funcionario_pessoa FOREIGN KEY (id_pessoa) 
	REFERENCES pessoa (id_pessoa)
	ON DELETE CASCADE
	ON UPDATE CASCADE,
    CONSTRAINT ck_funcionario_cargo CHECK (cargo IN ('Operador', 'Gerente'))
);
CREATE TABLE produto (
    id_produto           INT AUTO_INCREMENT,
    id_categoria         INT NOT NULL,
    codigo_barras       CHAR(13) NOT NULL,
    nome                 VARCHAR(150) NOT NULL,
    preco_custo          DECIMAL(5,2) NOT NULL,
    preco_venda          DECIMAL(5,2) NOT NULL,
    quantidade_estoque   INT NOT NULL DEFAULT 0,
    estoque_minimo       INT NOT NULL DEFAULT 0,
    classificacao_idade  INT NOT NULL DEFAULT 0,
    status               VARCHAR(20) NOT NULL DEFAULT 'Ativo',
    tipo                 VARCHAR(20) NOT NULL,

    CONSTRAINT pk_produto PRIMARY KEY (id_produto),
    CONSTRAINT uq_produto_codigo_barras UNIQUE (codigo_barras),
    CONSTRAINT fk_produto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
	CONSTRAINT ck_produto_estoque CHECK (quantidade_estoque >= 0),
	CONSTRAINT ck_produto_estoque_minimo CHECK (estoque_minimo >= 0),
	CONSTRAINT ck_produto_classificacao_idade CHECK (classificacao_idade >= 0),
    CONSTRAINT ck_produto_status CHECK (status IN ('Ativo', 'Inativo')),
    CONSTRAINT ck_produto_tipo CHECK (tipo IN ('Simples', 'Kit')),
    CONSTRAINT ck_produto_precos CHECK (
        preco_custo >= 0 
        AND preco_venda >= 0 
        AND preco_venda > preco_custo
	)
);
CREATE TABLE venda (
    id_venda        INT AUTO_INCREMENT,
    id_funcionario  INT NOT NULL,
    id_cliente      INT,
    data_hora       DATETIME NOT NULL,
    valor_total     DECIMAL(8,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'Concluída',

    CONSTRAINT pk_venda PRIMARY KEY (id_venda),

    CONSTRAINT fk_venda_funcionario FOREIGN KEY (id_funcionario)
        REFERENCES funcionario (id_pessoa)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_venda_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_pessoa)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT ck_venda_status CHECK (status IN ('Concluída', 'Cancelada'))
);
CREATE TABLE item_venda (
    id_venda          INT,
    id_produto        INT,
    quantidade        INT NOT NULL,
    preco_congelado   DECIMAL(5,2) NOT NULL,

    CONSTRAINT pk_item_venda PRIMARY KEY (id_venda, id_produto),

    CONSTRAINT fk_item_venda_venda FOREIGN KEY (id_venda)
        REFERENCES venda (id_venda)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_item_venda_produto FOREIGN KEY (id_produto)
        REFERENCES produto (id_produto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT ck_item_venda_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_item_venda_preco CHECK (preco_congelado >= 0)
);
CREATE TABLE pagamento (
    id_pagamento      INT AUTO_INCREMENT,
    id_venda          INT NOT NULL,
    forma_pagamento   VARCHAR(20) NOT NULL,
    valor_pago        DECIMAL(8,2) NOT NULL,

    CONSTRAINT pk_pagamento PRIMARY KEY (id_pagamento),

    CONSTRAINT fk_pagamento_venda FOREIGN KEY (id_venda)
        REFERENCES venda (id_venda)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT ck_pagamento_valor CHECK (valor_pago > 0),
    CONSTRAINT ck_pagamento_forma CHECK (forma_pagamento IN ('Dinheiro', 'Débito', 'Crédito', 'Pix'))
);
CREATE TABLE devolucao (
    id_devolucao   INT AUTO_INCREMENT,
    id_venda       INT NOT NULL,
    data_troca     DATETIME NOT NULL,
    motivo         VARCHAR(200) NOT NULL,
    tipo_destino   VARCHAR(20) NOT NULL,

    CONSTRAINT pk_devolucao PRIMARY KEY (id_devolucao),

    CONSTRAINT fk_devolucao_venda FOREIGN KEY (id_venda)
        REFERENCES venda (id_venda)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT ck_devolucao_tipo_destino CHECK (tipo_destino IN ('Normal', 'Avaria'))
);
CREATE TABLE kit_composicao (
    id_kit                  INT,
    id_componente           INT,
    quantidade_necessaria   INT NOT NULL,

    CONSTRAINT pk_kit_composicao PRIMARY KEY (id_kit, id_componente),

    CONSTRAINT fk_kit_composicao_kit FOREIGN KEY (id_kit)
        REFERENCES produto (id_produto)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,

    CONSTRAINT fk_kit_composicao_componente FOREIGN KEY (id_componente)
        REFERENCES produto (id_produto)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_kit_composicao_quantidade CHECK (quantidade_necessaria > 0),
    CONSTRAINT ck_kit_composicao_diferentes CHECK (id_kit <> id_componente)
);
CREATE INDEX idx_venda_data_hora ON venda (data_hora);
CREATE INDEX idx_produto_nome ON produto (nome);
